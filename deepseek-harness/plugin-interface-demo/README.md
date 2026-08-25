# plugin-interface-demo

基于**接口契约**的插件 demo（拉模式）。宿主定义 `Plugin` 接口，通过 `PluginManager` 动态加载/卸载插件，并在需要时**主动调用** `plugin.execute()`。

> 接口驱动 = 策略模式：宿主掌握完全控制权，插件是被动的"工具人"。

## 结构

```
src/
├── types.ts              # 接口契约 PluginContext / Plugin
├── plugin-manager.ts     # PluginManager：load / unload / execute / executeAll
├── main.ts               # 使用演示
└── plugins/
    ├── greet-plugin.ts   # 示例插件：读 config 定制输出
    └── metrics-plugin.ts # 示例插件：演示 destroy 资源清理
```

## 运行

```bash
cd plugin-interface-demo
pnpm install     # 首次：生成 node_modules
pnpm start       # tsc 编译 + 运行 dist/main.js
```

## 预期输出

```
[info] [greet] registered
[info] plugin loaded: greet
[info] [metrics] registered
[info] plugin loaded: metrics
--- loaded plugins ---
[ 'greet', 'metrics' ]
--- executeAll ---
[info] [greet] Hello from the greet plugin!!!
[info] [metrics] recording metrics...
--- execute(greet) ---
[info] [greet] Hello from the greet plugin!!!
--- unload metrics ---
[metrics] flushing buffer & cleanup
[info] plugin unloaded: metrics
--- unloadAll ---
[info] plugin unloaded: greet
```

## 插件生命周期

```
load(specifier)
  └─ import() → mod.default
     └─ plugin.register(ctx)   ← 激活：读 config / 初始化资源
     └─ plugins.set(name)

execute(name) / executeAll()
  └─ plugin.execute(ctx)       ← 宿主主动拉取调用

unload(name) / unloadAll()
  └─ plugin.destroy?.()       ← 卸载：释放资源
  └─ plugins.delete(name)
```

## 如何新增插件

1. 在 `src/plugins/` 下新建 `my-plugin.ts`，实现 `Plugin` 接口：
   ```ts
   import type { Plugin, PluginContext } from '../types.js';
   const myPlugin: Plugin = {
     name: 'my',
     register(ctx) { ctx.logger.info('[my] hi'); },
     execute(ctx) { ctx.logger.info('[my] run'); },
   };
   export default myPlugin;
   ```
2. 在 `main.ts` 中 `await manager.load('./plugins/my-plugin.js')` 即可。

> 注意：NodeNext ESM 下，动态 import 的 specifier 须写 **`.js`** 后缀（指向编译产物），即使源文件是 `.ts`。
