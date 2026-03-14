# Requirements — AuthenticateSystem

## 1. Overview

AuthenticateSystem is a standalone authentication and user management service.
It handles identity — who you are, how you prove it, and what you are allowed to do.
Other services delegate auth decisions to this system.

---

## 2. Stakeholders

| Role | Concern |
|---|---|
| End user | Register, log in, manage their own account securely |
| Admin | Manage users, assign roles, view activity |
| Frontend developer | Predictable API contracts, clear error codes |
| DevOps | Observable, containerised, environment-driven config |

---

## 3. Functional Requirements

### 3.1 Registration & Login

| ID | User Story | Acceptance Criteria |
|---|---|---|
| AUTH-01 | As a user, I want to register with email and password | Email must be unique. Password min 8 chars, at least 1 uppercase, 1 digit. Returns JWT pair on success. |
| AUTH-02 | As a user, I want to log in with email and password | Returns access token (15 min) + refresh token (30 days). Invalid credentials return 401 with error code. |
| AUTH-03 | As a user, I want to log out | Access token is blacklisted in Redis. Refresh token is deleted from DB. |
| AUTH-04 | As a user, I want to refresh my access token | Valid refresh token returns new access token. Expired or revoked refresh token returns 401. |

### 3.2 OAuth2 Social Login

| ID | User Story | Acceptance Criteria |
|---|---|---|
| OAUTH-01 | As a user, I want to log in with my Google account | Redirect to Google → callback → create or link user → return JWT pair to frontend via redirect. |
| OAUTH-02 | As a user, I want to log in with my Facebook account | Same flow as Google. Email collision (same email, different provider) is handled gracefully. |

### 3.3 OTP Verification

| ID | User Story | Acceptance Criteria |
|---|---|---|
| OTP-01 | As a user, I want to receive an OTP via email | 6-digit code sent via SendGrid. Expires in 5 minutes. Stored in Redis with TTL. |
| OTP-02 | As a user, I want to receive an OTP via WhatsApp | 6-digit code sent via Twilio WhatsApp. Same expiry and storage rules as email. |
| OTP-03 | As a user, I want to verify my OTP | Correct code within TTL → success. Wrong code increments attempt counter. After 5 failed attempts → locked for 5 minutes. |
| OTP-04 | As a user, I want to resend an OTP | Resend allowed after 60-second cooldown. Cooldown is per user per channel. |

### 3.4 TOTP — Two-Factor Authentication

| ID | User Story | Acceptance Criteria |
|---|---|---|
| TOTP-01 | As a user, I want to set up Google Authenticator | Returns a secret key and a QR code URL. User must verify one valid code to activate 2FA. |
| TOTP-02 | As a user, I want to log in with 2FA enabled | After password login, system requires a valid TOTP code before issuing JWT. |
| TOTP-03 | As a user, I want to disable 2FA | Requires current TOTP code to confirm before disabling. |

### 3.5 User Profile

| ID | User Story | Acceptance Criteria |
|---|---|---|
| USER-01 | As a user, I want to view my profile | Returns id, email, name, avatar URL, provider, roles, 2FA status. |
| USER-02 | As a user, I want to update my name | Name between 2–50 characters. Email is not changeable. |
| USER-03 | As a user, I want to upload a profile picture | Accepts JPG/PNG/WEBP, max 5 MB. Uploaded to Cloudinary. Old image is deleted. Returns new avatar URL. |

### 3.6 Role Management

| ID | User Story | Acceptance Criteria |
|---|---|---|
| ROLE-01 | As an admin, I want to assign a role to a user | Valid roles: USER, ADMIN, MODERATOR. Assigning a role the user already has is a no-op. |
| ROLE-02 | As an admin, I want to revoke a role from a user | Cannot remove the last role from a user. Every user must have at least USER. |
| ROLE-03 | As an admin, I want to list all roles of a user | Returns list of role names. |

---

## 4. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | Passwords stored as BCrypt (cost 12). JWT signed with HMAC-SHA256. No sensitive data in JWT payload except userId and roles. |
| Security | All OTP codes stored hashed in Redis, not plaintext. |
| Performance | Login endpoint must respond within 500 ms at p99 under 100 concurrent users. |
| Rate limiting | Login: max 10 attempts per minute per IP. OTP send: max 3 per hour per user. OTP verify: max 5 attempts per 5 minutes per user. |
| Availability | Service must pass `/actuator/health` with DB and Redis checks. |
| Observability | Every request must carry a traceId in logs. Slow queries (> 200 ms) must be visible in Jaeger. |
| Configuration | All secrets via environment variables. No hardcoded credentials anywhere in source code. |
| Compatibility | Java 21, Spring Boot 4.x. Runs on any Docker-capable host. |

---

## 5. Out of Scope

The following are explicitly **not** in scope for this service:

- Email verification on registration (can be added later via OTP-01)
- Password reset flow (can be added later using OTP + new password endpoint)
- Audit log / login history
- Multi-tenancy
- Payment or billing
- Any business domain beyond identity and access

---

## 6. Error Codes

All errors follow this response shape:

```json
{
  "success": false,
  "message": "Human-readable message",
  "code": "ERROR_CODE"
}
```

| Code | HTTP | Meaning |
|---|---|---|
| `USER_NOT_FOUND` | 404 | No user with given id or email |
| `USER_ALREADY_EXISTS` | 409 | Email already registered |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `TOKEN_EXPIRED` | 401 | JWT has expired |
| `TOKEN_INVALID` | 401 | JWT signature invalid or malformed |
| `TOKEN_REVOKED` | 401 | JWT has been blacklisted |
| `OTP_EXPIRED` | 400 | OTP code TTL has elapsed |
| `OTP_INVALID` | 400 | OTP code does not match |
| `OTP_MAX_ATTEMPTS` | 429 | Too many failed OTP attempts |
| `OTP_COOLDOWN` | 429 | Resend requested before cooldown elapsed |
| `TOTP_NOT_ENABLED` | 400 | User has not set up 2FA |
| `TOTP_INVALID_CODE` | 400 | TOTP code from authenticator is wrong |
| `ROLE_NOT_FOUND` | 404 | Role name does not exist |
| `ROLE_ALREADY_ASSIGNED` | 409 | User already has this role |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `PROVIDER_NOT_SUPPORTED` | 400 | OAuth2 provider is not configured |
| `VALIDATION_ERROR` | 422 | Request body failed Bean Validation |
| `INTERNAL_ERROR` | 500 | Unexpected server error |