# plugin-hook-demo

基于**事件钩子**的插件 demo（推模式 / 观察者模式）。宿主定义扩展点（`emit` 时机），插件通过 `apply(ctx)` 自主挂载回调。

## 结构

```
src/
├── types.ts              # 接口契约 HookRegistry / HookBus / HookPlugin / PluginContext
├── hook-bus.ts           # HookBusImpl：on / off / emit / listenerCount（实现 HookBus）
├── plugin-manager.ts     # PluginManager：load → apply(ctx) / unload → destroy()
├── main.ts               # 使用演示（模拟编译流程）
└── plugins/
    ├── lint-plugin.ts    # 挂 beforeCompile 钩子，读 config 决定 strict
    └── metrics-plugin.ts # 挂 beforeCompile + afterCompile 两个钩子
```

### 接口分层

```
PluginContext (插件可见)           HookBus (宿主内部)
├── logger                        ├── on  (继承自 HookRegistry)
├── config                        ├── off(event, fn)
└── hooks: HookRegistry           ├── emit(event, ...args)
    └── on(event, fn): unsub      └── listenerCount(event)
```

`PluginManager` 内部持有 `HookBus`，但把它作为 `HookRegistry` 注入 `ctx.hooks` —— 插件只能订阅，不能触发。

## 运行

```bash
cd plugin-hook-demo
pnpm install     # 首次：生成 node_modules
pnpm start       # tsc 编译 + 运行 dist/main.js
```

## 预期输出

```
[info] plugin loaded: lint
[info] plugin loaded: metrics
--- loaded plugins ---
[ 'lint', 'metrics' ]
--- listenerCount ---
beforeCompile: 2
afterCompile: 1
--- emit beforeCompile ---
[info] [lint] checking ./src/index.ts (strict)
[info] [metrics] compile start → ./src/index.ts
--- (host compiles...) ---
--- emit afterCompile ---
[info] [metrics] compile done: dist/bundle.js | errors=0 | 3ms
--- unload metrics ---
[info] plugin unloaded: metrics
afterCompile listeners after unload: 0
--- unloadAll ---
[info] plugin unloaded: lint
beforeCompile listeners after unloadAll: 0
```

## 插件生命周期

```
load(specifier)
  └─ import() → mod.default
     └─ plugin.apply(ctx)          ← 挂载回调：ctx.hooks.on(event, fn)
     └─ plugins.set(name)

emit(event, ...args)
  └─ for fn of hooks[event]        ← 宿主在恰当时机推送，串行 await
     └─ await fn(...args)

unload(name) / unloadAll()
  └─ plugin.destroy()             ← 插件自行取消订阅（调用 on() 返回的 unsub）
  └─ plugins.delete(name)
```

## 如何新增插件

1. 在 `src/plugins/` 下新建 `banner-plugin.ts`：
   ```ts
   import type { HookPlugin, PluginContext } from '../types.js';

   class BannerPluginImpl implements HookPlugin {
     name = 'banner';
     private unsub?: () => void;
     apply(ctx: PluginContext) {
       this.unsub = ctx.hooks.on('afterCompile', (result) => {
         ctx.logger.info('[banner] header injected');
       });
     }
     destroy() { this.unsub?.(); }
   }
   export default new BannerPluginImpl();
   ```
2. 在 `main.ts` 中 `await manager.load('./plugins/banner-plugin.js')` 即可。

> 注意：NodeNext ESM 下，动态 import 的 specifier 须写 **`.js`** 后缀（指向编译产物），即使源文件是 `.ts`。
