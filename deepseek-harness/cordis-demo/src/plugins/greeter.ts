/**
 * greeter — a *module* plugin.
 *
 * This file is loaded as a plugin via `import * as greeter`. A module that
 * exports `apply` (and optional `inject` / `name`) is a valid Cordis plugin
 * object, exactly like the `demo-hello-plugin` in the harness.
 *
 * `inject: ['logger']` means this plugin loads only once `ctx.logger`
 * exists — so it is safe to mount it before the logger.
 */
import type { Context } from 'cordis-demo-ts'

export const inject = ['logger']
export const name = 'greeter'

export function apply(ctx: Context) {
    const prefix = 'Hi'

    // 1) injected service — `logger` is guaranteed to exist here
    ctx.logger.info(`greeter mounted (prefix="${prefix}")`)

    // 2) event listener — removed when this fiber unloads
    ctx.on('greet', (who) => {
        ctx.logger.info(`${prefix}, ${who}!`)
    })

    // 3) reversible side effect — its disposer runs on unload
    ctx.effect(() => {
        ctx.logger.info('greeter effect registered')
        // Teardown uses console.log directly: by the time this runs, services
        // may already be revoked, so a service call is not safe here.
        return () => console.log('[greeter] effect disposed')
    })
}
