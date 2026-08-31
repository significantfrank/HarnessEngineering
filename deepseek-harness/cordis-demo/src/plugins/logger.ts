/**
 * LoggerService — a Service plugin that contributes `ctx.logger`.
 *
 * Mirrors a Cordis `Service` subclass: the constructor registers itself on
 * the context under a stable name, and the instance is removed when its
 * owning fiber unloads.
 */
import { Service } from 'cordis-demo-ts'
import type { Context } from 'cordis-demo-ts'

/** Make `ctx.logger` visible to TypeScript via declaration merging. */
declare module 'cordis-demo-ts' {
    interface Context {
        logger: LoggerService
    }
}

export class LoggerService extends Service {
    static override provide = 'logger'

    config: { prefix?: string }

    constructor(ctx: Context, config: { prefix?: string } = {}) {
        super(ctx, 'logger')
        this.config = config
    }

    info(msg: string) { console.log(`[info] ${msg}`) }
    warn(msg: string) { console.log(`[warn] ${msg}`) }
    error(msg: string) { console.error(`[error] ${msg}`) }
}

export default LoggerService