## ADDED Requirements

### Requirement: Lead CRUD
系统 SHALL 提供线索的创建、查询、更新和删除功能。线索创建时 name 字段 MUST NOT 为空。线索状态默认为 NEW。

#### Scenario: 创建线索
- **WHEN** 用户提交线索数据（name非空）
- **THEN** 系统创建线索，status默认为NEW，设置createTime和updateTime，返回创建的线索

#### Scenario: 创建线索名称为空
- **WHEN** 用户提交线索数据且name为空
- **THEN** 系统返回校验错误

#### Scenario: 查询线索列表
- **WHEN** 用户按条件（name模糊、status、source）分页查询线索
- **THEN** 系统返回匹配的分页结果

#### Scenario: 更新线索
- **WHEN** 用户修改线索信息并提交
- **THEN** 系统更新线索，刷新updateTime

#### Scenario: 删除线索
- **WHEN** 用户删除指定ID的线索
- **THEN** 系统删除该线索记录

### Requirement: Lead 状态流转
线索 SHALL 按以下状态流转：NEW → CONTACTED → QUALIFIED → CONVERTED（终态）；任意非终态可流转至 UNQUALIFIED（终态）。CONVERTED 和 UNQUALIFIED 为终态，MUST NOT 再变更状态。

#### Scenario: 正向推进状态
- **WHEN** 用户将线索状态从 NEW 改为 CONTACTED
- **THEN** 系统更新状态成功

#### Scenario: 标记为不合格
- **WHEN** 用户将非终态线索状态改为 UNQUALIFIED
- **THEN** 系统更新状态成功，该线索不可再变更状态

#### Scenario: 已转化线索不可变更
- **WHEN** 用户尝试修改已CONVERTED线索的状态
- **THEN** 系统返回错误，拒绝操作

### Requirement: Lead 转化
线索转化 SHALL 原子性地完成以下操作：创建Customer（从Lead复制信息）、创建Opportunity（关联新Customer）、将Lead标记为CONVERTED并关联新Customer。转化操作 MUST 在同一事务中完成，不可逆。

#### Scenario: 成功转化线索
- **WHEN** 用户对非终态线索执行转化操作，提供opportunityTitle（必填）、amount（可选）、expectedCloseDate（可选）
- **THEN** 系统在同一事务中创建Customer和Opportunity，Lead.status变为CONVERTED，Lead.customerId设为新Customer的ID，Opportunity.leadId设为Lead的ID，返回创建的Customer和Opportunity

#### Scenario: 已转化线索再次转化
- **WHEN** 用户对已CONVERTED的线索执行转化操作
- **THEN** 系统返回错误，拒绝操作

#### Scenario: 不合格线索转化
- **WHEN** 用户对UNQUALIFIED的线索执行转化操作
- **THEN** 系统返回错误，拒绝操作
