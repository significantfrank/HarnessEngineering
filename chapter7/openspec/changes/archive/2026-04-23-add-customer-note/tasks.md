## 1. 后端 - 数据层

- [x] 1.1 在 schema.sql 中新增 customer_note 表定义
- [x] 1.2 新增 NoteCategory 枚举（domain/customer/enums/NoteCategory.java）
- [x] 1.3 新增 CustomerNoteEntity（domain/customer/entity/CustomerNoteEntity.java），包含 updateCustomerLastFollowUp 领域方法

## 2. 后端 - Gateway & Repository

- [x] 2.1 新增 CustomerNoteGatewayI 接口（domain/customer/gateway/CustomerNoteGatewayI.java）
- [x] 2.2 新增 CustomerNoteRepository（infrastructure/customer/repository/CustomerNoteRepository.java）
- [x] 2.3 新增 CustomerNoteGatewayImpl（infrastructure/customer/gateway/CustomerNoteGatewayImpl.java）

## 3. 后端 - 应用层 & 适配层

- [x] 3.1 新增 CustomerNoteDTO（app/customer/dto/CustomerNoteDTO.java）
- [x] 3.2 新增 CustomerNoteService（app/customer/CustomerNoteService.java），实现新增小记（含 lastFollowUp 联动）和分页查询
- [x] 3.3 在 CustomerController 中新增小记端点：POST /api/customers/{id}/notes、GET /api/customers/{id}/notes

## 4. 后端 - 单元测试

- [x] 4.1 新增 CustomerNoteControllerIntegrationTest，包含新增小记 Happy Case 和 Bad Case
- [x] 4.2 运行全部测试确保通过

## 5. 前端 - 类型和 API

- [x] 5.1 在 types/common.ts 中新增 CustomerNote、NoteCategory 类型定义
- [x] 5.2 新增 api/customerNote.ts，封装小记 API 调用
- [x] 5.3 新增 stores/customerNote.ts，封装小记状态管理

## 6. 前端 - 客户详情页

- [x] 6.1 新增 CustomerDetail.vue，包含客户基本信息展示和小记时间线
- [x] 6.2 在路由中新增 /customers/:id 路由
- [x] 6.3 在 CustomerList.vue 操作列增加"详情"按钮
