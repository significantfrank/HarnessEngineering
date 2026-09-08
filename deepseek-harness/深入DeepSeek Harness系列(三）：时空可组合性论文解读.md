[_A Programming Paradigm for Spatiotemporal Composability_](https://github.com/cordiverse/paper)  是和DeepSeek Harness一起发布的78页的学术论文。

论文给出了"**软件如何在时间与空间两个维度上正确组合**"的范式，Cordis是论文的参考实现，dsh 是基于Cordis 之上构建的一个"可被动态拆装、可热替换、可自我演化"的 AI Agent harness 产品。

简单来说，你可以认为，dsh是时空可组合性论文的工程实践，论文是dsh的理论升华。

# 1. 别被 "Spatiotemporal" 吓到
"Spatiotemporal" 是时空的意思，这玩意没那么高深，只是在学术论文里被拔高了，不要被唬到。需要注意的是，这里的时空**不是物理意义上的时空**，而是指动态软件系统中的两个核心问题维度：

+ **时间维（Temporal）**：一个组件在 t1 被加入系统、在 t2 被移除时，它之前造成的所有影响能不能正确撤销？这由 **Temporal composability（时间可组合性）** 解决。

+ **空间维（Spatial）**：在同一时刻，组件 A、B、C 之间存在怎样的依赖拓扑？当拓扑变化时系统怎么办？ 这由 **Spatial composability（空间可组合性）** 解决。

# 2. Revertible Effects（可逆副作用）
**Temporal composability（时间可组合性）** 的关键是 **Revertible Effects（可逆副作用）**

在函数式编程的语境下，Effect可以理解为Side Effect（副作用）的缩写，在编程中指的是：**一个函数或表达式在完成其主要任务（返回值）之外，还修改了外部环境的状态，或与外部世界发生了交互**。要理解函数的副作用，首先要对比纯函数（无副作用）和 副作用函数：

+ **纯函数**：给定相同的输入，永远返回相同的输出，并且**不**修改任何外部状态。例如 `function add(a,b){ return a + b }`。
+ **有副作用函数**：函数不仅返回了值，还偷偷干了别的事（改全局变量、发请求、写数据库等）。

所以，我们平时倡导的函数参数要尽量是不可变的（immutable），就是在尽量消除副作用，减少函数改变外部状态的机会。

那么在dhs的语境下，副作用主要是指插件的可撤销性。即你要在时序上满足**Revertible Effects（可逆副作用）**。通俗的讲，就是在组件（或者叫插件）卸载的时候，要撤销组件安装过程中造成的所有副作用。

试想一下，windows系统安装软件的过程，他要复制 DLL 文件、写入注册表（`HKEY_LOCAL_MACHINE`）、创建 Windows 服务、修改环境变量 `PATH`等等。而卸载，需要按逆序执行所有撤销操作。理想情况是可以完全撤销，没有残留。但用过windows的人都知道，这有多难！

dhs是如何解决这个难题的呢？

按照论文的思想是：**每产生一个 effect，同时产生它的 inverse / disposer（撤销函数——effect 执行时，同时返回的逆操作，卸载时反向调用）。**

一个组件做了 e1（注册 listener）、e2（开 server）、e3（建 timer），就同时积累 undo(e1)、undo(e2)、undo(e3)。卸载时按相反顺序执行 undo(e3)→undo(e2)→undo(e1)，像一个**自动生成的 undo stack**。

**这种可逆还要具备可组合性**。如果 Plugin A 内嵌 B 内嵌 C，每一级 effect 都带 disposer，那么 `dispose(A)` 就能递归推出整个子树的清理序列，开发者不需要手写 stopA/stopB/stopC/removeX/clearY。"整个组件可撤销"由"原子操作可撤销 + 组合规则"推导出来——这是论文想要的代数性质。

# 3. Reactive Coeffects（反应式余效应）
**Spatial composability（空间可组合性）** 的关键是 **Reactive Coeffects（反应式余效应）**

要理解“Reactive Coeffects”，首先要分清 `Effect` 和 `Coeffect` 这对概念：

+ `Effect`**（副作用）**：描述一个组件**对世界做了什么**。例如：修改了全局状态、注册了一个事件监听、写入了一个文件。
+ `Coeffect`**（余效应）**：描述一个组件**需要从世界获得什么**才能工作。例如：需要一个数据库连接、依赖某个配置项、订阅了某个事件源。

“Reactive”描述了这个机制的核心运作方式：**当系统环境（Context）发生变化时，组件能自动、被动地得到通知并做出响应**

**Coeffect 描述"我要运行，需要环境给我什么"**——以一个RAG插件为例：

+ effects：注册 /chat API、注册 message handler
+ coeffects：需要 LLM service、需要 vector database

关键不是"启动时检查一次 dependency"，而是**运行期间 dependency graph 一旦改变，组件生命周期自动发生变化**：

```plain
LLM ✓ + vectorDB ✓ → ACTIVE
vectorDB 消失 → INACTIVE（自动停用）
vectorDB 重新出现 → 重新 ACTIVE（自动激活）
```

如果说 `Effect` 关注的是**动作的执行与撤销**（时间上的可逆），那么 `Reactive Coeffects` 关注的就是**依赖的感知与响应**（空间上的有序）。两者结合，为构建能够**热插拔、自修复、动态演化**的复杂插件系统（如dhs）提供了理论基础。

它的工作方式可以概括为：

1. **声明**：每个组件明确声明它需要什么（它的 `Coeffect` 规范）。
2. **通知**：系统上下文（Context）发生任何变化（如服务上线/下线、配置变更）时，会自动**通知**所有受影响的组件。
3. **响应**：组件根据通知类型（激活/停用/中性）采取相应行动，例如重新建立连接、或安全地关闭自己。

这比传统 Dependency Injection 更强：DI 通常在 boot 时构造依赖图后就默认稳定；论文讨论的是**运行期依赖图持续变化**的场景，所以更接近 "Reactive Dependency Injection + Lifecycle Management"，只是作者把它提升到 coeffect 的理论框架里。

# 4. Context（上下文）的核心作用
`Context`是**时空可组合性（Spatiotemporal Composability）** 时空两个维度的关键。它既是`effect context`，也是`coeffect context`。论文将这两个上下文**统一为一个单一的 `Context` 类型 ，通过这一个概念来同时管理“时间”上的副作用撤销和“空间”上的依赖管理。

论文明确指出，将 `effect context` 与 `coeffect context` 统一为单一的 `context type`，**本身就构成了一种编程范式**。 这意味着，该范式下的编程活动，核心就是围绕对 `Context` 的操作、变换和管理来展开的。所以`Context`同时承担了四重角色：

```plain
 Context
 ├── 我现在能访问什么 → coeffects / dependencies
 ├── 我现在改变了什么 → effects
 ├── 这些东西以后怎么撤销 → inverses / disposers
 └── 我的生命周期属于谁 → parent / child context
```

总的来说，`Context` 在论文提出的时空可组合新编程范式中，超越了传统“环境”的角色，成为一个**主动的、被严格形式化的核心编程抽象**。它将时间和空间两个维度的复杂性内聚于一身，并通过“可逆效应”和“响应式余效应”两套机制，为构建能够动态、安全、灵活组合的复杂系统（如插件系统、自进化AI框架）提供了理论基础。

Cordis就是在这些理论知识上搭建的元框架，后续我们会深入解读Cordis原理及其源码。

如果觉得本文对你有帮助，请支持我的新书[《Harness工程实战》](https://item.jd.com/10233699155932.html)
