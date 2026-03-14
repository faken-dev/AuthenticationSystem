# ADR-006 — MapStruct over manual DTO mapping

**Status:** Accepted
**Date:** 2026-03-13
 
---

## Context

Every API endpoint converts between entity objects and DTOs. This mapping code is
repetitive, error-prone when fields are added or renamed, and clutters service classes.

## Decision

Use **MapStruct** for all entity ↔ DTO conversions. Mappers are interfaces annotated
with `@Mapper(componentModel = "spring")` and injected as Spring beans.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Manual mapping in service | Verbose. Forgetting to map a new field is a silent bug. |
| ModelMapper | Runtime reflection-based — slower, harder to debug, no compile-time safety. |
| BeanUtils.copyProperties | Copies by field name only — breaks on type differences, no null handling, no custom logic. |

## Consequences

- **Positive:** Compile-time safety — missing mappings cause a build error.
- **Positive:** Zero runtime overhead — code is generated at compile time.
- **Negative:** Requires Lombok processor to be declared **before** MapStruct processor in `maven-compiler-plugin` annotation processor paths. Already configured correctly in `pom.xml`.

---