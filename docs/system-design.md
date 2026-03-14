# System Design — AuthenticateSystem

## 1. Overview

AuthenticateSystem is a stateless, single-service authentication backend.
It owns identity and access — registration, login, token lifecycle, social login,
OTP/TOTP verification, role management, and avatar storage.

Other services validate identity by verifying the JWT this service issues.
They do not call this service on every request — the JWT is self-contained.

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Client (Web / Mobile / SPA)                                │
└────────────────────────┬────────────────────────────────────┘
                         │  HTTPS  Bearer token or OAuth2 redirect
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Security Filter Chain                                      │
│  JwtAuthFilter → extract token → validate → check blacklist │
│  OAuth2 filter → handle /oauth2/authorize + /callback       │
└────────────────────────┬────────────────────────────────────┘
                         │
┌─────────────────────────────────────────────────────────────┐
│  Domain Layer                                               │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌─────┐ ┌──────┐             │
│  │ auth │ │ user │ │ role │ │ otp │ │ totp │             │
│  └──────┘ └──────┘ └──────┘ └─────┘ └──────┘             │
└────────────────────────┬────────────────────────────────────┘
                         │
┌─────────────────────────────────────────────────────────────┐
│  Infrastructure Layer                                       │
│  JwtService  OAuth2Handlers  NotificationService            │
│  RateLimitService  CloudStorageService                      │
└──────────────┬──────────────────────────┬───────────────────┘
               │                          │
     ┌─────────▼──────────┐    ┌──────────▼──────────┐
     │    PostgreSQL       │    │       Redis          │
     │  users · roles      │    │  OTP · blacklist     │
     │  refresh_tokens     │    │  rate-limit buckets  │
     │  totp_secrets       │    └──────────────────────┘
     └────────────────────┘
```

---

## 3. Component Responsibilities

### Security Filter Chain
- `JwtAuthFilter` — runs on every request. Extracts Bearer token, validates signature,
  checks Redis blacklist, sets `SecurityContextHolder`.
- Spring OAuth2 filters — handle the redirect-based OAuth2 flow automatically.
  `OAuth2UserService` and `OAuth2SuccessHandler` extend the default behavior.

### Domain Layer
Each domain is fully self-contained: controller → service → repository → entity + dto.

| Domain | Responsibility |
|---|---|
| `auth` | Register, login (password + TOTP), logout, refresh token |
| `user` | Profile read/update, avatar upload |
| `role` | Role assignment and revocation (admin only) |
| `otp` | Generate, store, send, and verify 6-digit OTP via email or WhatsApp |
| `totp` | TOTP secret lifecycle — setup QR, verify, disable 2FA |

### Infrastructure Layer
| Component | Responsibility |
|---|---|
| `JwtService` | Sign and verify JWTs using HMAC-SHA256 |
| `OAuth2UserService` | Map Google/Facebook attributes to `UserPrincipal` |
| `OAuth2SuccessHandler` | Issue JWT pair after successful OAuth2 login |
| `NotificationService` | Interface: `send(channel, target, code)` |
| `SendGridService` | Implements `NotificationService` for email |
| `TwilioService` | Implements `NotificationService` for WhatsApp |
| `RateLimitService` | Bucket4j + Redis: enforce rate limits per key |
| `CloudStorageService` | Interface: `upload(file)` → URL |


---

## 4. Key Design Decisions

See [adr/](adr/) for full rationale. Summary:

| Decision | Choice                   | Why |
|---|--------------------------|---|
| Token strategy | JWT (stateless)          | Scales horizontally, no session affinity |
| Token invalidation | Redis blacklist by `jti` | Short-circuit without DB query |
| OTP storage | Redis with TTL           | Auto-expiry, no cleanup job |
| OTP value | SHA-256 hashed           | Safe even if Redis is compromised |
| DB migration | Flyway                   | Full audit trail, no `ddl-auto` surprises |
| DTO mapping | MapStruct                | Compile-time safety, zero runtime overhead |
| Image storage | ...                      | CDN + on-the-fly transform out of the box |
| Notification | Interface + two impls    | Swap channels without touching OtpService |

---

## 5. Token Lifecycle

```
Register / Login
      │
      ▼
generateTokenPair(user)
  ├── accessToken  (JWT, signed, 15 min, contains userId + roles)
  └── refreshToken (UUID, stored in DB, 30 days)

Every authenticated request:
  Authorization: Bearer {accessToken}
  JwtAuthFilter:
    1. parse JWT → extract jti, userId, roles
    2. Redis GET blacklist:token:{jti}  → 401 if found
    3. load UserPrincipal from DB
    4. set SecurityContext

Refresh:
  POST /auth/refresh {refreshToken}
  → validate in DB → rotate (delete old, create new) → new pair

Logout:
  POST /auth/logout
  → Redis SET blacklist:token:{jti}  TTL = remaining access token life
  → DELETE refreshToken from DB
```

---

## 6. OAuth2 Flow

```
Client → GET /oauth2/authorize/{provider}
  Spring Security → redirect to provider consent screen
  User consents → provider → GET /oauth2/callback/{provider}?code=xxx
  Spring Security → exchange code → fetch userinfo
  OAuth2UserService:
    → OAuth2UserInfoFactory.getByProvider(provider, attributes)
    → findByEmail or createUser
    → return UserPrincipal
  OAuth2SuccessHandler:
    → generateTokenPair
    → redirect to: {frontendUrl}/oauth2/redirect?token={accessToken}&refresh={refreshToken}
```

Email collision rule: if a user registers with email `a@gmail.com` via password,
then later logs in with Google using the same email — the accounts are linked.
`provider` is updated to `GOOGLE`, `provider_id` is set. Password is preserved.

---

## 7. Rate Limiting Strategy

Implemented with Bucket4j + Redis. Buckets are shared across all app instances.

| Endpoint | Key | Capacity | Refill window |
|---|---|---|---|
| POST /auth/login | `IP` | 10 requests | 60 seconds |
| POST /otp/send | `userId` | 3 requests | 3600 seconds |
| POST /otp/verify | `userId` | 5 requests | 300 seconds |

When a bucket is exhausted: `429 Too Many Requests` with `Retry-After` header.

---

## 8. Non-Functional Targets

| Metric | Target |
|---|---|
| Login p99 latency | < 500 ms |
| Token validation overhead | < 5 ms (Redis blacklist check) |
| OTP delivery (email) | < 10 seconds |
| OTP delivery (WhatsApp) | < 15 seconds |
| Availability | Passes `/actuator/health` (DB + Redis checks) |
| Tracing | Every request has `traceId` in logs, span in Jaeger |