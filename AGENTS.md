# AGENTS.md

## Commands

### chapter7/customer-center (Java 21 / Maven)
```
cd chapter7/customer-center
mvn test                  # unit + integration tests
mvn spring-boot:run       # dev server on port 8081
```

### chapter8/crm-backend (Java 21 / Maven)
```
cd chapter8/crm-backend
mvn test                  # runs checkstyle + tests (checkstyle fails build)
mvn checkstyle:check      # lint only (50 LLOC max per method)
mvn spring-boot:run       # dev server on port 8080
```

### chapter8/crm-frontend (Vue 3 / TypeScript)
```
cd chapter8/crm-frontend
npm run dev               # dev server on port 5173, proxies /api → localhost:8080
npm run build             # vue-tsc typecheck + vite build
```

### E2E Tests (Playwright)
```
docker compose --profile test up                    # full stack E2E in Docker
cd e2e-tests && npx playwright test                 # local (requires all services running)
```

### Pre-commit Hook
`.githooks/pre-commit` runs `cd chapter8/crm-backend && mvn checkstyle:check`. Installed via `mvn initialize` in crm-backend (sets `core.hooksPath`).

## Architecture

This is a book companion repo organized by chapter. The two production codebases are:

- **chapter7/customer-center** — Customer master data microservice (port 8081, Spring Boot 3.3.6)
- **chapter8/crm-backend + crm-frontend** — Full-stack CRM app (backend port 8080, frontend port 5173)

Both backends use **COLA DDD** layered architecture under `com.harness.*`:

| Layer | Package pattern | Rule |
|-------|----------------|------|
| adapter/web | Controllers, DTOs, Assembler | Depends on app + domain |
| app | Application services, owns `@Transactional` | Depends on domain |
| domain | Entities, enums, gateway interfaces, domain services | **No outward dependencies** |
| infrastructure | Gateway implementations, JPA repos | Depends on domain only |

**Dependency direction enforced by ArchUnit:** `adapter → app → domain ← infrastructure`

### Key Architecture Differences Between Backends

| | chapter7 (customer-center) | chapter8 (crm-backend) |
|---|---|---|
| Gateway interface naming | `CustomerGateway` | `CustomerGatewayI` (I suffix) |
| Entity style | Pure domain objects + Assembler to JPA | JPA `@Entity` directly (no DO layer) |
| Service interfaces | — | None — concrete classes only |
| DTO pattern | — | Single DTO for create/update/response |
| API error handling | Standard HTTP status codes | **All HTTP 200**, errors in response body `{ "code": "404", ... }` |

## Coding Conventions (crm-backend)

Mandatory rules from `chapter8/docs/coding-guideline.md`:
- Chinese comments for core business code
- Max 50 LLOC per method (enforced by checkstyle)
- No DomainService by default — app layer operates gateways directly; only add DomainService with user approval
- Atomic business logic belongs in domain entities, not app layer
- Use CMP (Composed Method Pattern) to split complex methods

## Testing Conventions (crm-backend)

Mandatory rules from `chapter8/docs/test-guideline.md`:
- Write integration tests at controller level only (reference `CustomerControllerIntegrationTest`)
- Max 2 test cases per feature: 1 happy + 1 bad
- Backend changes must pass `mvn test` before committing

Integration test pattern: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`, H2 in-memory with `create-drop`, external service mocked via `@MockitoBean` or WireMock.

## Frontend Conventions

- Path alias: `@` → `src/`
- Ant Design Vue components auto-imported (no manual import needed)
- Axios interceptor unwraps `ApiResponse`: returns `res.data` directly, rejects on `code !== '200'`
- Pagination: 0-based to backend, 1-based display in Ant Design table
- No ESLint/Prettier configured; `vue-tsc -b` for type checking

## External Dependency

crm-backend depends on customer-center service. API spec: `chapter8/docs/customer-center-api.yaml`. Config: `customer-center.base-url=http://localhost:8081`.

## Docker

- `docker-compose.yml` — local dev/test (MySQL on host port 3307, init scripts from `e2e-tests/init-scripts/`)
- `docker-compose.prod.yml` — production (images from Aliyun ACR, passwords from env vars)
- E2E test service uses `--profile test`

## Sub-AGENTS.md Files

- `chapter7/customer-center/AGENTS.md` — customer-center specific conventions and known issues
- `chapter8/AGENTS.md` — CRM monorepo rules, coding/test guidelines, API conventions

When working in chapter7 or chapter8, also read the corresponding sub-AGENTS.md.
