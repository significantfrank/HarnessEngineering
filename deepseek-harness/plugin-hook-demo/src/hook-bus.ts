import type { HookFn, HookBus } from './types.js';

/** 钩子总线实现（宿主内部，对插件只暴露 HookRegistry 子集） */
export class HookBusImpl implements HookBus {
  private hooks = new Map<string, HookFn[]>();

  /** 扩展点：插件注册回调，返回取消订阅函数 */
  on(event: string, fn: HookFn): () => void {
    const list = this.hooks.get(event) ?? [];
    list.push(fn);
    this.hooks.set(event, list);
    return () => this.off(event, fn);
  }

  /** 取消订阅 */
  off(event: string, fn: HookFn): void {
    const list = this.hooks.get(event);
    if (!list) return;
    const i = list.indexOf(fn);
    if (i >= 0) list.splice(i, 1);
    if (list.length === 0) this.hooks.delete(event);
  }

  /** 宿主在恰当时机触发，按注册顺序串行 await */
  async emit(event: string, ...args: any[]): Promise<void> {
    for (const fn of this.hooks.get(event) ?? []) {
      await fn(...args);
    }
  }

  /** 当前事件已注册的回调数量 */
  listenerCount(event: string): number {
    return this.hooks.get(event)?.length ?? 0;
  }
}
