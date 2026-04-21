## Context

从零搭建前后端分离 CRM 系统。后端采用 COLA 分包架构（adapter / app / domain / infrastructure），不分模块，单 Maven 项目；前端采用 Vue 3 + TypeScript + Ant Design Vue。本次迭代只实现 Customer CRUD，重点在框架搭建。

## Goals / Non-Goals

**Goals:**
- 搭建可扩展的前后端项目框架，为后续业务迭代奠基
- 后端 COLA 分包架构完整落地：adapter → app → domain → infrastructure，依赖倒置
- 前端标准 Vue 3 项目结构，包含路由、状态管理、API 封装
- Customer 完整 CRUD：列表分页搜索、新增、编辑、删除
- 统一 API 响应格式

**Non-Goals:**
- 认证鉴权（后续迭代）
- 多模块 Maven 拆分（当前单模块分包即可）
- Customer 关联业务（联系人、跟进记录等，后续迭代）
- 部署方案

## Decisions

### 1. COLA 分包而非分模块

单 `crm-backend` Maven 模块，通过包（adapter/app/domain/infrastructure）隔离层次。分包比分模块更轻量，适合当前阶段，后续如需模块级编译隔离可再拆。

### 2. Entity 复用为 JPA 实体

`CustomerEntity` 同时承担领域对象和持久化对象角色，直接加 `@Entity` 注解。省去 DO 层和转换逻辑，对于 CRUD 阶段够用。若后续领域模型与数据模型出现分歧，再引入 DO 层分离。

### 3. Spring Data JPA 作为 ORM

JPA 与 Entity 复用策略天然契合，减少样板代码。Repository 接口放在 infrastructure 层，domain 层通过 GatewayI 接口实现依赖倒置。

### 4. 无 Service 接口，直接实现类

`CustomerService` 直接是类而非接口+实现，减少 CRUD 阶段不必要的抽象。Gateway 仍保留接口（`CustomerGatewayI`），因为这是 COLA 依赖倒置的核心。

### 5. 单一 DTO

`CustomerDTO` 用于创建、更新、返回，不拆分 CreateCmd / Query 等多 DTO。后续有差异化需求再拆分。

### 6. 枚举字段

source / level / status 使用 Java 枚举，JPA 以 `EnumType.STRING` 存储，保证可读性。枚举定义在 domain 层。

### 7. 前端技术选型

- Vue 3 + Composition API + `<script setup>`
- TypeScript
- Ant Design Vue 4.x
- Vite 构建
- Pinia 状态管理
- Axios HTTP 客户端
- Vue Router

## Risks / Trade-offs

- **Entity 兼做 JPA 实体** → 领域层依赖 JPA 注解，若未来需切换持久化方案需重构。风险可控，CRUD 阶段收益大于风险。
- **单 DTO 复用** → 创建/更新/返回共用一个 DTO，可能导致冗余字段或验证不精确。后续按需拆分即可。
- **无 Service 接口** → Mock 测试需要 mock 实现类而非接口。影响较小。
