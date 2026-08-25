import type { HookBus, HookPlugin, PluginContext, PluginContextInit } from './types.js';

/**
 * 插件管理器：动态加载插件 → apply(ctx) 注册回调；
 * 卸载时调用 destroy()，由插件自行取消订阅。
 *
 * 内部持有 HookBus（可 emit），ctx.hooks 只暴露 HookRegistry（只能 on）。
 */
export class PluginManager {
  private plugins = new Map<string, HookPlugin>();
  private bus: HookBus;
  private ctx: PluginContext;

  constructor(bus: HookBus, init: PluginContextInit) {
    this.bus = bus;
    // 把 bus 作为 HookRegistry 注入 ctx（只暴露 on）
    this.ctx = { ...init, hooks: bus };
  }

  /** 动态加载并注册 */
  async load(specifier: string): Promise<void> {
    const mod = await import(specifier);
    const plugin: HookPlugin = mod.default ?? mod;
    if (this.plugins.has(plugin.name)) {
      throw new Error(`duplicate plugin: ${plugin.name}`);
    }
    plugin.apply(this.ctx); // 只传 ctx，hooks 已内嵌
    this.plugins.set(plugin.name, plugin);
    this.ctx.logger.info(`plugin loaded: ${plugin.name}`);
  }

  /** 卸载插件 */
  unload(name: string): void {
    const p = this.plugins.get(name);
    if (!p) {
      throw new Error(`plugin not found: ${name}`);
    }
    p.destroy?.(); // 生命周期：卸载（插件在此取消订阅）
    this.plugins.delete(name);
    this.ctx.logger.info(`plugin unloaded: ${name}`);
  }

  /** 是否已加载 */
  has(name: string): boolean {
    return this.plugins.has(name);
  }

  /** 已加载插件名列表 */
  list(): string[] {
    return [...this.plugins.keys()];
  }

  /** 卸载所有插件 */
  unloadAll(): void {
    for (const name of [...this.plugins.keys()]) {
      this.unload(name);
    }
  }
}
