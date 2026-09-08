> **摘要**：文章从 Profile、Bundle、Patch、Plugin 四个核心概念入手，通过运行 dsh、安装自定义插件等实操步骤，逐步揭示 dsh 如何以“空根 + 补丁叠加”的方式动态组装 Agent，帮助读者理解一切皆插件背后的实现原理。

# DSH概念模型
要理解DSH一切皆插件的工作原理，我们必须要先理解下面几个概念：

+ **Profile**：代表一个具体的 Agent。它由一系列 Bundle（包括内置 Bundle 与客户定制 Bundle）通过层叠/叠加组合而成，决定了该 Agent 最终具备的具体能力与配置集合。
+ **Bundle**：本质上是一个 npm 包。它以 `package.json` 为配置文件，用于声明和配置该包所包含的插件（Plugin）、客户端模块及相关依赖项，是模块化分发与复用的基本载体。
+ **Patch**：Plugin 配置的具体物理载体，通常声明在 `cordis.patch.yml` 文件中。一个 Patch 可包含一到多个 Plugin 的具体配置项，并通过唯一的 `id` 进行索引与映射。
+ **Plugin**：系统的底层核心机制（“一切皆插件”）。它是真正承载具体功能的组件，既包含 Agent 的内置基础能力插件，也包含客户针对业务需求自定义扩展的功能插件。

