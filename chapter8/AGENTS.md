# AGENTS.md

## Project Layout
This is a monorepo CRM project， 前端代码在crm-frontend, 后端代码在crm-backend

## 编码规范务必遵守
生成代码的时候，必须参考: @docs/coding-guideline.md

## 测试规范务必遵守
生成测试代码的时候，必须参考: @docs/test-guideline.md

## 阅读代码守则
不需要Glob代码仓，可以直接访问 @docs/codebase-detail.md 

## 外部系统依赖
本系统依赖客户主数据服务（customer-center），涉及系统集成，务必参考其API：@docs/customer-center-api.yaml

## Key decisions:
- **Entity = JPA entity**: `CustomerEntity` has `@Entity` directly, no separate DO layer
- **Gateway interface only**: `CustomerGatewayI` in domain, implemented in infrastructure (dependency inversion)
- **No Service interface**: `CustomerService` is a concrete class
- **Single DTO**: `CustomerDTO` for create, update, and response

## API Conventions
- Base path: `/api/customers`
- All responses use `{ "code": "200", "message": "success", "data": ... }`
- **Errors also return HTTP 200** with error codes in the body (e.g., `"code": "404"`)
- Enums stored as STRING in both JPA and JSON
- Pagination is 0-based (Spring Data default)

## Frontend-Backend Integration
- Vite proxies `/api` → `http://localhost:8080` in dev
- Axios interceptor unwraps `ApiResponse`: checks `code !== '200'` and returns `res` (so `res.data` is the payload)
- Page index sent to backend is 0-based; displayed as 1-based in Ant Design table
