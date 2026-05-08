# HarnessEngineering

AI 辅助软件工程实战 — 从 AI Agent 基础到全栈 CRM 应用的渐进式工程实践。

## 项目概览

本项目是一本 AI 辅助软件工程书籍的配套代码仓库，按章节组织，循序渐进地构建从 AI Agent 基础到生产级全栈 CRM 应用的完整工程体系。

```
HarnessEngineering/
├── chapter1/          AI Agent 基础 — ReAct 模式与工具调用
├── chapter2/          MCP Server — Model Context Protocol 服务端开发
├── chapter3/          AI 代码审查 — PR 自动化审查 Skill 构建
├── chapter7/          客户中心微服务 — Spring Boot + COLA DDD
├── chapter8/          全栈 CRM 应用 — Vue 3 + Spring Boot + COLA DDD
├── chapter9/          测试策略 — WireMock 重构与 E2E 自动化
├── chapter10/         领域驱动重构 — 遗留代码的 DDD 改造
├── e2e-tests/         Playwright E2E 测试套件
├── .github/workflows/ GitHub Actions CI/CD 流水线
├── docker-compose.yml       本地开发环境
└── docker-compose.prod.yml  生产部署配置
```

## 章节详情

### Chapter 1: AI Agent 基础

| 文件 | 说明 |
|------|------|
| `deepseek.py` | 最简 LLM API 调用示例 |
| `calculator_agent.py` | 基于 XML 协议的自定义工具调用（乘除法） |
| `function_calling.py` | OpenAI Function Calling API 实现天气查询 |
| `react_agent.py` | 通用 ReAct Agent 框架（Thought → Action → Observation） |
| `simple_claude_agent.py` | 结合文件/终端工具的 CLI Agent |

**技术栈：** Python, DeepSeek LLM, OpenAI SDK

### Chapter 2: MCP Server

| 文件 | 说明 |
|------|------|
| `mcp_weather_server.py` | 基于 FastMCP 的天气查询 MCP Server（stdio 传输） |
| `mcp_logger.py` | MCP 通信 I/O 代理，用于调试客户端-服务端交互 |

**技术栈：** Python, FastMCP, stdio transport

### Chapter 3: AI 代码审查

| 文件/目录 | 说明 |
|-----------|------|
| `code-reviewer.md` | 代码审查 Agent 角色定义 |
| `pr-code-review/` | 完整 PR 代码审查 Skill（GitHub 集成） |
| `pr-code-review/references/` | Python / Go / Java / C++ / Shell 审查规范 |

**技术栈：** Python, GitHub API, DeepSeek

### Chapter 7: 客户中心微服务

独立微服务，管理银行客户主数据（个人信息、账户状态、会员等级、风险等级、AUM 等）。

**技术栈：** Java 21, Spring Boot 3.3.6, Spring Data JPA, MySQL 8.0, COLA DDD 架构

**COLA 分层：**

```
adapter/web/          控制器、DTO、Assembler
app/service/          应用服务（@Transactional）
domain/entity/        领域实体（业务规则）
domain/gateway/       网关接口（依赖倒置）
domain/service/       领域服务（跨实体规则）
infrastructure/       网关实现、JPA Repository、类型转换器
```

### Chapter 8: 全栈 CRM 应用

核心章节，使用 AI 辅助迭代式构建完整 CRM 系统。

**后端 (crm-backend)：** Java 21, Spring Boot 3.4.5, Spring Data JPA, MySQL, COLA DDD
**前端 (crm-frontend)：** Vue 3.5, TypeScript, Vite, Ant Design Vue 4, Pinia, Vue Router

**业务功能：** 客户 CRUD、线索管理（含转化）、商机管理（列表 + 看板）、订单管理、标签管理、客户 360 视图、客户备注

**迭代构建过程：**

1. 项目脚手架搭建（Vue + Spring Boot + COLA，仅客户 CRUD）
2. 集成测试安全网
3. 业务迭代 1：线索、商机、订单管理
4. 业务迭代 2：客户备注 + ArchUnit 架构守护
5. 业务迭代 3：客户标签
6. 业务迭代 4：外部系统集成（customer-center）

### Chapter 9: 测试策略

- **WireMock 重构：** 替换 `@MockitoBean`，使用 JSON Fixture 进行集成测试
- **E2E 自动化：** 裸机（Windows/macOS）与 Docker 沙箱两种环境的端到端测试流程

### Chapter 10: 领域驱动重构

从遗留代码中发现领域概念，提取 `PricingPolicy` 领域服务：

1. 识别 `LegacyOrderService` 中的隐藏领域模型
2. 为 `calculateFinancialDiscount` 建立单元测试安全网
3. 提取领域服务，支持扩展（如"高净值客户专属金融折扣"）

## 系统架构

```
┌─────────────┐     ┌──────────────┐     ┌──────────────────┐
│  Vue Frontend │────▶│  CRM Backend  │────▶│ Customer Center  │
│   (Nginx:80)  │     │  (Port 8080)  │     │  (Port 8081)     │
└─────────────┘     └──────┬───────┘     └────────┬─────────┘
                           │                       │
                           └───────────┬───────────┘
                                       ▼
                              ┌────────────────┐
                              │   MySQL 8.0    │
                              │   (Port 3306)  │
                              └────────────────┘
```

## 快速开始

### 前置条件

- Docker & Docker Compose
- Java 21（本地开发后端）
- Node.js 18+（本地开发前端 / E2E 测试）
- Python 3.10+（Chapter 1-3）
- uv 包管理器（Chapter 2）

### Docker 一键启动

```bash
# 启动所有服务（MySQL + 后端 + 前端）
docker compose up -d

# 运行 E2E 测试
docker compose --profile test up
```

### 本地开发

```bash
# 1. 启动 MySQL
docker compose up mysql -d

# 2. 启动后端服务
cd chapter8/crm-backend && mvn spring-boot:run
cd chapter7/customer-center && mvn spring-boot:run

# 3. 启动前端
cd chapter8/crm-frontend && npm install && npm run dev
```

## CI/CD

| 工作流 | 触发条件 | 说明 |
|--------|---------|------|
| `crm-backend-test.yml` | `chapter8/crm-backend/**` 变更 | Maven 单元测试 (JDK 21) |
| `customer-center-test.yml` | `chapter7/customer-center/**` 变更 | Maven 单元测试 (JDK 21) |
| `e2e-test.yml` | 手动触发 | Docker Compose 全链路 E2E 测试 |
| `deploy-aliyun.yml` | 手动触发 | 构建镜像 → 推送阿里云 ACR → 部署至 ECS |

## 技术栈总览

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3, TypeScript, Ant Design Vue, Pinia, Vite |
| 后端 | Java 21, Spring Boot 3, Spring Data JPA, Lombok |
| 架构 | COLA DDD（适配器-应用-领域-基础设施四层） |
| 数据库 | MySQL 8.0, H2（测试） |
| 测试 | JUnit 5, MockMvc, WireMock, ArchUnit, Playwright |
| 部署 | Docker, Nginx, GitHub Actions, 阿里云 ACR/ECS |
| AI | DeepSeek, OpenAI SDK, FastMCP |
