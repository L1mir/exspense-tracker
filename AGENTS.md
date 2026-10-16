# AGENTS.md

Instructions for AI coding agents (Codex, Cursor, Copilot, Claude Code) working in this repository.

## Project

Spring Boot 4.1 / Java 21 microservice: receives expense transactions in any currency, converts them to USD
by the daily close rate (twelvedata.com, cached in PostgreSQL) and sets `limit_exceeded` against a monthly
USD limit per account and category (`product` / `service`, default 1000 USD).

## Commands

```bash
docker compose up --build        # whole service + PostgreSQL in Docker (needs TWELVEDATA_API_KEY)
docker compose up -d postgres    # only PostgreSQL, for running the app locally
./mvnw verify                    # unit tests (*Test) + integration tests (*IT); Docker must be running
./mvnw spring-boot:run           # needs TWELVEDATA_API_KEY; Swagger UI at http://localhost:8080/swagger-ui.html
```

## Structure (`src/main/java/org/limir/expensetracker`)

| Package | Content |
|---|---|
| `controller` | `IntegrationTransactionController` (`/integration/v1`), `ClientLimitController` and `ClientTransactionController` (`/client/v1`) |
| `service` | `TransactionService` (limit check), `LimitService`, `AccountService` (row lock), `ExchangeRateService` |
| `repository` | Spring Data repositories, native SQL for the active limit and upserts |
| `entity`, `dto`, `mapper` | JPA entities, request/response records, MapStruct mappers |
| `client` | `ExchangeRateProvider` and its twelvedata implementation |
| `exception` | `GlobalExceptionHandler` (ProblemDetail), `ExchangeRateUnavailableException` (503) |
| `config`, `util` | `AppProperties` (`app.*`), `Clock` bean, OpenAPI; `MoneyUtils`, `DateTimeUtils` |

## Rules

- Current time only from the `Clock` bean, never `Instant.now()` without it.
- Money is `BigDecimal`, 2 decimals, `RoundingMode.HALF_UP` (`MoneyUtils`). Month boundaries in `app.time-zone` (Asia/Almaty).
- The schema is owned by Flyway (`src/main/resources/db/migration`, `ddl-auto: validate`).
  Never edit an existing migration; add `V{n}__description.sql`.
- Limits are insert-only; the limit date is set by the server.
- Any change of an account's limits or transactions goes through `AccountService.lockOrCreate` inside a transaction.
- Integration tests use `@IntegrationTest` (Testcontainers), not a bare `@SpringBootTest`; external HTTP is mocked with WireMock.
- Keep comments only where the reason is not obvious from the code.
- Do not commit or push unless the user explicitly asks.

## Skills and MCP

- Skills: `.agents/skills/` (for Claude Code also via `.claude/skills`). `write-limit-test`: how to test the limit logic.
- MCP (`.mcp.json`): `context7` for library docs, `postgres` for the local database under the read-only role
  `expense_readonly` (password in the `MCP_DB_PASSWORD` environment variable).
