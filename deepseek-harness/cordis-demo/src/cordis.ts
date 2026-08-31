/**
 * Cordis — a minimal plugin framework (TypeScript).
 *
 * A didactic, from-scratch re-implementation of the *core* plugin ideas
 * underneath DeepSeek Harness (whose framework is Cordis). It keeps only the
 * heart of a plugin system and drops Cordis's production machinery
 * (reflection proxies, isolate scopes, epochs, async reload/unload, ...).
 *
 * ── Five ideas, in brief ──────────────────────────────────────────────
 *  1. A plugin is a function / class / { apply } object (or a module that
 *     exports `apply` + `inject`) mounted into a context.
 *  2. A context is a repository of services; a service claims a stable
 *     `ctx.<name>` and other plugins find it by key, not by import.
 *  3. `inject` declares service dependencies; a plugin loads only once every
 *     required service exists — so load order is expressed through deps.
 *  4. Events (`ctx.on` / `ctx.emit`) let plugins communicate.
 *  5. Registrations are reversible effects (`ctx.effect`); disposal unwinds
 *     them in reverse registration order.
 */

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------
/** A disposer: run to release a resource registered through `ctx.effect`. */
export type Disposable = () => any

/** Anything a plugin's `apply` (or an effect body) may return as its cleanup. */
export type Effect =
    | Disposable
    | Promise<Disposable | null | undefined>
    | Iterable<Disposable>
    | null
    | undefined

/** Service dependency declaration: an array of names, or a name→config map. */
export type Inject = string[] | Record<string, any>

/** Shared metadata understood by the plugin registry. */
export interface PluginBase<T = any> {
    name?: string
    inject?: Inject
    provide?: string | string[]
    Config?: any
}

/** Function plugin called with `(ctx, config)`. */
export interface PluginFunction<T = any> extends PluginBase<T> {
    (ctx: Context, config: T): any
}

/** Class plugin constructed with `(ctx, config)`. */
export interface PluginConstructor<T = any> extends PluginBase<T> {
    new(ctx: Context, config: T): any
}

/** Object plugin (or a module namespace) with an `apply(ctx, config)` method. */
export interface PluginObject<T = any> extends PluginBase<T> {
    apply(ctx: Context, config: T): any
}

/** Supported plugin entrypoint shapes. */
export type Plugin<T = any> = PluginFunction<T> | PluginConstructor<T> | PluginObject<T>

type Listener = (...args: any[]) => void

interface ServiceEntry {
    value: any
    fiber: Fiber
}

// ---------------------------------------------------------------------------
// DisposableList — a stack of unloaders, drained in reverse registration order.
// ---------------------------------------------------------------------------
export class DisposableList extends Array<Disposable> {
    /** Add a disposer; return a remover that detaches it without running it. */
    add(dispose: Disposable): () => void {
        this.push(dispose)
        let removed = false
        return () => {
            if (removed) return
            removed = true
            const i = this.indexOf(dispose)
            if (i >= 0) this.splice(i, 1)
        }
    }

    /** Remove every disposer and return them in reverse registration order. */
    drain(): Disposable[] {
        return this.splice(0).reverse()
    }
}

// ---------------------------------------------------------------------------
// Fiber state — the lifecycle of one plugin instance.
// ---------------------------------------------------------------------------
export const FiberState = {
    PENDING: 'pending', // waiting for required services
    LOADING: 'loading', // apply is running (transient, before ACTIVE)
    ACTIVE: 'active', // loaded and providing
    FAILED: 'failed', // the callback (or an effect) threw
    DISPOSED: 'disposed', // torn down and cannot restart
} as const

export type FiberState = (typeof FiberState)[keyof typeof FiberState]

// ---------------------------------------------------------------------------
// Fiber — owns one plugin's effects, services, and dependency state.
// ---------------------------------------------------------------------------
export class Fiber {
    parent: Context
    plugin: Plugin
    config: any
    name: string
    state: FiberState
    disposables: DisposableList
    ctx: Context
    inject: Record<string, any>
    _impls: Record<string, any>
    _error: any
    _removeFromParent: () => void

    /** Create the always-active root fiber that hosts app-level effects. */
    static root(ctx: Context): Fiber {
        const f = Object.create(Fiber.prototype) as Fiber
        f.parent = ctx
        f.plugin = { name: 'root', apply: () => { } }
        f.config = {}
        f.name = 'root'
        f.state = FiberState.ACTIVE
        f.disposables = new DisposableList()
        f.ctx = ctx
        f.inject = Object.create(null)
        f._impls = Object.create(null)
        f._error = undefined
        f._removeFromParent = () => { }
        return f
    }

