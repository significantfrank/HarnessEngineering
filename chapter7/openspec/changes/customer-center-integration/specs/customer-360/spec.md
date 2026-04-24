## ADDED Requirements

### Requirement: 客户360视图API
系统SHALL提供`GET /api/customers/{id}/360`端点，后端组装customer-center主数据与CRM行为数据返回Customer360DTO。

#### Scenario: center可用时返回完整360视图
- **WHEN** 前端请求客户360视图且customer-center服务正常
- **THEN** 系统从center获取主数据（身份信息、账户状态、金融画像、持仓产品），从CRM获取行为数据（source/level/status/remark/公司信息/标签/跟进记录），合并返回Customer360DTO，degraded=false

#### Scenario: center不可用时返回降级360视图
- **WHEN** 前端请求客户360视图但customer-center服务不可用或超时
- **THEN** 系统用CRM本地保留的主数据（name/phone/email/idType/idNumber）填充身份信息，行为数据正常返回，degraded=true，金融画像和持仓产品字段为null/空

### Requirement: 360视图降级提示
当360视图处于降级模式时，SHALL在前端显示降级提示。

#### Scenario: 降级提示显示
- **WHEN** 360视图返回degraded=true
- **THEN** 前端在页面顶部显示警告提示"主数据服务暂时不可用，部分信息可能不是最新"

### Requirement: 360视图前端Tab化展示
客户360视图SHALL使用Tab布局展示不同维度的数据。

#### Scenario: 身份画像Tab展示center主数据
- **WHEN** 用户查看客户360视图的身份画像Tab
- **THEN** 展示center的主数据：性别、生日、职业、证件信息、账户状态、会员等级、认证等级、风险偏好、收入范围、AUM、可用余额、总收益、持仓产品列表

#### Scenario: 跟进记录Tab同步加载
- **WHEN** 用户打开客户360视图
- **THEN** 跟进记录Tab的数据随360视图同步加载返回，无需额外请求

#### Scenario: 商机Tab异步加载
- **WHEN** 用户点击商机Tab
- **THEN** 前端异步请求`GET /api/opportunities?customerId={id}`加载数据

#### Scenario: 订单Tab异步加载
- **WHEN** 用户点击订单Tab
- **THEN** 前端异步请求`GET /api/orders?customerId={id}`加载数据

### Requirement: Customer360DTO数据结构
系统SHALL定义Customer360DTO包含center主数据、CRM行为数据、降级标记。

#### Scenario: 360 DTO包含center独有字段
- **WHEN** 组装Customer360DTO且center可用
- **THEN** DTO包含gender、birthday、occupation、accountStatus、memberLevel、authLevel、riskProfile、incomeRange、aum、availableBalance、totalReturn、holdingProducts等center独有字段

#### Scenario: 360 DTO包含降级标记
- **WHEN** 组装Customer360DTO
- **THEN** DTO包含degraded布尔字段，center不可用时为true

### Requirement: 客户详情页路由不变
客户360视图SHALL复用现有客户详情页路由`/customers/:id`，替换原CustomerDetail.vue。

#### Scenario: 从列表点击详情
- **WHEN** 用户在客户列表点击"详情"按钮
- **THEN** 跳转到`/customers/:id`，展示客户360视图（非原详情页）

### Requirement: 客户列表页不接入center
客户列表页SHALL保持仅从CRM本地数据库查询，不调用customer-center。

#### Scenario: 列表页查询
- **WHEN** 用户在客户列表页执行搜索
- **THEN** 系统仅查询CRM本地数据，不调用center API
