# AGENTS.md

## Project Overview
customer center service for Bank.

## customer-center

**Stack:** Spring Boot 3.3.6 · Java 21 · Spring Data JPA · Lombok · MySQL (prod) / H2 (test)

Dev server port: **8081** (not the default 8080).

### Coding
对于复杂业务代码添加必要的中文注释

### COLA DDD Package Structure

All code under `com.harness.customer`:

| Package | Role |
|---|---|
| `adapter/web` | REST controllers, DTOs, assembler (DTO↔Entity mapping) |
| `app/service` | Application service — orchestrates domain + gateway, owns `@Transactional` |
| `domain/entity` | Entities with business rules (e.g. `Customer.activateAccount()`, `freezeAccount()`) |
| `domain/gateway` | Port interfaces — implemented by infrastructure |
| `domain/service` | Domain services (cross-entity rules, e.g. uniqueness checks) |
| `infrastructure/gatewayimpl` | Gateway implementations, delegates to JPA repos |
| `infrastructure/repository` | Spring Data JPA repository interfaces |
| `infrastructure/converter` | JPA `AttributeConverter` classes |

**Convention:** Domain layer defines gateway interfaces; infrastructure layer implements them. Never import infrastructure from domain.

### Test Notes

- Integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc` with H2 (`create-drop`). No external services needed.
- Test config in `src/test/resources/application.yml` overrides datasource to H2 and sets `server.port=0` (random port).
- Tests clean the DB via `customerJpaRepository.deleteAll()` in `@BeforeEach`.
- The existing test file references `CustomerStatus` (does not exist) and `$.status` — should be `AccountStatus` and `$.accountStatus`. These are known compile errors.
