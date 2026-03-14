# ADR-005 — Domain-Driven folder structure

**Status:** Accepted
**Date:** 2026-03-13
 
---

## Context

The codebase needs a folder structure that scales as features are added without
turning into a flat pile of classes. Two common patterns are package-by-layer
(controller/service/repository at the top level) and package-by-feature/domain.

## Decision

Use a **Domain-Driven** structure with four top-level packages:

```
common/        shared utilities, base entity, exceptions
config/        Spring @Configuration beans
domain/        business features (auth, user, role, otp, totp)
infrastructure/ external integrations (security, notification, storage, ratelimit)
```

Each domain package contains its own controller, service, repository, entity,
mapper, and dto — fully self-contained.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Package-by-layer | `service/` package grows to 20+ classes with no logical grouping. Changing one feature requires touching multiple top-level packages. |
| Full DDD with modules (Maven multi-module) | Overkill for a single-service application. Adds build complexity without benefit at this scale. |
| Hexagonal / Ports and Adapters | Too much ceremony for a focused authentication service. DDD folder structure achieves similar separation with less boilerplate. |

## Consequences

- **Positive:** Each domain is self-contained — finding all code for a feature means looking in one folder.
- **Positive:** Easy to extract a domain into a separate service later if needed.
- **Negative:** Some cross-domain calls are necessary (e.g. OtpService uses UserRepository). These must go through service interfaces, not direct repository access across domains.

---