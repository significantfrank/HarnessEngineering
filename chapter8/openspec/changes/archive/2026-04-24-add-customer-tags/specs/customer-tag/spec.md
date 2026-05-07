## ADDED Requirements

### Requirement: Create tag
系统 SHALL 允许通过 POST `/api/tags` 创建标签，请求体包含 name（非空，唯一）和 color（非空，HEX 格式），返回创建后的 TagDTO。

#### Scenario: Successful creation
- **WHEN** 发送 POST `/api/tags` 且 name 非空、color 非空、name 不重复
- **THEN** 返回 HTTP 200，响应 data 包含新建标签信息（含自动生成的 id 和 createTime）

#### Scenario: Duplicate tag name
- **WHEN** 发送 POST `/api/tags` 且 name 已存在
- **THEN** 返回 HTTP 200，响应 code 为 "400"，message 提示标签名称已存在

### Requirement: Update tag
系统 SHALL 允许通过 PUT `/api/tags/{id}` 更新标签的名称和颜色。

#### Scenario: Successful update
- **WHEN** 发送 PUT `/api/tags/{id}` 且 id 存在、name 非空、color 非空
- **THEN** 返回 HTTP 200，响应 data 包含更新后的标签信息（updateTime 自动刷新）

#### Scenario: Tag not found
- **WHEN** 发送 PUT `/api/tags/{id}` 且 id 不存在
- **THEN** 返回 HTTP 200，响应 code 为 "404"

### Requirement: Delete tag with usage protection
系统 SHALL 允许通过 DELETE `/api/tags/{id}` 删除标签，但标签被客户使用时 SHALL 拒绝删除并提示使用数量。

#### Scenario: Delete unused tag
- **WHEN** 发送 DELETE `/api/tags/{id}` 且该标签未被任何客户使用
- **THEN** 返回 HTTP 200，标签删除成功，关联数据同步清除

#### Scenario: Delete tag in use
- **WHEN** 发送 DELETE `/api/tags/{id}` 且该标签被 N 个客户使用
- **THEN** 返回 HTTP 200，响应 code 为 "400"，message 提示"该标签正在被 N 个客户使用，无法删除"

### Requirement: List all tags
系统 SHALL 允许通过 GET `/api/tags` 获取所有标签列表，每个标签包含 id、name、color、customerCount（使用该标签的客户数量）。

#### Scenario: Get tag list
- **WHEN** 发送 GET `/api/tags`
- **THEN** 返回 HTTP 200，响应 data 为标签数组，每项包含 id、name、color、customerCount、createTime、updateTime

### Requirement: Tag entity fields
Tag 实体 SHALL 包含以下字段：id, name（UNIQUE, NOT NULL, max 50）, color（NOT NULL, max 20, HEX 格式）, createTime, updateTime。

#### Scenario: Unique name constraint
- **WHEN** 尝试创建 name 相同的标签
- **THEN** 数据库 UNIQUE 约束阻止重复，返回错误

### Requirement: Customer tag association
系统 SHALL 支持客户与标签的 M:N 关联。创建或更新客户时，可通过 tagIds 字段指定关联的标签 ID 列表。查询客户时，返回结果 SHALL 包含 tags 列表（含每个标签的 id、name、color）。

#### Scenario: Create customer with tags
- **WHEN** 发送 POST `/api/customers` 且 tagIds 包含 [1, 3]
- **THEN** 客户创建成功，返回的 CustomerDTO 中 tags 包含 id=1 和 id=3 的标签信息

#### Scenario: Update customer tags (full replace)
- **WHEN** 发送 PUT `/api/customers/{id}` 且 tagIds 变更为 [1, 5]
- **THEN** 客户更新后标签关联为 id=1 和 id=5（原有其他关联清除）

#### Scenario: Get customer with tags
- **WHEN** 发送 GET `/api/customers/{id}`
- **THEN** 返回的 CustomerDTO 中 tags 包含该客户所有关联标签的 id、name、color

### Requirement: Filter customers by tags (AND semantics)
系统 SHALL 支持通过 GET `/api/customers?tagIds=1,3` 按标签筛选客户，多个标签为 AND 关系（客户必须同时拥有所有指定标签）。

#### Scenario: Filter by single tag
- **WHEN** 发送 GET `/api/customers?tagIds=1`
- **THEN** 返回的客户列表中每个客户都拥有 id=1 的标签

#### Scenario: Filter by multiple tags (AND)
- **WHEN** 发送 GET `/api/customers?tagIds=1,3`
- **THEN** 返回的客户列表中每个客户同时拥有 id=1 和 id=3 的标签

### Requirement: Tag management page
系统 SHALL 提供标签管理页面，路由为 `/tags`，位于"客户管理"菜单下作为子菜单。页面展示标签列表（名称、颜色、使用客户数、操作），支持新建、编辑、删除操作。

#### Scenario: Access tag management
- **WHEN** 用户点击客户管理下的"标签管理"菜单
- **THEN** 跳转至 `/tags`，展示标签列表

#### Scenario: Delete tag in use from management page
- **WHEN** 用户尝试删除一个被使用的标签
- **THEN** 提示"该标签正在被 N 个客户使用，无法删除"

### Requirement: TagSelect component with inline creation
系统 SHALL 提供 TagSelect 组件，支持下拉选择已有标签和内联创建新标签。内联创建时 SHALL 弹出颜色选择弹窗，创建成功后自动选上该标签。

#### Scenario: Select existing tag
- **WHEN** 用户在 TagSelect 下拉中选择已有标签
- **THEN** 该标签被添加到选中列表

#### Scenario: Create new tag inline
- **WHEN** 用户输入不匹配任何已有标签的文本并点击"创建"
- **THEN** 弹出颜色选择弹窗，选择颜色后创建标签，自动选上
