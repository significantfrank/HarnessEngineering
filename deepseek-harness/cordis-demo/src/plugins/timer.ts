/**
 * TimerService — a Service plugin that contributes disposable `ctx.timer`
 * helpers, modeled on the vendored `cordis-plugin-timer`.
 *
 * Every timer is registered through `ctx.effect`, so it is cleared
 * automatically when the owning fiber (or the whole context) disposes.
 */
import { Service } from 'cordis-demo-ts'
import type { Context, Disposable } from 'cordis-demo-ts'

/** Make `ctx.timer` visible to TypeScript via declaration merging. */
declare module 'cordis-demo-ts' {
    interface Context {
        timer: TimerService
    }
}

export class TimerService extends Service {
    static override provide = 'timer'

    constructor(ctx: Context) {
        super(ctx, 'timer')
    }

    /** Run `callback` every `delay` ms; the returned disposer stops it. */
    interval(callback: () => void, delay: number): Disposable {
        return this.ctx.effect(() => {
            const id = setInterval(callback, delay)
            return () => clearInterval(id as unknown as number)
        }, 'ctx.timer.interval()')
    }

    /** Run `callback` once after `delay` ms; the returned disposer cancels it. */
    timeout(callback: () => void, delay: number): Disposable {
        return this.ctx.effect(() => {
            const id = setTimeout(callback, delay)
            return () => clearTimeout(id as unknown as number)
        }, 'ctx.timer.timeout()')
    }
}

export default TimerService
