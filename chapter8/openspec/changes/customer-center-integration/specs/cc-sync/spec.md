## ADDED Requirements

### Requirement: 创建客户时同步主数据到customer-center
CRM创建客户时，SHALL将主数据字段（name, phone, email, idType, idNumber）同步到customer-center。id_number为与center的关联键。本地先save（cc_sync_status=PENDING），事务提交后调用center API，成功后更新cc_sync_status=SYNCED。

#### Scenario: 创建客户成功且center同步成功
- **WHEN** 用户通过CustomerForm提交新客户（含idType和idNumber）
- **THEN** 系统先在本地保存Customer记录（cc_sync_status=PENDING），然后调用center创建/同步，成功后将cc_sync_status更新为SYNCED

#### Scenario: 创建客户成功但center同步失败
- **WHEN** 本地save成功后，调用center API连续3次重试（退避1s/2s/4s）均失败
- **THEN** 系统删除本地Customer记录和标签关联，向前端返回错误

#### Scenario: 创建客户时center不可用
- **WHEN** center服务完全不可达，3次重试均超时
- **THEN** 系统删除本地Customer记录和标签关联，向前端返回错误

### Requirement: 更新客户时同步主数据到customer-center
CRM更新客户时，SHALL将主数据字段同步到customer-center。本地先update，事务提交后调用center PUT API。

#### Scenario: 更新客户成功且center同步成功
- **WHEN** 用户修改客户信息并提交
- **THEN** 系统先在本地更新Customer记录，然后调用center更新主数据，成功后cc_sync_status更新为SYNCED

#### Scenario: 更新客户成功但center同步失败
- **WHEN** 本地update成功后，调用center API连续3次重试均失败
- **THEN** 系统将cc_sync_status标记为FAILED，本地数据保留不回滚，向前端返回成功（附带同步状态提示）

### Requirement: 重复客户自动关联
当customer-center中已存在相同idNumber的客户时，SHALL关联已有记录而非新建。

#### Scenario: 创建客户时idNumber在center已存在
- **WHEN** 用户创建客户，idNumber在center已有记录
- **THEN** 系统先查询center全量列表匹配idNumber，找到后调用PUT更新已有记录

#### Scenario: 创建客户时idNumber在center不存在
- **WHEN** 用户创建客户，idNumber在center无记录
- **THEN** 系统调用POST创建新记录

### Requirement: 线索转化时idType和idNumber必填
从线索转化为客户时，SHALL要求填写idType和idNumber，并同步到customer-center。

#### Scenario: 线索转化成功且center同步成功
- **WHEN** 用户填写idType、idNumber和机会信息后提交转化
- **THEN** 系统在事务内创建Customer（cc_sync_status=PENDING）+ Opportunity + 更新Lead状态为CONVERTED，事务提交后调用center同步，成功后更新cc_sync_status=SYNCED

#### Scenario: 线索转化成功但center同步失败
- **WHEN** 本地事务提交后，调用center API连续3次重试均失败
- **THEN** 系统删除Customer和Opportunity记录，恢复Lead状态（status恢复原值，customerId置null），向前端返回错误

### Requirement: 同步状态追踪
系统SHALL通过cc_sync_status字段追踪每个客户的center同步状态。

#### Scenario: 查看客户同步状态
- **WHEN** 查询客户详情或列表
- **THEN** 返回的cc_sync_status字段为PENDING/SYNCED/FAILED之一

#### Scenario: 查看同步失败客户列表
- **WHEN** 管理员需要排查同步问题
- **THEN** 可通过筛选cc_sync_status=FAILED获取同步失败的客户列表

### Requirement: CustomerCenterGatewayI接口
系统SHALL在domain层定义CustomerCenterGatewayI接口，由infrastructure层实现，遵循依赖倒置原则。

#### Scenario: Gateway接口定义
- **WHEN** 需要与customer-center交互
- **THEN** 通过CustomerCenterGatewayI的createOrSync/update/findByIdNumber/isAvailable方法调用，不直接依赖HTTP实现细节

### Requirement: 退避重试机制
调用customer-center API失败时，SHALL进行退避重试，最多3次，间隔1s/2s/4s。

#### Scenario: 首次调用失败后重试
- **WHEN** 第一次调用center API失败
- **THEN** 等待1秒后重试

#### Scenario: 第二次调用失败后重试
- **WHEN** 第二次调用center API失败
- **THEN** 等待2秒后重试

#### Scenario: 第三次调用失败
- **WHEN** 第三次调用center API失败
- **THEN** 不再重试，根据创建/更新场景执行对应失败策略
