> **摘要**：本文从软件架构角度解读 Plugin 插件机制。聚焦两种核心实现范式——**接口驱动（拉模式）** 与 **事件驱动（推模式）**，为后续解读dsh的“Everything is Plugin”设计做铺垫。

DeepSeek Harness（后面用 dsh 简称）的口号是 **Everything is Plugin**，在深入解读 dsh 之前，我们有必要花一些篇幅了解一下软件设计中的 Plugin 架构模式，以及常见的实现方式。

# 什么是Plugin
**插件机制** 是一种软件架构模式，它允许在不修改宿主程序核心代码的前提下，通过外部的、可插拔的模块来扩展或定制应用程序的功能。

**插件机制可以带来以下好处**

+ **开闭原则**：对扩展开放，对修改关闭——加新功能不碰核心代码。
+ **解耦**：核心只依赖抽象接口，不依赖具体实现。
+ **生态**：第三方可以独立开发、分发插件（如 VS Code 扩展、Eclipse 插件、Maven 插件）。
+ **按需装配**：不同部署只装不同插件，减小体积。

鉴于这些好处，plugin一直是软件设计中重要的架构模式。为了OCP （**Open to extend Close to change**） ，无数人前赴后继，尝试用Plugin打造软件设计的乌托邦。虽然有VSCode和Chrome这样的成功典范，但也有OSGi和阿里中台这样的失败案例。

究其原因，还是要看产品形态和业务场景。但凡是PC软件，可控性高、领域垂直，比如IDE，浏览器等，能成功的可能性比较大。因为其扩展点有限，且可预测。但凡期望用Plugin一统天下，漫无边际，扩展点不可预期，失败的可能性就比较大。

Agent 比较特殊，它既有 PC 软件的特点——我们常用的 Agent 都是 PC 软件，都具有 Agent Loop、Function Calling、Session 管理、工具注册等特定功能——但如果和不同行业结合，其不可预测性显著增加。使用 Plugin 扩展机制对 Agent 产品来说，总体是合宜的。这也是为什么市面上 Agent 产品（OpenCode、Cursor、Codex 等）都标配 Plugin 能力的原因。然而像 dsh 这样把 Plugin 做到极致、一切皆插件，dsh 是首创。是噱头，还是颠覆，现在还不好说，先让子弹飞一会。

本文，我们还是聚焦技术本身，plugin作为重要的架构模式，值得每个技术人员好好掌握。

实际上，所有的插件其本质都是一种**扩展设计**。插件扩展在软件设计中，主要有两种实现方式：**接口驱动（拉模式）** 和 **事件/钩子驱动（推模式）**。

# Plugin的实现方式
拉模式和推模式是实现Plugin的两种主要范式：

+ **接口驱动（拉模式）**：宿主程序掌握完全的控制权。宿主在需要时（如启动时、菜单点击时），**主动拉取**所有实现了指定接口的插件实例，并调用其方法（如 `plugin.execute()`）。
+ **事件驱动（推模式）**：插件掌握部分主动权。插件在启动时**主动向**宿主的“扩展点”注册回调，之后宿主在特定时机（如提交commit、发送prompt）**主动推送**事件给所有注册的插件。宿主发送事件，回调函数响应事件。
  ![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/f6df2a3a26e74d1e9d5d2dc0443ff195.png)


**两种模式的对比如下：**

| 维度 | 接口驱动 | 事件驱动 |
| --- | --- | --- |
| 方向 | 拉（Pull） | 推（Push） |
| 耦合度 | 强耦合 | 松耦合 |
| 主导方 | 宿主 | 插件 |
| 扩展性 | 需修改接口 | 即插即用 |
| 典型场景 | OSGi、SPI | 事件总线、观察者模式 |


在后续 dsh 的插件实现解析文章中，你会看到 dsh 这两种模式都有用到。在此之前，先让我们看下如何用代码实现这两种插件模式。

## 接口驱动
接口驱动，即通常说的面向接口编程，也叫策略模式。例如，**Java 的 SPI（Service Provider Interface）**、**Eclipse 插件体系**、**IDEA 插件**。插件必须完整实现宿主定义的 `Action` 或 `ExtensionPoint` 接口。

接下来，我们用TypeScript演示如何实现一个基于接口的插件框架

首先，需要定义接口契约：

```typescript
export interface PluginContext {
  logger: { info(msg: string): void };
  config: Record<string, unknown>;
}

export interface Plugin {
  name: string;
  register(ctx: PluginContext): void;
  execute(ctx: PluginContext): void;
  destroy?(): void;
}
```

其次，需要管理插件，包括加载、卸载：

