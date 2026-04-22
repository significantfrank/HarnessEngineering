## ADDED Requirements

### Requirement: Backend project structure
后端项目 SHALL 采用 COLA 分包架构，包路径为 `com.harness.crm`，包含 adapter、app、domain、infrastructure 四个包，启动类为 `CrmApplication`。

#### Scenario: Package structure
- **WHEN** 查看 crm-backend 源码目录
- **THEN** 存在 adapter/web、app/customer/dto、domain/customer/{entity,enums,gateway,service}、infrastructure/customer/{gateway,repository} 包结构

### Requirement: Frontend project structure
前端项目 SHALL 基于 Vue 3 + TypeScript + Ant Design Vue + Vite，包含 api、views、router、stores、types、utils 目录。

#### Scenario: Project structure
- **WHEN** 查看 crm-frontend 源码目录
- **THEN** 存在 src/{api,views,router,stores,types,utils} 目录及对应文件

### Requirement: Customer list page
前端 SHALL 提供 Customer 列表页面，包含搜索栏（名称、状态、来源、等级）、数据表格、分页控件、新增按钮。

#### Scenario: Page rendering
- **WHEN** 访问客户列表页
- **THEN** 页面展示搜索栏、Ant Design Table、分页组件、新增按钮

### Requirement: Customer form modal
前端 SHALL 提供新增/编辑客户的 Modal 弹窗表单，包含所有 Customer 字段，提交后刷新列表。

#### Scenario: Create customer
- **WHEN** 点击新增按钮并填写表单提交
- **THEN** 调用 POST API，成功后关闭弹窗并刷新列表

#### Scenario: Edit customer
- **WHEN** 点击编辑按钮并修改表单提交
- **THEN** 调用 PUT API，成功后关闭弹窗并刷新列表

### Requirement: Delete customer confirmation
前端 SHALL 在删除客户前弹出确认框。

#### Scenario: Confirm deletion
- **WHEN** 点击删除按钮并确认
- **THEN** 调用 DELETE API，成功后刷新列表

#### Scenario: Cancel deletion
- **WHEN** 点击删除按钮后取消
- **THEN** 不发起请求
