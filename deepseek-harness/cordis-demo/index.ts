/**
 * cordis-demo (TypeScript) — a minimal end-to-end run of the Cordis plugin core.
 *
 *   node --experimental-strip-types index.ts
 *
 * It shows, in order:
 *   1. mount a module plugin that injects a service BEFORE that service exists
 *      — it stays PENDING until the service is provided (inject-based load order)
 *   2. a Service plugin contributing `ctx.logger` and `ctx.timer`
 *   3. a module plugin using an injected service, an event, and an effect
 *   4. dispatching an event
 *   5. a reversible effect (a timer) and clean teardown on `ctx.dispose()`
 */
import { Context } from 'cordis-demo-ts'
import { LoggerService } from './src/plugins/logger.ts'
import { TimerService } from './src/plugins/timer.ts'
import * as greeter from './src/plugins/greeter.ts'

const ctx = new Context()

// 1) greeter injects `logger` but the logger isn't loaded yet → it stays
//    PENDING. The dependency declaration, not the call order, drives loading.
const greeterFiber = ctx.plugin(greeter)
console.log(`[demo] greeter state right after mount: ${greeterFiber.state}`)

// 2) Provide the logger. Registering the service wakes the pending greeter.
const loggerFiber = ctx.plugin(LoggerService)
console.log(`[demo] greeter state after logger mounted: ${greeterFiber.state}`)

// 3) Provide the timer service.
const timerFiber = ctx.plugin(TimerService)

// 4) Dispatch an event — the greeter's listener (registered via ctx.on) fires.
ctx.emit('greet', 'Cordis')

// 5) A reversible effect: a timer that ticks a few times, then tears it all down.
let ticks = 0
ctx.timer.interval(() => {
    ticks += 1
    ctx.logger.info(`timer tick ${ticks}`)
    if (ticks >= 3) {
        console.log('[demo] disposing context…')
        ctx.dispose().then(() => {
            console.log(`[demo] greeter state after dispose: ${greeterFiber.state}`)
            console.log(`[demo] timer state after dispose: ${timerFiber.state}`)
            console.log('[demo] done — all effects unwound in reverse order')
        })
    }
}, 50)
