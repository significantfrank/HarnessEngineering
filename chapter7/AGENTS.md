# AGENTS.md

## Project Layout
This is a CRM project，Using COLA by Packages Architecture。
前端代码在crm-frontend, 后端代码在crm-backend

## 编码规范务必遵守
1. 核心业务代码必须添加中文注释
2. 默认不要在DomainService写代码，让app直接操作gateway去完成CRUD，除非很有必要，征求我同意后可添加
3. 原子的业务逻辑，要沉淀封装在领域对象中。 例如`OrderService`上的方法`generateOrderNo()`和`isTerminal()`
就应该被封装在`LeadEntity`领域对象内，而不是在app层。

## 测试要求务必遵守
1. 后端单元测试参考`CustomerControllerIntegrationTest`, 不要在App层和DomainService上做测试
2. 每一个业务功能最多写2个TestCase，一个Happy Case，一个Bad Case
3. 后端代码改动必须执行单元测试，且确保全部通过

## Codebase Structure
这是我们的Codebase Structure，你可以信任这个结构，如果发现codebase何其不一致，请及时更新。
crm-frontend
```
crm-frontend/
├── dist/
│   └── assets/
└── src/
    ├── api/
    ├── assets/
    ├── components/
    ├── router/
    ├── stores/
    ├── types/
    ├── utils/
    └── views/
        ├── customer/
        ├── lead/
        ├── opportunity/
        └── order/
```

crm-backend:
```
crm-backend/
└── src/
├── main/
│   ├── java/com/harness/crm/
│   │   ├── adapter/web/
│   │   │   └── common/
│   │   ├── app/
│   │   │   ├── customer/dto/
│   │   │   ├── lead/dto/
│   │   │   ├── opportunity/dto/
│   │   │   └── order/dto/
│   │   ├── domain/
│   │   │   ├── customer/{entity, enums, gateway, service}/
│   │   │   ├── lead/{entity, enums, gateway, service}/
│   │   │   ├── opportunity/{entity, enums, gateway, service}/
│   │   │   └── order/{entity, enums, gateway, service}/
│   │   └── infrastructure/
│   │       ├── customer/{gateway, repository}/
│   │       ├── lead/{gateway, repository}/
│   │       ├── opportunity/{gateway, repository}/
│   │       └── order/{gateway, repository}/
│   └── resources/db/
└── test/
├── java/com/harness/crm/
│   ├── adapter/web/
│   ├── app/{lead, opportunity, order}/
│   └── domain/{lead, opportunity, order}/service/
└── resources/
```

Key decisions:
- **Entity = JPA entity**: `CustomerEntity` has `@Entity` directly, no separate DO layer
- **Gateway interface only**: `CustomerGatewayI` in domain, implemented in infrastructure (dependency inversion)
- **No Service interface**: `CustomerService` is a concrete class
- **Single DTO**: `CustomerDTO` for create, update, and response
- **Manual lifecycle**: `prePersist()`/`preUpdate()` on Entity are called by domain service, NOT JPA `@PrePersist` callbacks. Bypassing the domain service means timestamps won't be set.

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

## Database
MySQL must be running at `localhost:3306`. Initialize with:
```bash
mysql -u root -proot < crm-backend/src/main/resources/db/schema.sql
```
Dev credentials: `root`/`root`, database: `crm`.
