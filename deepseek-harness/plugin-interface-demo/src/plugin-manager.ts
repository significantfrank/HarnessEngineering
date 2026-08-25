import type { Plugin, PluginContext } from './types.js';

/**
 * 插件管理器：负责插件的动态加载、卸载与执行。
 * 宿主掌握完全控制权 —— 决定“何时加载、何时调用、何时卸载”。
 */
export class PluginManager {
  private plugins = new Map<string, Plugin>();
  private ctx: PluginContext;

  constructor(ctx: PluginContext) {
    this.ctx = ctx;
  }

  /** 动态加载并注册 */
  async load(specifier: string): Promise<void> {
    const mod = await import(specifier); // 动态导入
    const plugin: Plugin = mod.default ?? mod;
    if (this.plugins.has(plugin.name)) {
      throw new Error(`duplicate plugin: ${plugin.name}`);
    }
    plugin.register(this.ctx); // 生命周期：激活
    this.plugins.set(plugin.name, plugin);
    this.ctx.logger.info(`plugin loaded: ${plugin.name}`);
  }

  /** 卸载插件 */
  unload(name: string): void {
    const p = this.plugins.get(name);
    if (!p) {
      throw new Error(`plugin not found: ${name}`);
    }
    p.destroy?.(); // 生命周期：卸载
    this.plugins.delete(name);
    this.ctx.logger.info(`plugin unloaded: ${name}`);
  }

  /** 执行单个插件 */
  execute(name: string): void {
    const p = this.plugins.get(name);
    if (!p) {
      throw new Error(`plugin not found: ${name}`);
    }
    p.execute(this.ctx);
  }

  /** 执行所有已加载插件 */
  executeAll(): void {
    for (const p of this.plugins.values()) {
      p.execute(this.ctx);
    }
  }

  /** 是否已加载 */
  has(name: string): boolean {
    return this.plugins.has(name);
  }

  /** 已加载插件名列表 */
  list(): string[] {
    return [...this.plugins.keys()];
  }

  /** 卸载所有插件（用于优雅关闭） */
  unloadAll(): void {
    for (const name of [...this.plugins.keys()]) {
      this.unload(name);
    }
  }
}
