## Context

CRM系统当前自成一体，CustomerEntity包含全部客户数据（主数据+行为数据），无外部系统依赖。现需与customer-center（客户主数据服务，http://localhost:8081）打通。

customer-center是一个金融客户主数据系统，提供：
- `GET /api/customers` 列表查询（返回全量，无idNumber精确过滤）
- `GET /api/customers/{id}` 按ID查询
- `POST /api/customers` 创建（idNumber重复返回400）
- `PUT /api/customers/{id}` 更新

CRM与center的数据边界：
- **center权威**: name, phone, email, idType, idNumber, gender, birthday, occupation, accountStatus, memberLevel, authLevel, riskProfile, incomeRange, aum, availableBalance, totalReturn, holdingProducts
- **CRM独有**: source, level, status, lastFollowUp, remark, company, address, industry, website, contactPerson
- **重叠3字段**: name, phone, email（center为权威源，CRM本地保留用于降级）

技术栈：Spring Boot 3.4.5, Java 21, JPA, MySQL, RestTemplate（已有），Vue 3 + Ant Design Vue（前端）。

## Goals / Non-Goals

**Goals:**
- 创建/更新客户时同步主数据到customer-center，保证跨系统数据一致性
- 客户360视图：后端组装center主数据+CRM行为数据，前端Tab化展示
- center不可用时优雅降级，用CRM本地主数据替代
- 重复客户（同idNumber）自动关联已有center记录而非新建

**Non-Goals:**
- 异步消息队列集成（当前同步调用足够）
- cc_sync_status=FAILED的后台自动补偿任务（标记为长期债务）
- center到CRM的反向同步（center数据变更不通知CRM）
- 客户列表页接入center（仅360视图走组装）

## Decisions

### D1: 创建流程 — 先本地后center（方案B）

**决策**: 先在CRM本地save（cc_sync_status=PENDING），事务提交后调用center，成功后更新cc_sync_status=SYNCED。使用id_number作为与center的关联键，无需回填cc_id。

**替代方案**: 先调center再本地save（方案A）— 更干净但center成功后本地失败时需清理center，且center API没有删除接口。

**理由**: 本地事务更可控，center无delete API，方案B的失败回滚路径更完整。

### D2: 事务边界 — 两步拆分

**决策**: 
- 事务1（@Transactional）: 本地save Customer + 处理标签关联
- 事务外: 调center + 更新cc_sync_status / 回滚本地

```
create():
  @Transactional step1 { save + tags }  → commit
  step2 { center调用 → 成功:cc_sync_status=SYNCED / 失败×3: 删除本地记录+标签 }
```

### D3: 同步失败策略

**决策**: 
- **创建失败**: 重试3次（退避1s/2s/4s）后回滚 — 删除本地Customer记录和标签关联
- **更新失败**: 重试3次后标记cc_sync_status=FAILED — 本地数据保留，不回滚（行为数据与center无关，不应因center不可用而丢失）
- **转化失败**: 重试3次后回滚范围扩大 — 删除Customer+Opportunity，恢复Lead状态（status和customerId复原）

### D4: 重复客户处理 — 策略A（先查后写）

**决策**: 
```
createOrSync():
  ① GET center/api/customers → 全量列表
     在列表中按idNumber匹配
     ├── 找到 → PUT center/api/customers/{id} 更新，返回id
     └── 未找到 → POST center/api/customers 创建，返回id
```

**替代方案**: 直接POST，冲突时转更新（依赖center返回409，实际返回400且无id信息）。

**权衡**: 全量列表查询在center客户量大时性能差，短期可接受（当前5条），长期需center提供idNumber精确查询API。

### D5: 360视图 — 后端组装 + 降级

**决策**: 
- 新增`GET /api/customers/{id}/360`端点
- 后端查询center主数据 + CRM行为数据，组装Customer360DTO返回
- center不可用/超时时：用CRM本地主数据（name/phone/email/idType/idNumber）填充，设置degraded=true

**替代方案**: 前端分别请求center和CRM — 跨域问题、延迟不可控、前端复杂度高。

### D6: 360前端 — Tab化异步加载

**决策**:
- 顶部概览区：同步加载360视图基础信息（身份+CRM运营数据+降级提示）
- Tab-跟进记录：同步加载（Notes随360一起返回）
- Tab-商机：异步加载 `GET /api/opportunities?customerId={id}`
- Tab-订单：异步加载 `GET /api/orders?customerId={id}`

### D7: 技术选型 — RestTemplate

**决策**: 使用RestTemplate调用center API。

**替代方案**: WebClient（需引入webflux依赖，过度设计）。

**理由**: 调用场景简单（创建/更新/查询），无需响应式，RestTemplate已包含在spring-boot-starter-web中。

### D8: 新增Gateway接口

**决策**: 
```
domain/customer/gateway/CustomerCenterGatewayI.java
  - createOrSync(name, phone, email, idType, idNumber) → void
  - update(idNumber, name, phone, email, idType, idNumber) → void
  - findByIdNumber(idNumber) → Optional<CustomerCenterData>
  - isAvailable() → boolean

infrastructure/customer/gateway/CustomerCenterGatewayImpl.java
  - RestTemplate实现，包含退避重试逻辑
```

id_number作为跨系统关联键，不需要cc_id。查询center时通过id_number在列表中匹配，找到后用center返回的id进行PUT操作。

### D9: 数据库变更

**决策**: 
- customer表新增: cc_sync_status VARCHAR(20) DEFAULT 'PENDING', id_type VARCHAR(20), id_number VARCHAR(50) UNIQUE
- lead表新增: id_type VARCHAR(20), id_number VARCHAR(50)
- CustomerEntity对应新增字段，id_number设为唯一约束，CcSyncStatus枚举(PENDING/SYNCED/FAILED)
- LeadEntity对应新增字段

## Risks / Trade-offs

- **[全量列表查询性能]** → 短期可接受，长期需center提供idNumber精确查询API。已在proposal中标记为长期债务。
- **[step1 commit后crash]** → 本地数据已提交但cc_sync_status仍为PENDING。当前不处理，依赖人工排查或未来补偿任务。
- **[id_number不可变]** → id_number作为跨系统关联键，一旦创建不可修改。前端编辑表单中id_number字段应为只读。
- **[center不可用影响创建]** → 创建客户时center必须可用（否则回滚），可能影响CRM可用性。这是刻意选择——保证数据一致性优先于可用性。
- **[转化回滚复杂度]** → 转化失败需回滚3张表（Customer+Opportunity+Lead），手动回滚逻辑需仔细处理。
- **[center数据变更不通知CRM]** → center侧修改主数据后，CRM本地保留的降级数据可能过期。360视图每次实时拉取center可缓解，但非360场景（如列表页）显示的是本地数据。
