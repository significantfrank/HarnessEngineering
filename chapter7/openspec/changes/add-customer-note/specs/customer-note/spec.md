## ADDED Requirements

### Requirement: Create customer note
系统 SHALL 允许通过 POST `/api/customers/{customerId}/notes` 为指定客户新增小记，请求体包含 category 和 content，返回创建后的 CustomerNoteDTO。

#### Scenario: Successful creation
- **WHEN** 发送 POST `/api/customers/{customerId}/notes` 且客户存在、category 和 content 非空
- **THEN** 返回 HTTP 200，响应 data 包含新建小记信息（含自动生成的 id 和 createTime）

#### Scenario: Customer not found
- **WHEN** 发送 POST `/api/customers/{customerId}/notes` 且 customerId 不存在
- **THEN** 返回 HTTP 200，响应 code 为 "404"，message 包含错误描述

### Requirement: List customer notes
系统 SHALL 允许通过 GET `/api/customers/{customerId}/notes` 分页查询指定客户的小记列表，按创建时间倒序排列。

#### Scenario: Successful list
- **WHEN** 发送 GET `/api/customers/{customerId}/notes?page=0&size=10` 且客户存在
- **THEN** 返回 HTTP 200，响应 data 包含 content（小记数组，按 createTime 倒序）、totalElements 等分页信息

#### Scenario: Customer not found
- **WHEN** 发送 GET `/api/customers/{customerId}/notes` 且 customerId 不存在
- **THEN** 返回 HTTP 200，响应 code 为 "404"

### Requirement: Note category enum
小记类型 SHALL 为枚举，包含以下值：PHONE_CALL（电话沟通）、VISIT（上门拜访）、EMAIL（邮件沟通）、WECHAT（微信沟通）、OTHER（其他）。枚举值以 STRING 存储于数据库和 JSON。

#### Scenario: Enum storage
- **WHEN** 创建小记时 category=PHONE_CALL
- **THEN** 数据库中以字符串 "PHONE_CALL" 存储，JSON 响应中也为 "PHONE_CALL"

### Requirement: Auto update lastFollowUp on note creation
新增小记时，系统 SHALL 自动更新所属客户的 `lastFollowUp` 字段为小记的创建时间（当小记创建时间晚于当前 lastFollowUp 时）。此业务逻辑 SHALL 封装在 CustomerNoteEntity 领域对象的方法中。

#### Scenario: LastFollowUp is null
- **WHEN** 新增小记且客户当前 lastFollowUp 为 null
- **THEN** 客户的 lastFollowUp 被更新为小记的 createTime

#### Scenario: LastFollowUp is earlier than note creation time
- **WHEN** 新增小记且客户当前 lastFollowUp 早于小记 createTime
- **THEN** 客户的 lastFollowUp 被更新为小记的 createTime

### Requirement: Note is append-only
小记 SHALL 不提供删除和编辑接口，仅支持新增和查询。

#### Scenario: No delete endpoint
- **WHEN** 尝试 DELETE `/api/customers/{customerId}/notes/{noteId}`
- **THEN** 返回 HTTP 405

### Requirement: Customer note entity fields
CustomerNote 实体 SHALL 包含以下字段：id, customerId, category, content, createTime, updateTime。其中 category 为 NoteCategory 枚举类型。

#### Scenario: Field completeness
- **WHEN** 创建小记时提供 customerId=1, category=VISIT, content="拜访客户"
- **THEN** 保存的实体包含自动生成的 id、提供的 customerId/category/content、自动填充的 createTime 和 updateTime
