## ADDED Requirements

### Requirement: Order CRUD
系统 SHALL 提供订单的创建、查询、更新和删除功能。订单创建时 customerId 和 opportunityId MUST NOT 为空。订单状态默认为 PENDING。

#### Scenario: 创建订单
- **WHEN** 用户提交订单数据（customerId非空、opportunityId非空、totalAmount非空）
- **THEN** 系统创建订单，status默认为PENDING，自动生成orderNo，设置createTime和updateTime，返回创建的订单

#### Scenario: 创建订单缺少必填字段
- **WHEN** 用户提交订单数据且customerId或opportunityId或totalAmount为空
- **THEN** 系统返回校验错误

#### Scenario: 查询订单列表
- **WHEN** 用户按条件（orderNo精确、status、customerId、opportunityId）分页查询订单
- **THEN** 系统返回匹配的分页结果

#### Scenario: 更新订单
- **WHEN** 用户修改订单信息并提交
- **THEN** 系统更新订单，刷新updateTime，orderNo MUST NOT 被修改

#### Scenario: 删除订单
- **WHEN** 用户删除指定ID的订单
- **THEN** 系统删除该订单记录

### Requirement: Order 订单号生成
系统 SHALL 自动生成订单号，格式为 `ORD-yyyyMMdd-NNN`，其中 NNN 为当日三位序列号。订单号 MUST 全局唯一。

#### Scenario: 首个当日订单
- **WHEN** 当日无已有订单时创建订单
- **THEN** 系统生成订单号如 ORD-20260422-001

#### Scenario: 后续当日订单
- **WHEN** 当日已有订单（最大序列号为005）时创建订单
- **THEN** 系统生成订单号如 ORD-20260422-006

### Requirement: Order 状态流转
订单 SHALL 按以下状态流转：PENDING → CONFIRMED → PROCESSING → COMPLETED（终态）；PENDING 或 CONFIRMED 状态可流转至 CANCELLED（终态）。COMPLETED 和 CANCELLED 为终态，MUST NOT 再变更状态。

#### Scenario: 正向推进状态
- **WHEN** 用户将订单状态从 PENDING 改为 CONFIRMED
- **THEN** 系统更新状态成功

#### Scenario: 取消订单
- **WHEN** 用户将PENDING或CONFIRMED的订单状态改为CANCELLED
- **THEN** 系统更新状态成功，该订单不可再变更状态

#### Scenario: 已完成订单不可变更
- **WHEN** 用户尝试修改已COMPLETED订单的状态
- **THEN** 系统返回错误，拒绝操作
