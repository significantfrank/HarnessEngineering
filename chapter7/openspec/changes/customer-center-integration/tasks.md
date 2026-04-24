## 1. 数据库与Domain层变更

- [x] 1.1 customer表新增字段: cc_sync_status VARCHAR(20) DEFAULT 'PENDING', id_type VARCHAR(20), id_number VARCHAR(50) UNIQUE
- [x] 1.2 lead表新增字段: id_type VARCHAR(20), id_number VARCHAR(50)
- [x] 1.3 CustomerEntity新增ccSyncStatus(CcSyncStatus)、idType(String)、idNumber(String,唯一约束)字段及JPA注解
- [x] 1.4 新增CcSyncStatus枚举(PENDING/SYNCED/FAILED)，放在domain/customer/enums/下
- [x] 1.5 LeadEntity新增idType(String)、idNumber(String)字段及JPA注解
- [x] 1.6 CustomerGatewayI新增updateSyncStatus(Long id, CcSyncStatus status)方法

## 2. CustomerCenterGateway实现

- [x] 2.1 新增domain/customer/gateway/CustomerCenterGatewayI接口: createOrSync/update/findByIdNumber/isAvailable方法
- [x] 2.2 新增infrastructure/customer/gateway/CustomerCenterGatewayImpl，RestTemplate实现，base-url从配置读取
- [x] 2.3 实现createOrSync: 先GET全量列表按idNumber匹配，存在则PUT更新，不存在则POST创建
- [x] 2.4 实现update(idNumber, name, phone, email, idType): 先按idNumber查center列表找到center-id，再PUT center/api/customers/{center-id}
- [x] 2.5 实现findByIdNumber(idNumber): GET center/api/customers全量列表按idNumber匹配，返回Optional<CustomerCenterData>
- [x] 2.6 实现isAvailable: 探测center是否可达
- [x] 2.7 实现退避重试逻辑: 失败后1s/2s/4s重试，最多3次
- [x] 2.8 新增CustomerCenterData DTO封装center返回的完整客户数据
- [x] 2.9 application-dev.yml新增customer-center.base-url=http://localhost:8081配置

## 3. CustomerService重构（两步事务）

- [x] 3.1 CustomerService.create重写: 事务1本地save(PENDING)+标签 → 事务外调center同步 → 更新cc_sync_status=SYNCED或回滚
- [x] 3.2 CustomerService.update重写: 事务1本地update+标签 → 事务外调center同步 → 更新状态或标记FAILED
- [x] 3.3 CustomerDTO新增idType、idNumber、ccSyncStatus字段
- [x] 3.4 toEntity/updateEntity/toDTO方法适配新字段
- [x] 3.5 CustomerGatewayImpl实现updateSyncStatus方法

## 4. LeadService转化流程改造

- [x] 4.1 LeadConvertDTO新增idType(@NotBlank)和idNumber(@NotBlank)字段
- [x] 4.2 LeadService.convert重写: 事务1创建Customer(PENDING)+Opportunity+更新Lead → 事务外调center同步
- [x] 4.3 转化失败回滚逻辑: 删除Customer+Opportunity，恢复Lead原状态和customerId

## 5. Customer360 API

- [x] 5.1 新增Customer360DTO: 包含CRM行为字段+center主数据字段+holdingProducts+degraded标记
- [x] 5.2 新增HoldingProductDTO: productType/productCode/productName/amount
- [x] 5.3 CustomerService新增find360ById方法: 用idNumber查center主数据(降级用本地) + CRM行为数据 + Tags + Notes → 组装Customer360DTO
- [x] 5.4 CustomerController新增GET /api/customers/{id}/360端点

## 6. 前端变更

- [x] 6.1 types/common.ts: Customer接口新增idType/idNumber/ccSyncStatus字段
- [x] 6.2 api/customer.ts: 新增getCustomer360(id)方法
- [x] 6.3 CustomerForm.vue: 新增idType(证件类型下拉)和idNumber(证件号码输入)表单项
- [x] 6.4 ConvertDialog.vue: 新增idType和idNumber必填表单项
- [x] 6.5 CustomerDetail.vue重构为Customer360.vue: 顶部概览+降级提示+Tab(身份画像/跟进记录/商机/订单)
- [x] 6.6 360视图身份画像Tab: 展示center主数据(性别/生日/职业/证件/账户/金融画像/持仓)
- [x] 6.7 360视图跟进记录Tab: 同步加载的Notes timeline
- [x] 6.8 360视图商机Tab: 异步加载Opportunities
- [x] 6.9 360视图订单Tab: 异步加载Orders
- [x] 6.10 router更新: /customers/:id指向Customer360.vue

## 7. 测试适配

- [x] 7.1 CustomerControllerIntegrationTest适配: 创建/更新请求需含idType/idNumber
- [x] 7.2 LeadControllerIntegrationTest适配: 转化请求需含idType/idNumber
- [x] 7.3 新增CustomerCenterGatewayImplTest: mock RestTemplate测试createOrSync/update/findByIdNumber/重试/降级
- [x] 7.4 新增Customer360ControllerTest: 测试正常组装和降级场景