    /** @param parent  @param plugin  @param config */
    constructor(parent: Context, plugin: Plugin, config: any) {
        this.parent = parent
        this.plugin = plugin
        this.config = config
        this.name = plugin.name || 'anonymous'
        this.state = FiberState.PENDING
        this.disposables = new DisposableList()
        /** The plugin's own context is a child of the parent context. */
        this.ctx = parent.extend({ fiber: this })
        /** Service name → resolved implementation snapshot (while loaded). */
        this.inject = normalizeInject(plugin.inject)
        this._impls = Object.create(null)
        this._error = undefined

        // Tie this fiber's teardown to the parent so parent.dispose() unwinds it.
        this._removeFromParent = parent.fiber.disposables.add(() => this._unload())
        parent.root._allFibers.add(this)
        parent.emit('internal/plugin', this)

        // Resolve dependencies now — may start the plugin immediately.
        this._refresh()
    }

    /**
     * Register a reversible effect. `execute` runs at once; the disposer it
     * returns (sync, a promise of one, or an iterable of them) is collected and
     * run in reverse order on unload. Returns a disposer that tears it down.
     */
    effect(execute: () => Effect, label = 'anonymous'): Disposable {
        if (this.state === FiberState.DISPOSED) {
            throw new Error('cannot create effect on a disposed fiber')
        }
        const locals: Disposable[] = []
        let draining = false
        const run = async (): Promise<void> => {
            if (draining) return
            draining = true
            for (const d of locals.splice(0).reverse()) {
                try { await Promise.resolve(d()) } catch (e) { this._report(e) }
            }
        }
        let result: Effect
        try {
            result = execute()
        } catch (e) {
            this._fail(e)
            throw e
        }
        collectEffect(result, locals, (e) => this._fail(e))
        const dispose = () => run()
        this.disposables.push(dispose)
        return dispose
    }

    /** Re-evaluate dependencies; start the plugin when they are satisfied. */
    _refresh(): void {
        if (this.state !== FiberState.PENDING) return
        let ready = true
        for (const name of Object.keys(this.inject)) {
            const impl = this.ctx._getImpl(name)
            this._impls[name] = impl
            if (!impl) { ready = false; break }
        }
        if (ready) this._start()
    }

    _start(): void {
        const ctx = this.ctx
        // Flip to LOADING synchronously, before apply runs, so a re-entrant
        // _refreshDependents (triggered when a Service registers itself via
        // _provide) does not start this fiber a second time.
        this.state = FiberState.LOADING
        let ret: any
        try {
            ret = invokePlugin(this.plugin, ctx, this.config)
        } catch (e) {
            this._fail(e)
            return
        }
        // When apply is synchronous, settle at once so the fiber is ACTIVE by the
        // time ctx.plugin() returns. Async apply (a promise) settles later.
        const settle = (settled: any): void => {
            collectEffect(settled, this.disposables, (e) => this._fail(e))
            if (this.state !== FiberState.DISPOSED) {
                this.state = FiberState.ACTIVE
                ctx.emit('internal/status', this)
            }
        }
        if (ret != null && typeof (ret as any).then === 'function') {
            ; (ret as Promise<any>).then(settle, (e: any) => this._fail(e))
        } else {
            settle(ret)
        }
    }

    async _unload(): Promise<void> {
        if (this.state === FiberState.DISPOSED) return
        this.state = FiberState.DISPOSED
        // Run this fiber's effects in reverse, then drop its services.
        for (const d of this.disposables.drain()) {
            try { await Promise.resolve(d()) } catch (e) { this._report(e) }
        }
        this.ctx._revokeServices(this)
        this.ctx.root._allFibers.delete(this)
    }

    dispose(): Promise<void> { return this._unload() }

    _fail(e: any): void {
        if (this.state === FiberState.DISPOSED) return
        this._error = e
        this.state = FiberState.FAILED
        this._report(e)
    }

    _report(e: any): void {
        console.error(`[cordis:${this.name}]`, e?.stack || e)
    }

    /** Await in-flight startup work and rethrow a startup error. */
    async await(): Promise<Fiber> {
        if (this._error) throw this._error
        return this
    }
}

// ---------------------------------------------------------------------------
// Service — base class for things that expose a named API on `ctx`.
// ---------------------------------------------------------------------------
export abstract class Service {
    ctx!: Context
    name!: string
    static provide?: string
    start?(ctx: Context): void

    /** Register this instance as `name` in the current context. */
    constructor(ctx: Context, name?: string) {
        name ??= (this.constructor as { provide?: string }).provide
        if (!name) throw new Error('Service requires a name or a static `provide`')
        this.ctx = ctx
        this.name = name
        ctx._provide(name, this, ctx.fiber)
        // Optional post-registration hook (mirrors Cordis `[Service.init]`).
        this.start?.(ctx)
    }
}

// ---------------------------------------------------------------------------
// Context — root dependency container and plugin host.
// ---------------------------------------------------------------------------
export class Context {
    root: Context
    fiber: Fiber
    _services: Record<string, ServiceEntry>
    _listeners: Map<string, Set<Listener>>
    _allFibers: Set<Fiber>

    constructor() {
        this.root = this
        /** All services live on the root: name → { value, fiber }. */
        this._services = Object.create(null)
        /** event name → Set<listener>. */
        this._listeners = new Map()
        /** Every non-disposed fiber, so a newly provided service can wake waiters. */
        this._allFibers = new Set()
        /** App-level effects + per-child unload entries. */
        this.fiber = Fiber.root(this)
    }