其核心概念模型如下图所示：  
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/d3db9b200d604a89b081055d079a61ab.png)
这样看起来，这些概念还是太抽象。别着急，接下去我们通过运行dsh，安装插件，通过具体的文件配置讲解这套体系是如何运作的。具象感知是了解抽象概念最好的方式。
# 运行dsh，了解Profile、Bundle和Patch
首先，需要安装dsh，可通过命令`npm i -g @deepseek-ai/dsh`进行全局安装。遇到问题可以查看[Deepseek Harness官网](https://www.deepseek.com/harness/)

安装完之后，可运行`dsh --profile web` 启动dsh的web profile模式。一个profile是一个目录结构，web profile的目录，默认是在`$USER_HOME/.dsh/profiles/web/` ，以我自己的Mac电脑为例，
其Profile所在位置为`~/.dsh/profiles/web/`。![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/a3550b8a6dbe452aabd479e8fc49563b.png)
如上图所示，它主要包含以下内容：
```plain
~/.dsh/profiles/web/
├── package.json         ← 声明 dsh.profile.bundles + dependencies
├── cordis.yml           ← 空根 []（组合的起点，不手改）
├── cordis.patch.yml     ← 用户自己的补丁层（你改这个）
├── node_modules/        ← dependencies bundle 装在这里
├── pnpm-workspace.yaml  ← 让 profile 当独立 pnpm 项目管 out-of-tree 插件
└── pnpm-lock.yaml       ← 锁文件
```

这里最关键的是`package.json` 文件，里面包含了该profile应该包含的bundles，查看其内容：
```json
{
  "dsh": {
    "profile": {
      "bundles": [                          
        "@deepseek-ai/dsh-base",
        "@deepseek-ai/dsh-web-app",
      ]
    }
  }
}
```

可以看到web profile主要是有`dsh-base` 和 `dsh-web-app`这两个内置bundle组成。他们的作用分别是：

+ `@deepseek-ai/dsh-base`：每个 profile 的第一层。模型适配器、工具注册表、Agent Loop，会话日志、持久化、沙箱与审批策略、settings、credentials、telemetry——共享核心。
+ `@deepseek-ai/dsh-web-app`：在 base 上加浏览器应用（web GUI）。

查看`dsh-base/cordis.patch.yml` 源码文件，你可以看到下面的内容：
```yaml
# The dsh-base bundle patch: the shared core of every dsh profile, applied as
# ONE insert over the empty profile root. Later bundle patches and the user's
# profile cordis.patch.yml address these rows by id, with the last write
# winning per row.

- insert:
    - id: timer
      name: '@deepseek-ai/cordis-plugin-timer'
    - id: hmr
      name: '@deepseek-ai/cordis-plugin-hmr'
      config:
        root: ['.']
    - id: llm
      name: '@deepseek-ai/dsh-llm'
    - id: session
      name: '@deepseek-ai/dsh-session'
    - id: tools
      name: '@deepseek-ai/dsh-tools'
      
# 省略其它73个内置插件配置内容
```

通过注释，我们可以看到，`dsh-base` patch是一切的基础，它是通过id进行插件管理，后续的配置可以通过id覆写之前的配置，从而起到置换的作用。

由此可见，所谓的profile代表的是一种特定功能的Agent，比如**web profile**就是拥有web GUI能力的Agent。**headless profile**代表无头，只提供API功能的agent，类似于**serve**模式。

其存在方式是 `~/.dsh/profiles/<name>/` 下的一个目录，这个目录规定了Agent的组成，包括"用哪些 bundle、按什么顺序、外加哪些自己的补丁"的组合声明。`dsh --profile web --dump-config` 能把最终组合出的树打印出来，任何一行都能被你自己的 patch 替换。

若要创建一个全新的Agent，也就是一个新的profile，我们可以用 `dsh plugin --profile <name> add <package>` 命令创建，默认从 `@deepseek-ai/dsh-base` 起步。

这里，我们可以看到dsh的野心，这是一个能在运行时动态创造Agent的Agent，在Agent自进化赛道如火如荼的今天，这无疑给自进化带来了更大的想象空间。

# 安装Plugin，理解Plugin运作机制
接下来，我们以[dsh-at-file](https://deepseekplugins.com/plugins/dsh-at-file) 为例，如下图所示，安装完这个插件，我们就可以用@的形式进行文件选择。
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/1f2cdb2b43cf4099898e769fff694b30.png)
为此，我们先需要安装插件：
```shell
npx -y @deepseek-ai/dsh plugin --profile web add github:omdsh-dev/dsh-at-file
```

如果dsh是被全局安装的（`npm i -g @deepseek-ai/dsh`），也可以直接用下面的命令：
```shell
dsh plugin --profile web add github:omdsh-dev/dsh-at-file
```

这个命令的意思是为web profile安装`dsh-at-file` 插件，安装完成后，我们再查看`$USER_HOME/.dsh/profiles/web/package.json`文件，会发现其内容将变为：
```json
{
  "dependencies": {                        // ← 管"装"：out-of-tree 插件从哪来
    "dsh-at-file": "github:omdsh-dev/dsh-at-file"  // dsh-at-file插件新增
  },
  "dsh": {
    "profile": {
      "bundles": [                          // ← 管"用+顺序"：堆叠哪些 bundle
        "@deepseek-ai/dsh-base",
        "@deepseek-ai/dsh-web-app",
        "dsh-at-file"                // dsh-at-file插件新增
      ]
    }
  }
}
```

主要变化体现在`dependencies` 和 `dsh.profile.bundles` 中分别多出了`dsh-at-file`内容。新增的`dsh-at-file` bundle，通过pnpm安装后，会被下载到web profile的node_modules中。其包的位置在`$USER_HOME/.dsh/profiles/web/node_modules/dsh-at-file` ，查看其`cordis.patch.yml` 内容，可以看到，它配置的插件内容：

```yaml
- insert:
    - id: dsh-at-file
      name: dsh-at-file
```

`insert`表示是要新增插件。这样在运行时，dsh的插件Loader可以通过import `dsh-at-file` bundle代码来安装插件。因为`dsh-at-file` 同时也是插件导出的module名称。

这就是dsh安装使用用户自定义的plugin的全过程。

总而言之，**profile** 代表一种Agent，他是由一些列bundles叠加而成（内置的+客户定制的）；**bundle** 是一个npm包，通过`package.json` 进行插件、客户端某块等配置；**patch** 是plugin配置的载体，一般放在 `cordis.patch.yml` 文件中，可以包含1到多个plugin配置，通过id唯一索引；**plugin** 是真正的功能载体，一切皆插件，包括内置的基本agent能力插件，也包括客户自定义的功能扩展插件。

# 一切皆插件
dsh中**一切皆插件** 不是口号，而是产品的DNA，还是以web profile为例，我们打开  
`$DSH_HOME/profiles/web/cordis.yml` 这个文件，其内容显示为：

```yaml
# dsh profile root — an empty entry list. The tree is composed as patches:
# each bundle in package.json's dsh.profile.bundles, then cordis.patch.yml, then any
# --patch overlays. Edit cordis.patch.yml, not this file.
[]
```
没错，这是一个空的根文件，而这个空根正是"**一切皆插件**"这个设计的精髓所在。

这段注释说明，**根是空的，一切靠补丁（patch）叠加**。这让 dsh 的任何行为都可从配置改写——你要换某行配置、禁用某插件、加自己的工具，都不改 dsh 源码，只在 `cordis.patch.yml`（或更上层）加一条对那个 id 的 patch 即可。

对于dsh来说，一个profile所需要的所有plugin的加载顺序如下：

```plain
profile的空根 cordis.yml  []

  ① 各 bundle 的 cordis.patch.yml（按 dsh.profile.bundles 顺序）
       @deepseek-ai/dsh-base        ← 基座：tools/session/llm/sandbox…
       @deepseek-ai/dsh-web-app     ← 加浏览器应用
       dsh-at-file                  ← 你的扩展（可覆盖上面行）

  ② profile 的 cordis.patch.yml     ← 你自己的补丁层（覆盖任何 bundle 行）

  ③ home 级 cordis.patch.yml        ← 全局用户补丁（跨所有 profile）

  ④ --patch overlay                 ← 命令行临时覆盖，优先级最高
```

**顺序即优先级**：列表后面的层能覆盖前面的。所以用户自定义扩展放在 `dsh-base` 和 `dsh-web-app ` **之后**，这样才能改写基座行，插件的身份是通过id标识的，后面的同名插件可以覆盖前面的插件。

前文我们展示了部分`dsh-base`的插件配置，实际上，完整的`dsh-base` 的patch（`dsh-base/cordis.patch.yml`）总共包含 78 个 plugins，这些都是Agent非常核心的插件，比如Agent功能主要以下三个插件完成：

| id | plugin 包 |
| --- | --- |
| agent | dsh-agent |
| agent-loop | dsh-agent-loop |
| system-prompt | dsh-system-prompt |


所有的插件都可以被替换。假设你想要为web profile替换掉`dsh-agent-loop`的话，你可以修改`$USER_HOME/.dsh/profiles/web/cordis.patch.yml` 文件，添加下面的内容：

```yaml
# 1. 禁用 dsh-base 挂的原 agent-loop 行（name 不用写，写了反而要匹配）
- id: agent-loop
  disabled: true

# 2. 插入你的替换实现：新 id + 你的包名
- insert:
    - id: my-agent-loop
      name: '@your-org/dsh-agent-loop-mine'
      config:
        agents: []
```

你看，连最核心的Agent Loop都能以配置的方式进行替换，还有什么是不能替换的呢。所以在dsh中，**Everything is plugin！**

然而，就我自己实现Agent的经验来看，其中最复杂的部分就是Agent Loop，所以，即使dsh有替换一切的能力，又有谁会真正把这些核心替换掉呢，或者说替换的价值在哪里呢。所以。还是那句话，是噱头还是颠覆，我们让子弹飞一会，让时间去评论吧！

如果觉得本文对你有帮助，请支持我的新书[《Harness工程实战》](https://item.jd.com/10233699155932.html)
