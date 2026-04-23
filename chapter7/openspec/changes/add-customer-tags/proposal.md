## Why

客户管理目前只能按固定枚举维度（状态、来源、等级）筛选，无法按业务特征灵活分类。销售人员需要给客户打上如"大客户""需回访""华东"等标签，并通过多标签组合快速定位目标客户群。标签功能是CRM系统的基础能力，直接影响日常使用效率。

## What Changes

- 新增标签（Tag）CRUD：支持创建、编辑、删除标签，标签包含名称和颜色
- 新增客户-标签关联：客户可以被打上多个标签，支持在创建/编辑客户时选择标签
- 标签混合模式：预定义常用标签 + 用户可自由创建新标签
- 客户列表增加标签筛选：支持多标签AND组合筛选
- 标签删除保护：标签被客户使用时拒绝删除，提示使用数量
- 新增标签管理页面：在"客户管理"菜单下新增"标签管理"子页面
- 客户列表、详情、表单页面展示和操作标签

## Capabilities

### New Capabilities
- `customer-tag`: 客户标签的完整生命周期管理，包括标签CRUD、客户-标签关联管理、多标签AND筛选

### Modified Capabilities
- `customer-crud`: 客户创建/编辑需支持标签关联，客户查询需支持按标签筛选，客户展示需显示标签信息

## Impact

- **数据库**: 新增 `tag` 表和 `customer_tag` 关联表
- **后端**: 新增 tag 域（entity/gateway/service/controller/dto），改造 customer 域（GatewayI/Service/DTO/GatewayImpl/Controller）
- **API**: 新增 `/api/tags` CRUD 端点，`/api/customers` 查询参数增加 `tagIds`
- **前端**: 新增标签管理页 + TagSelect组件，改造客户列表/详情/表单页面，路由新增 `/tags`
