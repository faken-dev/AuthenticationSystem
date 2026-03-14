# ADR-003 — Flyway over Hibernate ddl-auto

**Status:** Accepted
**Date:** 2026-03-13
 
---

## Context

The database schema needs to be created and evolved over time as the application
changes. Two common approaches in the Spring ecosystem are Hibernate's `ddl-auto`
and Flyway versioned migrations.

## Decision

Use **Flyway** with `ddl-auto: none`. All schema changes are versioned SQL scripts
in `src/main/resources/db/migration/`.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| `ddl-auto: create-drop` | Drops the entire schema on every restart — data loss in development, catastrophic in production. |
| `ddl-auto: update` | Hibernate can add columns but cannot safely drop them or rename them. Schema drift accumulates silently. No migration history. |
| `ddl-auto: validate` | Read-only check — still needs something to create the schema in the first place. |
| Liquibase | Flyway is simpler for SQL-first teams. Plain SQL migrations are easier to review and debug than XML/YAML changesets. |

## Consequences

- **Positive:** Full audit trail of every schema change in version control.
- **Positive:** Flyway runs automatically on startup — no manual migration step.
- **Positive:** `baseline-on-migrate: true` allows adopting Flyway on an existing database.
- **Negative:** Every schema change requires a new migration file. Cannot just edit an entity and have the DB update automatically — intentional, not a limitation.

---