    /** Create a child context prototypally inheriting from this one. */
    extend(meta: Record<string, any> = {}): Context {
        const child = Object.create(this) as Context
        for (const prop of Reflect.ownKeys(meta)) {
            Object.defineProperty(child, prop, Reflect.getOwnPropertyDescriptor(meta, prop)!)
        }
        return child
    }

    /** Read a service implementation by name. */
    _getImpl(name: string): any {
        return this.root._services[name]?.value
    }

    /** Register a service under `name`, owned by `fiber`; returns a remover. */
    _provide(name: string, value: any, fiber: Fiber): () => void {
        this.root._services[name] = { value, fiber }
        Object.defineProperty(this.root, name, { value, configurable: true, enumerable: false })
        this._refreshDependents()
        return () => this._revoke(name, fiber)
    }

    /** Remove a service (only if still owned by `fiber`). */
    _revoke(name: string, fiber: Fiber): void {
        const entry = this.root._services[name]
        if (!entry || entry.fiber !== fiber) return
        delete this.root._services[name]
        delete (this.root as any)[name]
    }

    /** Remove every service owned by `fiber`. */
    _revokeServices(fiber: Fiber): void {
        for (const name of Object.keys(this.root._services)) {
            if (this.root._services[name].fiber === fiber) this._revoke(name, fiber)
        }
    }

    /** Re-run dependency resolution for every non-disposed fiber. */
    _refreshDependents(): void {
        for (const f of [...this.root._allFibers]) {
            if (f.state === FiberState.PENDING) f._refresh()
        }
    }

    /**
     * Load a plugin in the current context and return its fiber.
     * @param plugin — a function, class, { apply } object, or a module
     *   namespace that exports `apply` (and optional `inject`/`name`).
     * @param config — the plugin config.
     */
    plugin(plugin: Plugin, config?: any): Fiber {
        if (!isPlugin(plugin)) {
            throw new TypeError('invalid plugin: expect a function, class, or object/module with an apply method')
        }
        return new Fiber(this, plugin, config)
    }

    /** Run a callback once the requested services are available. */
    inject(deps: Inject, callback: PluginFunction<void>): Fiber {
        return this.plugin({ inject: deps, apply: callback, name: callback.name })
    }

    /** Register a reversible effect on the current fiber. */
    effect(execute: () => Effect, label?: string): Disposable {
        return this.fiber.effect(execute, label)
    }

    /** Subscribe to an event; returns a disposer that removes the listener. */
    on(event: string, listener: Listener): Disposable {
        let set = this.root._listeners.get(event)
        if (!set) this.root._listeners.set(event, (set = new Set()))
        return this.effect(() => {
            set!.add(listener)
            return () => set!.delete(listener)
        }, `ctx.on("${event}")`)
    }

    /** Dispatch an event to every listener registered for that name. */
    emit(event: string, ...args: any[]): void {
        const set = this.root._listeners.get(event)
        if (!set) return
        for (const fn of set) {
            try { fn(...args) } catch (e) { console.error(`[cordis:event:${event}]`, (e as Error)?.stack || e) }
        }
    }

    /** Tear the whole context (root fiber) down, running effects in reverse. */
    dispose(): Promise<void> {
        return this.fiber._unload()
    }
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------
function normalizeInject(inject: Inject | undefined): Record<string, any> {
    const out: Record<string, any> = Object.create(null)
    if (!inject) return out
    if (Array.isArray(inject)) {
        for (const name of inject) out[name] = null
    } else {
        for (const name of Object.keys(inject)) out[name] = (inject as Record<string, any>)[name]
    }
    return out
}

function isPlugin(p: any): p is Plugin {
    if (!p) return false
    if (typeof p === 'function') return true
    return typeof p.apply === 'function'
}

function invokePlugin(plugin: Plugin, ctx: Context, config: any): any {
    if (typeof plugin === 'function') {
        return isClass(plugin) ? new (plugin as any)(ctx, config) : (plugin as PluginFunction).call(ctx, ctx, config)
    }
    return (plugin as PluginObject).apply.call(ctx, ctx, config)
}

function isClass(fn: any): boolean {
    return typeof fn === 'function' && /^\s*class\b/.test(Function.prototype.toString.call(fn))
}

// Collect a sync / promise / iterable disposer into `bucket`.
function collectEffect(result: Effect, bucket: Disposable[], fail: (e: any) => void): void {
    if (result == null) return
    if (typeof result === 'function') { bucket.push(result); return }
    if (typeof (result as any).then === 'function') {
        ; (result as Promise<any>).then(
            (d: any) => { if (typeof d === 'function') bucket.push(d) },
            (e: any) => fail(e),
        )
        return
    }
    if (typeof (result as any)[Symbol.iterator] === 'function') {
        for (const d of result as Iterable<Disposable>) {
            if (typeof d === 'function') bucket.push(d)
        }
    }
}
