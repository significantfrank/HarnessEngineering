import type { HookPlugin, PluginContext } from '../types.js';

/**
 * metrics 插件：同时挂载 beforeCompile 和 afterCompile 两个钩子
 * 演示一个插件订阅多个扩展点
 */
class MetricsPluginImpl implements HookPlugin {
  name = 'metrics';
  private unsubs: Array<() => void> = [];

  apply(ctx: PluginContext): void {
    const start = Date.now();

    this.unsubs.push(
      ctx.hooks.on('beforeCompile', (compileCtx: unknown) => {
        const { entry } = compileCtx as { entry: string };
        ctx.logger.info(`[metrics] compile start → ${entry}`);
      }),
    );

    this.unsubs.push(
      ctx.hooks.on('afterCompile', (result: unknown) => {
        const { output, errors } = result as { output: string; errors: number };
        ctx.logger.info(`[metrics] compile done: ${output} | errors=${errors} | ${Date.now() - start}ms`);
      }),
    );
  }

  destroy(): void {
    for (const unsub of this.unsubs) unsub();
    this.unsubs = [];
  }
}

export default new MetricsPluginImpl();
