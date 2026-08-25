import type { Plugin, PluginContext } from '../types.js';

const metricsPlugin: Plugin = {
  name: 'metrics',
  register(ctx: PluginContext): void {
    ctx.logger.info('[metrics] registered');
  },
  execute(ctx: PluginContext): void {
    ctx.logger.info('[metrics] recording metrics...');
  },
  destroy(): void {
    // 模拟刷新缓冲区 & 清理资源
    console.log('[metrics] flushing buffer & cleanup');
  },
};

export default metricsPlugin;
