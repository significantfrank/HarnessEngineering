import type { Plugin, PluginContext } from '../types.js';

interface GreetConfig {
  punctuation?: string;
}

const greetPlugin: Plugin = {
  name: 'greet',
  register(ctx: PluginContext): void {
    ctx.logger.info('[greet] registered');
  },
  execute(ctx: PluginContext): void {
    const cfg = ctx.config.greet as GreetConfig | undefined;
    const mark = cfg?.punctuation ?? '!';
    ctx.logger.info(`[greet] Hello from the greet plugin${mark}`);
  },
  destroy(): void {
    // greet 无外部资源，空实现即可
  },
};

export default greetPlugin;
