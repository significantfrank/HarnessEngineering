## Why

CRM系统需要与客户主数据服务(customer-center)打通，实现客户数据的跨系统一致性。当前CRM自成一体，客户身份信息（姓名、电话、邮箱、证件）与金融主数据服务脱节，导致数据孤岛和重复维护。同时，销售人员在CRM中无法看到客户在主数据侧的金融画像（AUM、持仓、风险偏好等），错失交叉销售机会。

## What Changes

- 创建/更新客户时，同步客户主数据（name, phone, email, idType, idNumber）到customer-center
- CustomerEntity新增字段：cc_sync_status（同步状态）、id_type、id_number（id_number为与center的关联键）
- LeadEntity新增字段：id_type、id_number（线索转化时必填）
- 新增CustomerCenterGatewayI接口及RestTemplate实现，封装与customer-center的HTTP交互
- 创建/更新流程拆为两步事务：本地事务提交后再调用center，同步失败创建则回滚、更新则标记FAILED
- 客户详情页改造为客户360视图：后端从customer-center拉取主数据+本地拉取行为数据，组装返回
- 新增API `GET /api/customers/{id}/360`，center不可用时降级为本地数据并标记degraded
- 线索转化(ConvertDialog)表单新增idType/idNumber必填项
- 客户创建表单(CustomerForm)新增idType/idNumber字段

## Capabilities

### New Capabilities
- `cc-sync`: 与customer-center的数据同步能力，包括创建/更新时的主数据推送、重复客户检测（先查后写）、退避重试、同步状态追踪，id_number为跨系统关联键
- `customer-360`: 客户360视图能力，后端组装center主数据与CRM行为数据，降级策略，前端Tab化展示（身份画像/跟进记录/商机/订单）

### Modified Capabilities
（无既有spec需要修改）

## Impact

- **数据库**: customer表+cc_sync_status, +id_type, +id_number(UNIQUE,关联center); lead表+id_type, +id_number
- **后端Domain**: CustomerEntity、LeadEntity新增字段；新增CustomerCenterGatewayI接口；新增CcSyncStatus枚举
- **后端Infrastructure**: 新增CustomerCenterGatewayImpl（RestTemplate）；CustomerGatewayImpl新增updateSyncStatus方法
- **后端App**: CustomerService.create/update重写（两步事务）；LeadService.convert重写（加center同步+回滚扩大）；新增Customer360DTO
- **后端Adapter**: CustomerController新增get360端点；LeadController convert参数变更
- **前端**: CustomerForm.vue/ConvertDialog.vue新增表单项；CustomerDetail.vue重构为360视图；api/customer.ts新增getCustomer360
- **配置**: application-dev.yml新增customer-center.base-url
- **依赖**: 无新依赖（使用已有RestTemplate）
- **长期债务**: center需提供idNumber精确查询API（当前遍历全量匹配）；cc_sync_status=FAILED的后台补偿机制待建设
