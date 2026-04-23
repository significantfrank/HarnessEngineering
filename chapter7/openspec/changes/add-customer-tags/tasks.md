## 1. 数据库

- [ ] 1.1 在 schema.sql 中新增 `tag` 表（id, name UNIQUE, color, create_time, update_time）
- [ ] 1.2 在 schema.sql 中新增 `customer_tag` 关联表（id, customer_id, tag_id, create_time, 联合唯一索引, tag_id 索引）

## 2. 后端 - Tag 域

- [ ] 2.1 创建 TagEntity（id, name, color, createTime, updateTime, prePersist/preUpdate）
- [ ] 2.2 创建 CustomerTagRelEntity（id, customerId, tagId, createTime）
- [ ] 2.3 创建 TagGatewayI 接口（save, findById, findAll, findByName, deleteById）
- [ ] 2.4 创建 CustomerTagGatewayI 接口（save, deleteByCustomerId, findByCustomerId, countByTagId）
- [ ] 2.5 创建 TagDTO（id, name, color, customerCount, createTime, updateTime）
- [ ] 2.6 创建 TagService（create, update, deleteById 含使用校验, findAll, findById）
- [ ] 2.7 创建 TagController（/api/tags CRUD 端点）
- [ ] 2.8 创建 TagRepository（JpaRepository）
- [ ] 2.9 创建 CustomerTagRelRepository（JpaRepository, countByTagId, deleteByCustomerId, findByCustomerId）
- [ ] 2.10 创建 TagGatewayImpl 和 CustomerTagGatewayImpl

## 3. 后端 - 改造 Customer 域

- [ ] 3.1 CustomerDTO 增加 tags（List<TagDTO>）和 tagIds（List<Long>）字段
- [ ] 3.2 CustomerGatewayI.findByConditions 签名增加 List<Long> tagIds 参数
- [ ] 3.3 CustomerService.create 增加 tagIds 处理逻辑（校验标签存在、批量创建关联）
- [ ] 3.4 CustomerService.update 增加 tagIds 全量替换逻辑（deleteByCustomerId + 批量创建）
- [ ] 3.5 CustomerService.toDTO 增加 tags 加载逻辑（查询 customer_tag 关联 + tag 信息）
- [ ] 3.6 CustomerGatewayImpl.findByConditions Specification 增加 tagIds EXISTS 子查询（AND 语义）
- [ ] 3.7 CustomerController.list 接口增加 tagIds 请求参数

## 4. 后端 - 单元测试

- [ ] 4.1 TagControllerIntegrationTest：标签 CRUD 测试（创建、更新、删除未使用标签、删除使用中标签被拒、名称重复创建失败）
- [ ] 4.2 CustomerControllerIntegrationTest：增加标签关联测试（创建客户带标签、更新客户标签、按标签筛选、按多标签 AND 筛选）

## 5. 前端 - 基础设施

- [ ] 5.1 创建 types/tag.ts（Tag 接口定义）
- [ ] 5.2 创建 api/tag.ts（getTags, createTag, updateTag, deleteTag）
- [ ] 5.3 创建 stores/tag.ts（Pinia store：tags 列表、fetchTags、addTag、editTag、removeTag）
- [ ] 5.4 types/common.ts 中 Customer 增加 tags 字段，CustomerQuery 增加 tagIds
- [ ] 5.5 api/customer.ts 请求参数支持 tagIds
- [ ] 5.6 router/index.ts 新增 /tags 路由，客户管理菜单下增加"标签管理"子菜单

## 6. 前端 - 标签管理页面

- [ ] 6.1 创建 views/tag/TagList.vue（标签表格：名称、颜色、使用客户数、操作列编辑/删除）
- [ ] 6.2 标签新建/编辑弹窗（名称输入 + 颜色选择）

## 7. 前端 - TagSelect 组件

- [ ] 7.1 创建 components/TagSelect.vue（下拉多选已有标签，输入不匹配时显示"创建 xxx"，点击弹出颜色选择弹窗，创建后自动选上）

## 8. 前端 - 改造客户页面

- [ ] 8.1 CustomerList.vue：筛选栏增加标签多选下拉，表格增加标签列（展示彩色标签）
- [ ] 8.2 CustomerForm.vue：集成 TagSelect 组件，提交时携带 tagIds
- [ ] 8.3 CustomerDetail.vue：基本信息区域增加标签展示与增删（TagSelect + 直接操作）
