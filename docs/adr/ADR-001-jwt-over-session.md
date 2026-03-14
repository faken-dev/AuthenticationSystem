# ADR-001 — JWT over server-side session

**Status:** Accepted
**Date:** 2026-03-13

---

## Context

The system needs to authenticate API requests. Two mainstream approaches exist:
server-side sessions (session ID stored in a cookie, session data in server memory
or a shared store) and stateless JWT tokens (self-contained, signed token sent in
the Authorization header).

## Decision

Use **JWT (JSON Web Token)** for authentication.

- Access token: short-lived (15 minutes), sent in `Authorization: Bearer <token>` header.
- Refresh token: long-lived (30 days), stored in the database, used to issue new access tokens.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Server-side session with in-memory store | Does not scale horizontally — sessions are lost when the instance restarts or when a load balancer routes to a different instance. |
| Server-side session with Redis store | Adds statefulness and a network round-trip on every request just to validate the session. |
| Opaque token (random string looked up in DB) | Every request requires a DB query to validate — higher latency and load. |

## Consequences

- **Positive:** Stateless — any instance can validate a token without shared state.
- **Positive:** Works naturally with mobile clients and SPAs that use the Authorization header.
- **Negative:** Tokens cannot be truly invalidated before expiry — mitigated by keeping the access token TTL short (15 min) and maintaining a Redis blacklist for explicitly revoked tokens (logout).
- **Negative:** JWT payload is base64-encoded, not encrypted — must not contain sensitive data (passwords, PII). Only `userId` and `roles` are included.