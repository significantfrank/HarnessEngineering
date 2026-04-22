# AGENTS.md

## Project Layout
This is a  CRM project

## 编码要求务必遵守
1. 核心业务代码必须添加中文注释
2. 后端代码改动必须执行单元测试，且确保全部通过

## Commands
### crm-backend
```bash
mvn compile              # Compile
mvn spring-boot:run      # Dev server on port 8080
mvn test                 # Run tests (none exist yet)
```
No Maven wrapper (`mvnw`) is checked in. Requires global Maven.

### crm-frontend
```bash
npm run dev              # Vite dev server on port 5173
npm run build            # vue-tsc typecheck then vite build (no separate lint/typecheck script)
```
No ESLint or Prettier is configured.

### Database
MySQL must be running at `localhost:3306`. Initialize with:
```bash
mysql -u root -proot < crm-backend/src/main/resources/db/schema.sql
```
Dev credentials: `root`/`root`, database: `crm`.

## Architecture: COLA by Packages (Not Modules)

Single Maven module with package-level layering under `com.harness.crm`:

| Package | Role |
|---|---|
| `adapter/web` | REST controllers + `common/ApiResponse`, `common/GlobalExceptionHandler` |
| `app/customer` | `CustomerService` (concrete class, no interface) + `dto/CustomerDTO` |
| `domain/customer` | `entity/CustomerEntity`, `enums/`, `gateway/CustomerGatewayI`, `service/CustomerDomainService` |
| `infrastructure/customer` | `gateway/CustomerGatewayImpl`, `repository/CustomerRepository` |

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
