# cordis-demo

[Cordis](https://deepseek-harness.github.io/deepseek-harness/develop/cordis-tutorial/) 插件框架核心机制的从零实现 demo，用约 450 行 TypeScript 复现了 Cordis 的**反应式依赖注入**与**可逆副作用**两大核心理念。

## 五个核心思想

1. **插件即函数 / 类 / `{ apply }` 对象**（或导出 `apply` 的模块），挂载到 Context 中
2. **Context 是服务仓库**，服务通过稳定的 `ctx.<name>` 暴露，按 key 查找而非 import
3. **`inject` 声明依赖**，插件仅在所有必需服务就绪后才加载——加载顺序由依赖驱动
4. **事件通信**（`ctx.on` / `ctx.emit`）让插件之间解耦协作
5. **注册即可逆**（`ctx.effect`），`ctx.dispose()` 按注册的逆序展开所有副作用

## 结构

```
├── index.ts                  # 端到端演示入口
└── src/
    ├── cordis.ts              # 框架核心：Context / Fiber / Service / DisposableList
    └── plugins/
        ├── logger.ts          # Service 插件：贡献 ctx.logger（声明合并）
        ├── timer.ts           # Service 插件：贡献 ctx.timer（可逆 interval/timeout）
        └── greeter.ts         # 模块插件：inject logger + 事件监听 + 可逆 effect
```

## 运行

```bash
cd cordis-demo
pnpm install          # 首次：安装 typescript（仅用于类型检查）
pnpm start            # node --experimental-strip-types index.ts
```

> 本 demo 依赖 Node 22+ 的 `--experimental-strip-types` 直接执行 `.ts` 文件，无需编译。

## 类型检查

```bash
pnpm typecheck        # tsc --noEmit
```

## 预期输出

```
[demo] greeter state right after mount: pending
[info] greeter mounted (prefix="Hi")
[info] greeter effect registered
[demo] greeter state after logger mounted: active
[info] Hi, Cordis!
[info] timer tick 1
[info] timer tick 2
[info] timer tick 3
[demo] disposing context…
[greeter] effect disposed
[demo] greeter state after dispose: disposed
[demo] timer state after dispose: disposed
[demo] done — all effects unwound in reverse order
```

## 演示了什么

| 步骤 | 行为 | 体现的机制 |
|------|------|-----------|
| 1 | 先挂载 greeter，此时 logger 尚不存在 → `pending` | `inject` 声明依赖，驱动加载顺序 |
| 2 | 注册 LoggerService → greeter 被唤醒 → `active` | 反应式依赖注入：服务注册即触发重评估 |
| 3 | 注册 TimerService | Service 基类 + 声明合并 |
| 4 | `ctx.emit('greet', 'Cordis')` → greeter 监听器触发 | 事件系统 `ctx.on` / `ctx.emit` |
| 5 | timer tick 3 次后 `ctx.dispose()` | 可逆副作用：按逆序展开所有 effect |

## 如何新增插件

1. 在 `src/plugins/` 下新建 `my-plugin.ts`：

   ```ts
   import type { Context } from 'cordis-demo-ts'

   export const inject = ['logger']
   export const name = 'my-plugin'

   export function apply(ctx: Context) {
     ctx.logger.info('my-plugin mounted')
     ctx.effect(() => {
       return () => console.log('my-plugin disposed')
     })
   }
   ```

2. 在 `index.ts` 中 `import * as myPlugin from './src/plugins/my-plugin.ts'`，然后 `ctx.plugin(myPlugin)` 即可。

> **声明合并**：如果插件贡献新的 `ctx.xxx` 服务，需在文件内 `declare module 'cordis-demo-ts' { interface Context { xxx: MyService } }` 让 TypeScript 识别该属性。
