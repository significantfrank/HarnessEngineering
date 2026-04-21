## 1. Backend 项目初始化

- [x] 1.1 创建 crm-backend Maven 项目，配置 pom.xml（JDK 21、Spring Boot 3、Spring Data JPA、MySQL Driver、Lombok）
- [x] 1.2 创建 CrmApplication 启动类
- [x] 1.3 配置 application.yml 和 application-dev.yml（数据源、JPA、服务端口）

## 2. 后端 COLA 分包结构

- [x] 2.1 创建 adapter/web 包
- [x] 2.2 创建 app/customer/dto 包
- [x] 2.3 创建 domain/customer/{entity,enums,gateway,service} 包
- [x] 2.4 创建 infrastructure/customer/{gateway,repository} 包

## 3. 后端 Domain 层实现

- [x] 3.1 创建 CustomerEntity（@Entity，含全部字段及 JPA 注解）
- [x] 3.2 创建枚举类：CustomerSource、CustomerLevel、CustomerStatus
- [x] 3.3 创建 CustomerGatewayI 接口（CRUD 方法签名）
- [x] 3.4 创建 CustomerDomainService（占位，委托 Gateway）

## 4. 后端 Infrastructure 层实现

- [x] 4.1 创建 CustomerRepository（JPA Repository，含分页和筛选查询方法）
- [x] 4.2 创建 CustomerGatewayImpl（实现 CustomerGatewayI，调用 Repository）

## 5. 后端 App 层实现

- [x] 5.1 创建 CustomerDTO
- [x] 5.2 创建 CustomerService（CRUD 编排逻辑，调用 DomainService）

## 6. 后端 Adapter 层实现

- [x] 6.1 创建统一响应类 ApiResponse
- [x] 6.2 创建全局异常处理 GlobalExceptionHandler
- [x] 6.3 创建 CustomerController（5 个 REST 端点）

## 7. 数据库初始化

- [x] 7.1 创建 schema.sql（customer 表建表语句）

## 8. 前端项目初始化

- [x] 8.1 使用 Vite 创建 crm-frontend（Vue 3 + TypeScript）
- [x] 8.2 安装依赖：ant-design-vue、axios、vue-router、pinia
- [x] 8.3 配置 vite.config.ts（API 代理）
- [x] 8.4 创建目录结构：api、views、router、stores、types、utils

## 9. 前端基础配置

- [x] 9.1 配置 Ant Design Vue 按需引入
- [x] 9.2 封装 Axios 请求（api/request.ts），统一错误处理
- [x] 9.3 配置 Vue Router
- [x] 9.4 配置 Pinia Store

## 10. 前端 Customer 页面实现

- [x] 10.1 定义 TypeScript 类型（types/customer.ts）
- [x] 10.2 封装 Customer API（api/customer.ts）
- [x] 10.3 创建 Customer Store（stores/customer.ts）
- [x] 10.4 实现 CustomerList.vue（搜索栏 + 表格 + 分页）
- [x] 10.5 实现 CustomerForm.vue（新增/编辑 Modal 表单）
- [x] 10.6 实现删除确认交互
