## Why

当前CRM仅有客户（Customer）管理功能，缺少从线索到成单的核心业务管线。销售团队无法追踪潜在客户的转化过程、商机跟进阶段和最终订单成交，导致业务流程断裂、数据无法串联。需要增加线索（Lead）、机会（Opportunity）、订单（Order）三大模块，构建完整的 Lead → Opportunity → Order 销售管线。

## What Changes

- 新增**线索（Lead）**模块：线索的CRUD、状态流转（NEW → CONTACTED → QUALIFIED → CONVERTED / UNQUALIFIED）、线索转化功能
- 新增**机会（Opportunity）**模块：机会的CRUD、阶段流转（PROSPECTING → QUALIFYING → PROPOSAL → NEGOTIATION → WON / LOST）、看板视图、赢单创建订单
- 新增**订单（Order）**模块：订单的CRUD、状态流转（PENDING → CONFIRMED → PROCESSING → COMPLETED / CANCELLED）、订单号自动生成
- 新增**线索转化**流程：转化时原子创建Customer + Opportunity，标记Lead为CONVERTED，不可逆
- 新增**赢单创建订单**流程：Opportunity赢单时自动创建首个Order，之后可继续手动创建多个Order（1:N）
- 新增前端看板视图：Opportunity按阶段拖拽卡片，全量加载不分页
- 新增侧边栏导航，整合四个模块页面
- 新增 owner_name 字段（Lead/Opportunity/Order），轻量负责人标识，后续接入权限体系

## Capabilities

### New Capabilities
- `lead-management`: 线索的创建、编辑、删除、查询、状态流转和转化
- `opportunity-management`: 机会的创建、编辑、删除、查询、阶段流转、看板视图和赢单
- `order-management`: 订单的创建、编辑、删除、查询、状态流转和订单号生成

### Modified Capabilities
<!-- 无已有spec需要修改 -->

## Impact

- **数据库**：新增 lead、opportunity、order 三张表，customer 表无变更
- **后端**：新增3套完整COLA分层代码（adapter/app/domain/infrastructure），LeadService和OpportunityService存在跨域编排调用
- **前端**：新增3个模块的页面组件、API层、类型定义、路由和侧边栏导航；Opportunity看板需要拖拽交互（vue-draggable-plus或类似库）
- **API**：新增 /api/leads、/api/opportunities、/api/orders 三组REST端点，含转化（/convert）、赢单（/win）、看板（/kanban）、阶段更新（/stage）等特殊操作
