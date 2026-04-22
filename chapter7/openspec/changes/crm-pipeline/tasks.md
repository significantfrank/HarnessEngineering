## 1. 数据库

- [ ] 1.1 在 schema.sql 中新增 lead 表（id, name, phone, email, company, source, status, customer_id, owner_name, remark, create_time, update_time）
- [ ] 1.2 在 schema.sql 中新增 opportunity 表（id, title, customer_id, amount, stage, probability, expected_close_date, lead_id, owner_name, remark, create_time, update_time）
- [ ] 1.3 在 schema.sql 中新增 order 表（id, order_no, customer_id, opportunity_id, total_amount, status, owner_name, remark, create_time, update_time）
- [ ] 1.4 执行 schema.sql 初始化数据库

## 2. Lead 后端

- [ ] 2.1 创建 Lead 枚举：LeadStatus（NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED）
- [ ] 2.2 创建 LeadEntity（JPA实体，含所有字段，prePersist/preUpdate方法）
- [ ] 2.3 创建 CustomerSource 复用：确认 Lead 使用已有的 CustomerSource 枚举
- [ ] 2.4 创建 LeadGatewayI 接口（save, findById, deleteById, findByConditions）
- [ ] 2.5 创建 LeadRepository（JpaRepository + JpaSpecificationExecutor）
- [ ] 2.6 创建 LeadGatewayImpl（实现条件查询，复用 CustomerGatewayImpl 的 Specification 模式）
- [ ] 2.7 创建 LeadDomainService（create, update, findById, deleteById, findByConditions，含状态流转校验）
- [ ] 2.8 创建 LeadDTO（含所有字段和校验注解）
- [ ] 2.9 创建 LeadConvertDTO（opportunityTitle必填, amount, expectedCloseDate可选）
- [ ] 2.10 创建 LeadService（CRUD + convert方法，convert中编排CustomerDomainService和OpportunityDomainService，@Transactional）
- [ ] 2.11 创建 LeadController（/api/leads，CRUD + POST /api/leads/{id}/convert）

## 3. Opportunity 后端

- [ ] 3.1 创建 OppStage 枚举（PROSPECTING, QUALIFYING, PROPOSAL, NEGOTIATION, WON, LOST）
- [ ] 3.2 创建 OpportunityEntity（JPA实体，含所有字段，prePersist/preUpdate方法）
- [ ] 3.3 创建 OpportunityGatewayI 接口（save, findById, deleteById, findByConditions, findAllForKanban）
- [ ] 3.4 创建 OpportunityRepository（JpaRepository + JpaSpecificationExecutor + 按日期查最大orderNo的方法预留）
- [ ] 3.5 创建 OpportunityGatewayImpl（含条件查询和看板全量查询实现）
- [ ] 3.6 创建 OpportunityDomainService（create, update, findById, deleteById, findByConditions, updateStage含流转校验, findKanbanData）
- [ ] 3.7 创建 OpportunityDTO（含所有字段和校验注解）
- [ ] 3.8 创建 OppWinDTO（amount必填, remark可选）
- [ ] 3.9 创建 OpportunityService（CRUD + kanban + updateStage + win方法，win中编排OrderDomainService，@Transactional）
- [ ] 3.10 创建 OpportunityController（/api/opportunities，CRUD + GET /kanban + PATCH /{id}/stage + POST /{id}/win）

## 4. Order 后端

- [ ] 4.1 创建 OrderStatus 枚举（PENDING, CONFIRMED, PROCESSING, COMPLETED, CANCELLED）
- [ ] 4.2 创建 OrderEntity（JPA实体，含所有字段，prePersist/preUpdate方法）
- [ ] 4.3 创建 OrderGatewayI 接口（save, findById, deleteById, findByConditions, findMaxOrderNoByDate）
- [ ] 4.4 创建 OrderRepository（JpaRepository + JpaSpecificationExecutor + @Query查当日最大orderNo）
- [ ] 4.5 创建 OrderGatewayImpl（含订单号生成逻辑：查询当日最大orderNo并递增）
- [ ] 4.6 创建 OrderDomainService（create含订单号生成, update, findById, deleteById, findByConditions，含状态流转校验）
- [ ] 4.7 创建 OrderDTO（含所有字段和校验注解，orderNo为只读）
- [ ] 4.8 创建 OrderService（CRUD，update时orderNo不可修改）
- [ ] 4.9 创建 OrderController（/api/orders，CRUD端点）

## 5. Lead 前端

- [ ] 5.1 创建 types/lead.ts（LeadStatus, Lead接口, LeadQuery, LeadConvertData）
- [ ] 5.2 创建 api/lead.ts（CRUD + convert API调用）
- [ ] 5.3 创建 stores/lead.ts（Pinia store，复用customer store模式）
- [ ] 5.4 创建 views/lead/LeadList.vue（列表 + 筛选 + 转化按钮）
- [ ] 5.5 创建 views/lead/LeadForm.vue（新增/编辑表单）
- [ ] 5.6 创建 views/lead/ConvertDialog.vue（转化弹窗，填写Opp信息）
- [ ] 5.7 添加 /leads 路由

## 6. Opportunity 前端

- [ ] 6.1 创建 types/opportunity.ts（OppStage, Opportunity接口, OpportunityQuery, KanbanData）
- [ ] 6.2 创建 api/opportunity.ts（CRUD + kanban + updateStage + win API调用）
- [ ] 6.3 创建 stores/opportunity.ts（Pinia store，含看板数据管理）
- [ ] 6.4 安装 vue-draggable-plus 拖拽库
- [ ] 6.5 创建 views/opportunity/OppKanban.vue（看板视图，按stage分列，支持拖拽更新stage）
- [ ] 6.6 创建 views/opportunity/OppList.vue（列表视图，筛选+分页）
- [ ] 6.7 创建 views/opportunity/OppForm.vue（新增/编辑表单）
- [ ] 6.8 创建 views/opportunity/OppDetail.vue（详情 + 关联订单列表 + 赢单按钮 + 创建订单入口）
- [ ] 6.9 添加 /opportunities 路由（默认看板，可切换列表）

## 7. Order 前端

- [ ] 7.1 创建 types/order.ts（OrderStatus, Order接口, OrderQuery）
- [ ] 7.2 创建 api/order.ts（CRUD API调用）
- [ ] 7.3 创建 stores/order.ts（Pinia store）
- [ ] 7.4 创建 views/order/OrderList.vue（列表 + 筛选）
- [ ] 7.5 创建 views/order/OrderForm.vue（新增/编辑表单，新增时选择Opportunity和Customer）
- [ ] 7.6 创建 views/order/OrderDetail.vue（订单详情）
- [ ] 7.7 添加 /orders 路由

## 8. 导航与集成

- [ ] 8.1 创建全局侧边栏布局组件（线索/机会/订单/客户四个导航项）
- [ ] 8.2 更新 App.vue 使用侧边栏布局
- [ ] 8.3 更新路由配置，整合所有模块路由
- [ ] 8.4 提取公共类型 PageResult/ApiResponse 到 types/common.ts，各模块复用

## 9. 测试

- [ ] 9.1 LeadDomainService 单元测试（状态流转校验、转化逻辑）
- [ ] 9.2 OpportunityDomainService 单元测试（阶段流转校验）
- [ ] 9.3 OrderDomainService 单元测试（订单号生成、状态流转校验）
- [ ] 9.4 LeadService 单元测试（转化编排事务性）
- [ ] 9.5 OpportunityService 单元测试（赢单创建订单）
- [ ] 9.6 OrderService 单元测试（CRUD、orderNo不可修改）
