// 接口契约 —— 所有插件必须遵守的约定（拉模式：宿主主动调用）
export interface PluginContext {
  logger: { info(msg: string): void };
  config: Record<string, unknown>;
}

export interface Plugin {
  /** 唯一标识，用于加载/卸载/执行时的寻址 */
  name: string;
  /** 生命周期：注册激活（可读 config、初始化资源） */
  register(ctx: PluginContext): void;
  /** 核心逻辑：宿主在需要时主动拉取并调用 */
  execute(ctx: PluginContext): void;
  /** 生命周期：卸载（可选，释放资源） */
  destroy?(): void;
}
