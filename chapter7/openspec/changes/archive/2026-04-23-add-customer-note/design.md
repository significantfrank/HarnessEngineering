## Context

当前客户模块只有 `CustomerEntity`，包含 `lastFollowUp`（最后跟进时间戳）和 `remark`（静态备注），无法记录跟进历史。销售团队需要按时间线记录每次客户互动的内容和方式，形成可回溯的沟通记录。

系统采用 COLA 分层架构，app 层直接操作 gateway，原子业务逻辑沉淀在领域对象中。前端使用 Vue3 + Ant Design Vue + Pinia。

## Goals / Non-Goals

**Goals:**
- 为客户增加小记功能，支持按时间线记录跟进内容
- 小记类型化（电话/拜访/邮件/微信/其他），前端按类型展示不同 icon
- 新增小记时自动联动更新 `customer.lastFollowUp`
- 新增客户详情页，整合基本信息和小记时间线

**Non-Goals:**
- 不做小记的删除和编辑，只增不改
- 不做小记的通用化（仅客户域，不扩展到 lead/opportunity/order）
- 不做小记的搜索和统计功能
- 不做小记附件或图片

## Decisions

### 1. 小记归属：嵌入 customer 域（方案 A）

**选择**：小记放在 `domain/customer/` 下，而非独立域。

**理由**：小记离开客户没有独立含义，未来也不计划扩展到其他领域。若独立建域，会增加不必要的跨域依赖。

### 2. API 风格：嵌套资源

**选择**：`/api/customers/{id}/notes`

**备选**：`/api/customer-notes` 独立资源

**理由**：小记从属于客户，URL 应体现归属关系。Controller 增加 3 个方法（POST/GET/GET列表）不算膨胀。

### 3. lastFollowUp 联动：领域对象封装

**选择**：在 `CustomerNoteEntity` 上定义 `updateCustomerLastFollowUp(CustomerEntity)` 方法，由 app 层 service 调用。

**理由**：按编码规范，原子业务逻辑沉淀在领域对象中。联动逻辑是业务规则，不是简单的数据搬运。

### 4. 前端页面：独立详情页

**选择**：新增 `CustomerDetail.vue`，路由 `/customers/:id`

**备选**：Drawer 侧边抽屉

**理由**：详情页可承载更多信息（基本信息 + 小记 + 未来扩展），抽屉空间有限。已有 OppDetail/OrderDetail 的详情页先例。

### 5. 小记不可删除

**选择**：不提供 DELETE 端点

**理由**：小记是沟通记录，具有审计性质。误录场景可通过后续补充小记纠正。

## Risks / Trade-offs

- **CustomerController 膨胀** → 目前只增加 3 个端点，可接受。若后续扩展可抽取 `CustomerNoteController`
- **小记量大时的查询性能** → 初期数据量小，不做分页优化。后期可加 `customer_id + create_time` 联合索引
- **lastFollowUp 联动的事务性** → 新增小记和更新 lastFollowUp 在同一 Service 方法中完成，使用 Spring 声明式事务保证一致性
