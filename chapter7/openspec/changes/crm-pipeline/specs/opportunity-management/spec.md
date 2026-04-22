## ADDED Requirements

### Requirement: Opportunity CRUD
系统 SHALL 提供机会的创建、查询、更新和删除功能。机会创建时 title 和 customerId MUST NOT 为空。机会阶段默认为 PROSPECTING。

#### Scenario: 创建机会
- **WHEN** 用户提交机会数据（title非空、customerId非空）
- **THEN** 系统创建机会，stage默认为PROSPECTING，设置createTime和updateTime，返回创建的机会

#### Scenario: 创建机会缺少必填字段
- **WHEN** 用户提交机会数据且title为空或customerId为空
- **THEN** 系统返回校验错误

#### Scenario: 查询机会列表
- **WHEN** 用户按条件（title模糊、stage、customerId、ownerName）分页查询机会
- **THEN** 系统返回匹配的分页结果

#### Scenario: 更新机会
- **WHEN** 用户修改机会信息并提交
- **THEN** 系统更新机会，刷新updateTime

#### Scenario: 删除机会
- **WHEN** 用户删除指定ID的机会
- **THEN** 系统删除该机会记录

### Requirement: Opportunity 阶段流转
机会 SHALL 按以下阶段流转：PROSPECTING → QUALIFYING → PROPOSAL → NEGOTIATION → WON（终态）；任意非终态可流转至 LOST（终态）。WON 和 LOST 为终态，MUST NOT 再变更阶段。

#### Scenario: 正向推进阶段
- **WHEN** 用户将机会阶段从 PROSPECTING 改为 QUALIFYING
- **THEN** 系统更新阶段成功

#### Scenario: 标记为丢单
- **WHEN** 用户将非终态机会阶段改为 LOST
- **THEN** 系统更新阶段成功，该机会不可再变更阶段

#### Scenario: 已赢单机会不可变更
- **WHEN** 用户尝试修改已WON机会的阶段
- **THEN** 系统返回错误，拒绝操作

### Requirement: Opportunity 看板视图
系统 SHALL 提供看板视图API，按阶段分组返回所有机会数据。看板数据 SHALL 全量加载，不做分页。

#### Scenario: 获取看板数据
- **WHEN** 用户请求看板数据
- **THEN** 系统返回按stage分组的机会列表，结构为 { PROSPECTING: [...], QUALIFYING: [...], PROPOSAL: [...], NEGOTIATION: [...], WON: [...], LOST: [...] }

#### Scenario: 更新机会阶段（拖拽）
- **WHEN** 用户通过PATCH请求更新机会的stage
- **THEN** 系统校验阶段流转合法性，合法则更新，刷新updateTime；不合法则返回错误

### Requirement: Opportunity 赢单创建订单
机会赢单时 SHALL 自动创建首个Order。赢单后仍可手动创建更多Order（1:N关系）。Opp.amount仅为预计金额，MUST NOT 与Order合计金额做对比追踪。

#### Scenario: 赢单创建首个订单
- **WHEN** 用户对非终态机会执行赢单操作，提供amount（必填）
- **THEN** 系统将Opp.stage更新为WON，自动创建Order（关联customerId和opportunityId），自动生成orderNo，返回创建的Order

#### Scenario: 已终态机会赢单
- **WHEN** 用户对已WON或LOST的机会执行赢单操作
- **THEN** 系统返回错误，拒绝操作

#### Scenario: 赢单后继续创建订单
- **WHEN** 用户对已WON的机会手动创建新Order
- **THEN** 系统创建Order，关联同一Opportunity和Customer
