## Why

项目需要一个前后端分离的 CRM 系统基础框架。本次迭代以搭建框架为主，仅实现 Customer 的简单 CRUD，为后续业务功能迭代奠定架构基础。

## What Changes

- 创建 `crm-frontend` 项目：Vue 3 + TypeScript + Ant Design Vue，包含 Customer 列表页（搜索/分页）、新增/编辑弹窗
- 创建 `crm-backend` 项目：JDK 21 + Spring Boot 3 + Maven + MySQL，采用 COLA 分包架构（adapter / app / domain / infrastructure），实现 Customer CRUD REST API
- 定义 Customer 实体完整字段集：name, phone, email, company, address, source, level, industry, website, contactPerson, lastFollowUp, status, remark
- 定义统一 API 响应格式

## Capabilities

### New Capabilities
- `customer-crud`: Customer 实体的增删改查，涵盖后端 REST API 及前端页面交互
- `project-scaffold`: 前后端项目结构搭建、依赖配置、数据库初始化

### Modified Capabilities

## Impact

- 新增 `crm-frontend` 目录及完整 Vue 3 项目
- 新增 `crm-backend` 目录及完整 Spring Boot 3 项目
- 新增 MySQL 数据库及 `customer` 表
- 前后端通过 REST API（`/api/customers`）通信
