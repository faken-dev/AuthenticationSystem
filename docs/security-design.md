# Security Design — AuthenticateSystem

## 1. Authentication Model

The system uses **stateless JWT authentication** with a short-lived access token
and a long-lived refresh token stored in the database.

```
Access token:   15 minutes   JWT, signed HMAC-SHA256, contains userId + roles
Refresh token:  30 days      Random UUID, stored hashed in DB
```

Every access token contains:
```json
{
  "sub":   "uuid-of-user",
  "roles": ["ROLE_USER"],
  "iss":   "AuthenticateSystem.com",
  "iat":   1700000000,
  "exp":   1700000900,
  "jti":   "unique-token-id"
}
```

**What is NOT in the JWT:** email, full_name, avatar_url, password hash, TOTP secret.
Sensitive data stays in the database.

---

## 2. Password Security

| Rule | Implementation |
|---|---|
| Hashing algorithm | BCrypt, cost factor 12 |
| Minimum length | 8 characters (enforced by `@Size`) |
| Complexity | At least 1 uppercase, 1 digit (enforced by `@Pattern`) |
| Storage | Only the BCrypt hash is stored — plaintext never persists |
| Comparison | `BCryptPasswordEncoder.matches()` — constant-time comparison |

OAuth2 users have `password = null`. The login endpoint rejects null-password
users with `INVALID_CREDENTIALS` — they must use the OAuth2 flow.

---

## 3. Token Security

### Access token invalidation
JWT is stateless — it cannot be truly invalidated before expiry.
Mitigation: Redis blacklist keyed by `jti` (JWT ID).

```
On logout:
  Redis SET blacklist:token:{jti}  ""  EX {remaining_seconds}

On every request (JwtAuthFilter):
  Redis GET blacklist:token:{jti}
  → if exists: 401 TOKEN_REVOKED  (no DB query needed)
```

The blacklist entry TTL equals the token's remaining lifetime.
Expired tokens auto-cleanup from Redis — no maintenance job needed.

### Refresh token rotation
Every use of a refresh token issues a brand new one and deletes the old one.

```
Client sends refreshToken A
→ Server: DELETE refreshToken A from DB
→ Server: CREATE refreshToken B in DB
→ Return: new accessToken + refreshToken B
```

If refreshToken A is stolen and used after B is already issued: the old token
is no longer in the DB → attacker gets `401 TOKEN_INVALID`.

### Refresh token storage
Refresh tokens are stored as hashed UUIDs (`SHA-256`) in the database.
A DB breach does not expose usable tokens.

---

## 4. OTP Security

| Property | Implementation |
|---|---|
| Code generation | `SecureRandom` — cryptographically strong |
| Code length | 6 digits |
| Storage | `SHA-256(code)` in Redis — plaintext never stored |
| TTL | 300 seconds (5 minutes) |
| Attempt limit | 5 wrong attempts → code invalidated, `429 OTP_MAX_ATTEMPTS` |
| Resend cooldown | 60 seconds per user per channel |
| Rate limit | Max 3 send requests per user per hour |

Comparison on verify:
```java
Redis.get("otp:email:{email}") == SHA256(submittedCode)
```

---

## 5. TOTP Security

| Property | Implementation |
|---|---|
| Algorithm | HMAC-SHA1 (RFC 6238 TOTP standard) |
| Time step | 30 seconds |
| Code length | 6 digits |
| Secret generation | 32-byte random → Base32 encoded |
| Secret storage | Encrypted at rest in `totp_secrets.secret` |
| Activation | User must verify one valid code before TOTP is marked `enabled` |
| Disable | Requires a valid current TOTP code to confirm |

---

## 6. OAuth2 Security

| Concern | Mitigation |
|---|---|
| State parameter | Spring Security generates and validates `state` automatically — prevents CSRF on callback |
| Redirect URI validation | Only URIs in `app.oauth2.authorized-redirect-uris` are allowed |
| Token in redirect | Access token passed as query param — short-lived (15 min), HTTPS required |
| Email collision | Same email from different provider links to existing account — does not create duplicate |
| Provider ID binding | `provider_id` stored — subsequent logins verify both email AND provider_id |

---

## 7. API Security

### Endpoint classification
```
Public (no token required):
  POST /api/v1/auth/register
  POST /api/v1/auth/login
  POST /api/v1/auth/login/totp
  POST /api/v1/auth/refresh
  GET  /oauth2/authorize/**
  GET  /oauth2/callback/**
  GET  /api-docs/**
  GET  /swagger-ui/**
  GET  /actuator/health

Authenticated (valid Bearer token required):
  All other endpoints

Admin only (@PreAuthorize("hasRole('ADMIN')")):
  POST   /api/v1/roles/assign
  DELETE /api/v1/roles/revoke
  GET    /api/v1/roles/user/{userId}
```

### Method-level security
`@EnableMethodSecurity` is active. Sensitive endpoints use `@PreAuthorize`:
```java
@PreAuthorize("hasRole('ADMIN')")
public void assignRole(...) { ... }

@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
public UserResponse getProfile(UUID userId) { ... }
```

---

## 8. Input Validation

All request DTOs use Bean Validation (`jakarta.validation`).
`GlobalExceptionHandler` catches `MethodArgumentNotValidException` → `422 VALIDATION_ERROR`.

```java
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
             message = "Password must contain at least 1 uppercase letter and 1 digit")
    private String password;

    @NotBlank @Size(min = 2, max = 50)
    private String fullName;
}
```

---

## 9. Security Headers

Configured in `SecurityConfig`:

```java
http.headers(headers -> headers
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
    .xssProtection(Customizer.withDefaults())
    .contentSecurityPolicy(csp ->
        csp.policyDirectives("default-src 'self'"))
);
```

---

## 10. Secrets Management

| Secret | Storage | Access |
|---|---|---|
| `JWT_SECRET` | Environment variable | Never logged, never in source code |
| `SENDGRID_API_KEY` | Environment variable | Used only in `SendGridService` |
| `TWILIO_AUTH_TOKEN` | Environment variable | Used only in `TwilioService` |
| `DB_PASSWORD` | Environment variable | Used only in datasource config |
| `REDIS_PASSWORD` | Environment variable | Used only in Redis config |
| TOTP secrets | DB column, encrypted | `TotpService` only |
| Refresh tokens | DB column, SHA-256 hashed | `TokenService` only |

`.env` is in `.gitignore` — never committed.
Production secrets are injected via CI/CD secrets or a secrets manager (Vault, AWS SSM).