```typescript
class PluginManager {
  private plugins = new Map<string, Plugin>();
  private ctx: PluginContext;

  constructor(ctx: PluginContext) {
    this.ctx = ctx;
  }

  /** 动态加载并注册 */
  async load(specifier: string): Promise<void> {
    const mod = await import(specifier);          // 动态导入
    const plugin: Plugin = mod.default ?? mod;
    if (this.plugins.has(plugin.name)) {
      throw new Error(`duplicate plugin: ${plugin.name}`);
    }
    plugin.register(this.ctx);                          // 生命周期：激活
    this.plugins.set(plugin.name, plugin);
    console.log(`plugin loaded: ${plugin.name}`);
  }

  unload(name: string): void {
    const p = this.plugins.get(name);
    p?.destroy?.();                                 // 生命周期：卸载
    this.plugins.delete(name);
  }
}
```

有了上面的框架之后，我们就可以利用Plugin定义的接口进行扩展实现了，比如：

```typescript
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
```

这里需要注意的是，**TypeScript 使用的是鸭子类型**，即并不需要显式地 `implement Plugin` 接口，只要其对象定义中有接口中定义的属性（name、register、execute，以及可选的 destroy 方法），它就是间接实现了 `Plugin` 接口。

最后，是对插件的使用，以及生命周期的管理。

```typescript
// 1. 构建插件上下文（注入 logger 与 config）
const ctx: PluginContext = {
    logger: {
        info(msg: string) {
          console.log(`[info] ${msg}`);
    },
      },
    config: {
    greet: { punctuation: '!!!' },
      },
};

async function main(): Promise<void> {
    const manager = new PluginManager(ctx);

    // 2. 动态加载插件（按需装配，互不耦合）
    await manager.load('./plugins/greet-plugin.js');
    await manager.load('./plugins/metrics-plugin.js');
    
    console.log('--- loaded plugins ---');
    console.log(manager.list());
    
    // 3. 执行所有已加载插件（宿主主动拉取）
      console.log('--- executeAll ---');
    manager.executeAll();
    
    // 4. 执行单个插件
      console.log('--- execute(greet) ---');
    manager.execute('greet');
    
    // 5. 卸载单个插件（触发 destroy）
      console.log('--- unload metrics ---');
    manager.unload('metrics');
    
    // 6. 卸载剩余插件（优雅关闭）
  console.log('--- unloadAll ---');
  manager.unloadAll();
}
```

完整代码，请在[plugin-interface-demo](https://github.com/significantfrank/HarnessEngineering/tree/main/deepseek-harness/plugin-interface-demo)查看。

## 事件驱动
如果把接口驱动理解成策略设计模式，那么事件驱动可以理解为观察者设计模式。

所谓的Plugin，就是观察者设计模式中的Observer/Listener，当特定事件（或者叫hook钩子）被触发时，会发送事件（emit event），然后触发Plugin执行。典型的实现有，**Git Hook**（pre-commit）、**VS Code 插件**（注册 `onCommand` 和 `onDidSaveTextDocument` 等事件）、以及Cordis中基于事件的插件。

接下来，我们用TypeScript实现一个简单的基于事件的hooks plugin demo。

首先，是类型定义，主要包括回调函数，插件注册：

```typescript
/** 钩子回调函数 */
export type HookFn = (...args: any[]) => void | Promise<void>;

export interface HookRegistry {
  /** 扩展点：插件注册回调 */
  on(event: string, fn: HookFn): () => void;
}

/** 插件上下文：logger + config + hooks（只能订阅） */
export interface PluginContext {
    logger: { info(msg: string): void };
    config: Record<string, unknown>;
    /** 钩子注册器：插件只能 on，不能 emit */
    hooks: HookRegistry;
}

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
```

这里有个问题：为了回调函数 HookFn 的通用性，我们这里使用了 any，这就意味着我们会丢失类型检查。这个问题我们先放在这，在后续分析 dsh 插件文章的时候，我们看看它是如何解决这个类型问题的。

同样，我们需要加载插件：

```typescript
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
```

最后，当宿主需要在扩展点调用插件时，会通过emit广播事件，然后注册该事件的hooks都会被调用。

```typescript
  /** 宿主在恰当时机触发，按注册顺序串行 await */
    async emit(event: string, ...args: any[]): Promise<void> {
        for (const fn of this.hooks.get(event) ?? []) {
            await fn(...args);
        }
    }
```

完整代码，请在[plugin-hook-demo](https://github.com/significantfrank/HarnessEngineering/tree/main/deepseek-harness/plugin-hook-demo) 查看。

如果觉得本文对你有帮助，请支持我的新书[《Harness工程实战》](https://item.jd.com/10233699155932.html)