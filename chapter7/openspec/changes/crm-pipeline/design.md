## Context

当前CRM系统仅有Customer模块，采用COLA架构（按包分层而非模块分层），单一Maven模块。现有模式清晰：Entity即JPA实体、Gateway接口在domain层实现在infrastructure层、DomainService负责生命周期、App层Service做DTO转换和编排。前端使用Vue3 + Ant Design Vue + Pinia。需要在此基础上新增Lead、Opportunity、Order三大业务模块，构建完整的销售管线。

## Goals / Non-Goals

**Goals:**
- 实现Lead → Opportunity → Order的完整销售管线，支持线索转化、商机跟进、订单管理
- 遵循现有COLA分层架构，每个实体独立包结构
- 跨域调用在App层编排，保持Domain Service独立性
- Opportunity提供看板视图（按阶段拖拽），全量加载不分页
- 支持Opportunity 1:N Order关系
- 轻量负责人字段（owner_name），为后续权限体系预留

**Non-Goals:**
- 不做用户认证/权限系统（owner_name仅为字符串）
- 不做Opportunity金额与Order金额的追踪对比
- 不做看板分页（假设按销售人员过滤后数据量可控）
- 不做领域事件解耦（App层编排已足够）
- 不做消息通知或邮件提醒

## Decisions

### D1: 跨域调用在App层编排

Lead转化需要同时操作Customer和Opportunity，Opportunity赢单需要创建Order。

**方案**: App层Service注入多个DomainService进行编排，@Transactional保证原子性。

**备选**: 领域事件解耦 → 过度设计，当前阶段增加复杂度无收益；直接在DomainService中调用其他DomainService → 域间耦合，违反COLA分层原则。

**依赖方向**: LeadService → CustomerDomainService + OpportunityDomainService；OpportunityService → OrderDomainService。无循环依赖。

### D2: Lead转化不可逆，原子事务

转化操作一次性完成：创建Customer + 创建Opportunity + 标记Lead为CONVERTED。一旦CONVERTED不可回退。

**方案**: LeadService.convert() 方法加 @Transactional，三步操作在同一事务中。CONVERTED和UNQUALIFIED均为终态。

### D3: Opportunity 1:N Order

一个Opportunity赢单后可创建多个Order。赢单时自动创建首个Order，之后可手动继续创建。

**方案**: Order表通过opportunity_id关联Opportunity。Opp.stage=WON后仍可创建Order。Opp.amount仅为"预计金额"，不与Order合计做对比追踪。

### D4: 订单号生成规则

格式：`ORD-yyyyMMdd-NNN`（日期 + 当日三位序列号）

**方案**: OrderDomainService创建Order时，查询当日最大订单号，递增序列。事务内查询保证唯一性。备选：UUID → 可读性差；雪花ID → 需额外基础设施。

### D5: 看板视图全量加载

**方案**: 新增 `GET /api/opportunities/kanban` 端点，返回按stage分组的数据结构。拖拽更新通过 `PATCH /api/opportunities/{id}/stage` 实现。不做分页，后续数据量问题通过筛选条件解决。

### D6: owner_name 轻量负责人字段

**方案**: Lead、Opportunity、Order均增加 owner_name (VARCHAR 100) 字段，仅为字符串存储，不关联用户表。后续接入权限系统时升级为owner_id外键。

### D7: Opportunity状态机约束

**方案**: DomainService中校验stage流转合法性，仅允许相邻阶段推进或回退。WON/LOST为终态不可变更。stage更新通过独立API（PATCH /stage），看板拖拽调用此API。

## Risks / Trade-offs

- **[订单号并发冲突]** → 事务内MAX查询+序列递增，高并发下可能冲突。当前阶段可接受，后续可改用数据库序列或Redis自增。
- **[看板全量加载性能]** → Opportunity数据量大时API响应慢。缓解：默认按owner_name或时间范围过滤，加"查看全部"需谨慎。
- **[owner_name无约束]** → 可填任意字符串，无法保证数据一致性。后续接入用户系统时需做数据迁移。
- **[转化不可逆]** → 误操作无法恢复。缓解：前端加二次确认弹窗，后端校验status非CONVERTED才允许操作。
