## Why

客户跟进是CRM的核心场景，但当前系统只能记录"最后跟进时间"和静态备注，无法回溯跟进历史。销售人员需要按时间线记录每次沟通的内容和方式，形成完整的客户互动记录。

## What Changes

- 新增客户小记（CustomerNote）实体，属于客户域，记录跟进类型（电话/拜访/邮件/微信/其他）和内容
- 新增小记 CRUD API，以嵌套资源形式挂载在 `/api/customers/{id}/notes` 下，只增不删
- 新增小记时自动更新客户的 `lastFollowUp` 字段，逻辑封装在领域对象中
- 新增客户详情页（`/customers/:id`），展示客户基本信息和小记时间线
- 客户列表页操作列增加"详情"入口

## Capabilities

### New Capabilities
- `customer-note`: 客户小记的新增、查询能力，以及与客户 lastFollowUp 的联动逻辑

### Modified Capabilities
- `customer-crud`: 客户列表页增加"详情"入口，新增客户详情页路由

## Impact

- 后端：customer 域内新增 entity、enum、gateway、repository、service、controller 端点；schema.sql 新增 customer_note 表
- 前端：新增 CustomerDetail.vue 页面和路由；CustomerList.vue 增加详情入口；新增小记相关 API、类型、store
