## Requirements

### Requirement: Create customer
系统 SHALL 允许通过 POST `/api/customers` 创建新客户，请求体为 CustomerDTO，返回创建后的 CustomerDTO。CustomerDTO SHALL 支持 tagIds 字段（标签 ID 列表）。

#### Scenario: Successful creation
- **WHEN** 发送 POST `/api/customers` 且 name 非空
- **THEN** 返回 HTTP 200，响应 data 包含新建客户信息（含自动生成的 id 和 createTime），以及 tags 列表

#### Scenario: Name is empty
- **WHEN** 发送 POST `/api/customers` 且 name 为空
- **THEN** 返回 HTTP 400，响应包含校验错误信息

### Requirement: Update customer
系统 SHALL 允许通过 PUT `/api/customers/{id}` 更新客户信息。更新时 tagIds 字段采用全量替换策略。

#### Scenario: Successful update
- **WHEN** 发送 PUT `/api/customers/{id}` 且 id 存在、name 非空
- **THEN** 返回 HTTP 200，响应 data 包含更新后的客户信息（updateTime 自动刷新），以及 tags 列表

#### Scenario: Customer not found
- **WHEN** 发送 PUT `/api/customers/{id}` 且 id 不存在
- **THEN** 返回 HTTP 404

### Requirement: Delete customer
系统 SHALL 允许通过 DELETE `/api/customers/{id}` 删除客户。

#### Scenario: Successful deletion
- **WHEN** 发送 DELETE `/api/customers/{id}` 且 id 存在
- **THEN** 返回 HTTP 200

#### Scenario: Customer not found
- **WHEN** 发送 DELETE `/api/customers/{id}` 且 id 不存在
- **THEN** 返回 HTTP 404

### Requirement: Get customer by id
系统 SHALL 允许通过 GET `/api/customers/{id}` 查询单个客户详情，返回结果 SHALL 包含 tags 列表（含每个标签的 id、name、color）。

#### Scenario: Successful query
- **WHEN** 发送 GET `/api/customers/{id}` 且 id 存在
- **THEN** 返回 HTTP 200，响应 data 包含完整客户信息和 tags 列表

#### Scenario: Customer not found
- **WHEN** 发送 GET `/api/customers/{id}` 且 id 不存在
- **THEN** 返回 HTTP 404

### Requirement: List customers with pagination and filtering
系统 SHALL 允许通过 GET `/api/customers` 分页查询客户列表，支持按 name（模糊）、status、source、level、tagIds（AND 语义）筛选。客户列表页操作列 SHALL 包含"详情"入口，点击跳转至客户详情页。列表表格 SHALL 展示每个客户的标签。

#### Scenario: Pagination
- **WHEN** 发送 GET `/api/customers?page=0&size=10`
- **THEN** 返回 HTTP 200，响应 data 包含 content（客户数组，每项含 tags 列表）、totalElements、totalPages、number、size

#### Scenario: Filter by status
- **WHEN** 发送 GET `/api/customers?status=ACTIVE`
- **THEN** 返回 HTTP 200，data.content 只包含 status 为 ACTIVE 的客户

#### Scenario: Filter by name fuzzy match
- **WHEN** 发送 GET `/api/customers?name=张`
- **THEN** 返回 HTTP 200，data.content 包含 name 中含"张"的客户

### Requirement: Unified API response format
所有 API SHALL 返回统一 JSON 格式：`{ "code": "200", "message": "success", "data": ... }`。错误时 data 为 null，code 和 message 反映错误信息。

#### Scenario: Success response
- **WHEN** API 调用成功
- **THEN** 返回 `{ "code": "200", "message": "success", "data": <payload> }`

#### Scenario: Error response
- **WHEN** API 调用失败（如 404）
- **THEN** 返回 `{ "code": "404", "message": "<error description>", "data": null }`

### Requirement: Customer detail page
系统 SHALL 提供客户详情页，路由为 `/customers/:id`，展示客户基本信息（含标签展示与增删）和小记时间线。

#### Scenario: Access detail page
- **WHEN** 用户点击客户列表中的"详情"按钮
- **THEN** 跳转至 `/customers/{id}`，页面展示该客户的基本信息、标签和小记时间线

### Requirement: Note timeline on detail page
客户详情页 SHALL 展示小记时间线，按创建时间倒序排列，不同类型小记使用不同 icon 和颜色标识。页面顶部 SHALL 提供内联添加小记的表单。

#### Scenario: Display note with type icon
- **WHEN** 客户有小记且 category=PHONE_CALL
- **THEN** 时间线中该条小记显示电话沟通 icon，标签为"电话沟通"

#### Scenario: Add note inline
- **WHEN** 用户在详情页选择类型并输入内容后点击提交
- **THEN** 小记创建成功，时间线实时刷新，客户 lastFollowUp 自动更新

### Requirement: Customer entity fields
Customer 实体 SHALL 包含以下字段：id, name, phone, email, company, address, source, level, industry, website, contactPerson, lastFollowUp, status, remark, createTime, updateTime。其中 source、level、status 为枚举类型。标签信息通过关联表查询，不在 CustomerEntity 内存储。

#### Scenario: Enum storage
- **WHEN** 创建客户时 source=WEBSITE, level=VIP, status=ACTIVE
- **THEN** 数据库中对应字段以字符串 "WEBSITE"/"VIP"/"ACTIVE" 存储
