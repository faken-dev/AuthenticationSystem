# ADR-002 — Redis for token blacklist and OTP storage

**Status:** Accepted
**Date:** 2026-03-13

---

## Context

Two features require temporary key-value storage with automatic expiry:

1. **JWT blacklist** — when a user logs out, the access token must be invalidated
   before its natural expiry. The entry only needs to live until the token expires.
2. **OTP storage** — a 6-digit code must be stored briefly (5 minutes) and then
   automatically discarded. Failed attempt counters and resend cooldowns follow
   the same pattern.

## Decision

Use **Redis** for both concerns, leveraging its native TTL (Time-To-Live) feature.

Key schema:
```
blacklist:token:<jti>          TTL = remaining token lifetime
otp:email:<email>              TTL = 300s (5 min)
otp:whatsapp:<phone>           TTL = 300s
otp:attempts:<target>          TTL = 300s
otp:cooldown:<target>:<channel> TTL = 60s
rate:limit:<action>:<identifier> TTL = managed by Bucket4j
```

OTP values are stored **hashed** (SHA-256), never as plaintext.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Store blacklist in PostgreSQL | Requires a cleanup job to delete expired rows. Every token validation requires a DB query. Redis TTL handles expiry automatically. |
| Store OTP in PostgreSQL | Same cleanup problem. Redis is purpose-built for ephemeral key-value data with expiry. |
| No blacklist (accept short window) | Unacceptable — a stolen access token would remain valid for up to 15 minutes after logout. |

## Consequences

- **Positive:** TTL-based expiry is automatic — no cleanup jobs needed.
- **Positive:** Redis read latency is sub-millisecond — negligible overhead per request.
- **Negative:** Redis becomes a critical dependency. If Redis is unavailable, OTP and logout flows fail. Mitigated by health check (`management.health.redis.enabled: true`) and fast reconnect config in Lettuce.
- **Negative:** Redis data is not durable by default. A Redis restart clears the blacklist — tokens issued before the restart can be replayed until their natural expiry. Acceptable given the 15-minute access token TTL.