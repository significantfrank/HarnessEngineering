## MODIFIED Requirements

### Requirement: List customers with pagination and filtering
系统 SHALL 允许通过 GET `/api/customers` 分页查询客户列表，支持按 name（模糊）、status、source、level 筛选。客户列表页操作列 SHALL 包含"详情"入口，点击跳转至客户详情页。

#### Scenario: Pagination
- **WHEN** 发送 GET `/api/customers?page=0&size=10`
- **THEN** 返回 HTTP 200，响应 data 包含 content（客户数组）、totalElements、totalPages、number、size

#### Scenario: Filter by status
- **WHEN** 发送 GET `/api/customers?status=ACTIVE`
- **THEN** 返回 HTTP 200，data.content 只包含 status 为 ACTIVE 的客户

#### Scenario: Filter by name fuzzy match
- **WHEN** 发送 GET `/api/customers?name=张`
- **THEN** 返回 HTTP 200，data.content 包含 name 中含"张"的客户

## ADDED Requirements

### Requirement: Customer detail page
系统 SHALL 提供客户详情页，路由为 `/customers/:id`，展示客户基本信息和小记时间线。客户列表页操作列 SHALL 增加"详情"按钮，点击跳转至详情页。

#### Scenario: Access detail page
- **WHEN** 用户点击客户列表中的"详情"按钮
- **THEN** 跳转至 `/customers/{id}`，页面展示该客户的基本信息和小记时间线

### Requirement: Note timeline on detail page
客户详情页 SHALL 展示小记时间线，按创建时间倒序排列，不同类型小记使用不同 icon 和颜色标识。页面顶部 SHALL 提供内联添加小记的表单。

#### Scenario: Display note with type icon
- **WHEN** 客户有小记且 category=PHONE_CALL
- **THEN** 时间线中该条小记显示电话沟通 icon，标签为"电话沟通"

#### Scenario: Add note inline
- **WHEN** 用户在详情页选择类型并输入内容后点击提交
- **THEN** 小记创建成功，时间线实时刷新，客户 lastFollowUp 自动更新
