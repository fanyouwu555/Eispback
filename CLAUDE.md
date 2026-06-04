# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Infrastructure (Docker: PostgreSQL on :5432, Redis on :6379)
docker compose up -d

# Backend build + run
mvn clean install -DskipTests        # Install all modules to local repo
mvn spring-boot:run -pl aeisp-boot   # Run backend on :8080

# Backend tests
mvn test                             # All modules
mvn test -pl aeisp-system            # Single module

# Frontend
cd aeisp-admin && npm install && npm run dev   # Dev server on :5173, proxies /api to :8080

# DB init (execute against running PostgreSQL, or run DbInitTest.java)
psql -U postgres -d aeisp -f docs/sql/init-postgresql-complete.sql

# Swagger UI: http://localhost:8080/swagger-ui.html
# Default admin: admin / admin123
```

## Project Architecture

**9-module Maven project** (Java 17, Spring Boot 3.2.5, PostgreSQL, MyBatis-Plus 3.5.7):

| Module | Purpose |
|--------|---------|
| `aeisp-common` | Shared: `Result<T>`, `BaseEntity`, `JwtUtil`, `RedisUtil`, constants, exceptions, rate-limit |
| `aeisp-system` | RBAC (admin user/role/permission), operation logs, dict, system config |
| `aeisp-user` | Frontend user CRUD, registration, login logs, duration, permissions, stats |
| `aeisp-message` | Notification push, scheduling, user-targeted notifications |
| `aeisp-template` | Template ZIP upload/version/category, usage stats |
| `aeisp-model` | AI model integration (stub) |
| `aeisp-recharge` | Duration packages, recharge orders, balance (stub) |
| `aeisp-project` | User project management |
| `aeisp-boot` | Entry point, Security/JWT config, AuthController, Dashboard, CORS |

**Dependency chain:** `common` ← `system` ← `user` ← business modules (message, template, model, recharge, project). `aeisp-boot` depends on everything and is the only runnable JAR.

**Layered pattern per module:** Controller → Service(interface) → ServiceImpl → Mapper(MyBatis-Plus). Entities are internal-only; use VO/DTO for API responses. Controllers only receive params, call service, return response.

**Frontend:** Vue 3.4 + Vite 5 + Pinia + Element Plus 2.7 + Vue Router 4 (hash mode). Dynamic sidebar routing from backend menu tree. Axios wrapper with auto-refresh token.

## Database

- **PostgreSQL 14+** via Docker Compose (also handles Redis)
- **No ORM migration tool** — schema changes via raw SQL in `docs/sql/`
  - `init-postgresql-complete.sql` — full schema + seed data (唯一初始化脚本，新旧环境均适用)
- Table prefixes: `sys_` (RBAC/config), `usr_` (user), `msg_` (message), `tpl_` (template), `ai_`/`model_` (AI), `prj_` (project), `duration_`/`recharge_` (recharge)
- MyBatis-Plus: logical delete on `deleted` field, auto ID, `classpath*:/mapper/**/*.xml` for mapper XMLs

## Security

- JWT stateless auth: Access Token (2h) + Refresh Token (7d), Bearer header
- RBAC with Spring Security 6: `@PreAuthorize("hasAuthority('perm:key')")` on controller methods
- Super admin has full access bypass
- Passwords: BCrypt

## API Conventions

- All endpoints: `/api/v1/*`
- Unified response: `Result<T>` with `{code, message, data, timestamp}`
- Error codes: 200 ok, 400 bad request, 401 unauth, 403 forbidden, 500 error
- Pagination: `PageResult<T>` with MyBatis-Plus `Page` helper

## Key Coding Conventions

- Service must be interface-based (interface + Impl)
- Entity → VO/DTO: no entity exposed in API responses
- Mapper: single-table CRUD; handwrite XML for complex queries
- No magic values: use enums or constants
- Import Hutool & MyBatis-Plus utilities before writing custom code
- Tests: `@SpringBootTest` integration tests against real DB (no embedded/testcontainers)
- Operation logging via `@OperationLog` annotation + AOP aspect
- Dictionary cache: Redis + Pinia store + `useDict()` composable for frontend dropdown options

## DB Config

- `application.yml` at `aeisp-boot/src/main/resources/`
- PostgreSQL: `localhost:5432/aeisp`, user/pass `postgres`/`postgres`
- Redis: `localhost:6379`
- MyBatis-Plus SQL logging enabled (stdout) — turn off for prod

# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
