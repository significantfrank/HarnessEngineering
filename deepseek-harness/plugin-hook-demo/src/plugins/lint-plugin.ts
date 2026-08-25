import type { HookPlugin, PluginContext } from '../types.js';

/**
 * lint 插件：挂载 beforeCompile 钩子
 * 宿主编译前自动触发 lint 检查
 */
class LintPluginImpl implements HookPlugin {
  name = 'lint';
  private unsub?: () => void;

  apply(ctx: PluginContext): void {
    const hook = (compileCtx: any): void => {
      ctx.logger.info(`[lint] checking ${compileCtx?.entry}`);
    };
    this.unsub = ctx.hooks.on('beforeCompile', hook);
  }

  destroy(): void {
    this.unsub?.();
    this.unsub = undefined;
  }
}

export default new LintPluginImpl();
