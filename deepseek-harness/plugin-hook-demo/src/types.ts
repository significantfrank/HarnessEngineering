// 事件驱动插件 —— 推模式（观察者模式）
// 宿主定义扩展点（emit 时机），插件自主决定挂哪些回调

/** 钩子回调函数 */
export type HookFn = (...args: any[]) => void | Promise<void>;

/**
 * 插件可见的注册器（最小权限）
 * 插件只能 on（订阅 + 取消订阅），不能 emit / off / listenerCount
 */
export interface HookRegistry {
  /** 扩展点：插件注册回调，返回取消订阅函数 */
  on(event: string, fn: HookFn): () => void;
}

/**
 * 宿主内部接口：在 HookRegistry 基础上增加触发与管理能力
 * 插件拿不到这个接口，无法自行 emit 或取消别人的订阅
 */
export interface HookBus extends HookRegistry {
  /** 取消订阅 */
  off(event: string, fn: HookFn): void;
  /** 宿主在恰当时机触发，按注册顺序串行 await */
  emit(event: string, ...args: unknown[]): Promise<void>;
  /** 当前事件已注册的回调数量 */
  listenerCount(event: string): number;
}

/** 插件上下文：logger + config + hooks（只能订阅） */
export interface PluginContext {
  logger: { info(msg: string): void };
  config: Record<string, unknown>;
  /** 钩子注册器：插件只能 on，不能 emit */
  hooks: HookRegistry;
}

/** 宿主构建上下文时传入（不含 hooks，由 PluginManager 内部注入） */
export type PluginContextInit = Omit<PluginContext, 'hooks'>;

/**
 * 事件驱动插件：通过 apply(ctx) 挂载回调
 * ctx.hooks 是 HookRegistry —— 插件无法触发事件
 */
export interface HookPlugin {
  name: string;
  /** 注册阶段：通过 ctx.hooks.on() 挂载回调，可读 config / 初始化资源 */
  apply(ctx: PluginContext): void;
  /** 卸载（可选）：释放资源 */
  destroy?(): void;
}
