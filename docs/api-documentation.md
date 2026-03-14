# API Documentation — AuthenticateSystem

Base URL: `http://localhost:8080`
API prefix: `/api/v1`
Content-Type: `application/json`

All responses follow this envelope:
```json
{
  "success": true,
  "message": "Message description",
  "data": { }
}
```
Errors:
```json
{
  "success": false,
  "message": "Message description",
  "code": "ERROR_CODE"
}
```

---

## Auth — `/api/v1/auth`

### POST `/register`
Register a new user with email and password.

**Request**
```json
{
  "email":    "user@example.com",
  "password": "Secret123",
  "fullName": "Nguyen Van A"
}
```

**Response `201`**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken":  "eyJ...",
    "refreshToken": "uuid-string",
    "expiresIn":    900
  }
}
```

**Errors:** `409 USER_ALREADY_EXISTS` · `422 VALIDATION_ERROR`

---

### POST `/login`
Login with email and password.

**Request**
```json
{
  "email":    "user@example.com",
  "password": "Secret123"
}
```

**Response `200`** — TOTP not enabled
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken":  "eyJ...",
    "refreshToken": "uuid-string",
    "expiresIn":    900
  }
}
```

**Response `202`** — TOTP enabled, second step required
```json
{
  "success": true,
  "message": "TOTP verification required",
  "data": {
    "tempToken": "short-lived-token",
    "totpRequired": true
  }
}
```

**Errors:** `401 INVALID_CREDENTIALS` · `429 RATE_LIMIT_EXCEEDED`

---

### POST `/login/totp`
Complete login when TOTP is enabled.

**Request**
```json
{
  "tempToken": "short-lived-token",
  "code":      "123456"
}
```

**Response `200`** — same as `/login` 200

**Errors:** `400 TOTP_INVALID_CODE` · `401 TOKEN_EXPIRED`

---

### POST `/logout`
`Authorization: Bearer {accessToken}` required.

Blacklists the access token and deletes all refresh tokens for the user.

**Response `200`**
```json
{ "success": true, "message": "Logged out successfully" }
```

---

### POST `/refresh`
Issue a new token pair using a valid refresh token.

**Request**
```json
{ "refreshToken": "uuid-string" }
```

**Response `200`**
```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "accessToken":  "eyJ...",
    "refreshToken": "new-uuid-string",
    "expiresIn":    900
  }
}
```

**Errors:** `401 TOKEN_INVALID` · `401 TOKEN_EXPIRED`

---

## OAuth2

### GET `/oauth2/authorize/google`
Redirects to Google consent screen. No request body.

### GET `/oauth2/authorize/facebook`
Redirects to Facebook consent screen.

### GET `/oauth2/callback/{provider}`
Handled internally by Spring Security. On success, redirects to:
```
{frontendUrl}/oauth2/redirect?token={accessToken}&refresh={refreshToken}
```

---

## User — `/api/v1/user`
All endpoints require `Authorization: Bearer {accessToken}`.

### GET `/me`
Get current user's profile.

**Response `200`**
```json
{
  "success": true,
  "data": {
    "id":          "uuid",
    "email":       "user@example.com",
    "fullName":    "Nguyen Van A",
    "avatarUrl":   "https://res.cloudinary.com/...",
    "provider":    "LOCAL",
    "totpEnabled": false,
    "roles":       ["USER"],
    "createdAt":   "2025-01-01T00:00:00Z"
  }
}
```

---

### PUT `/me`
Update current user's name.

**Request**
```json
{ "fullName": "Nguyen Van B" }
```

**Response `200`** — returns updated `UserResponse`

**Errors:** `422 VALIDATION_ERROR`

---

### POST `/me/avatar`
Upload profile picture. `Content-Type: multipart/form-data`

| Field | Type | Constraint |
|---|---|---|
| `file` | File | JPG / PNG / WEBP, max 5 MB |

**Response `200`**
```json
{
  "success": true,
  "message": "Avatar updated",
  "data": { "avatarUrl": "https://res.cloudinary.com/..." }
}
```

**Errors:** `400 INVALID_FILE_TYPE` · `400 FILE_TOO_LARGE`

---

## OTP — `/api/v1/otp`
All endpoints require `Authorization: Bearer {accessToken}`.

### POST `/send`
Send a 6-digit OTP via the specified channel.

**Request**
```json
{
  "target":  "user@example.com",
  "channel": "EMAIL"
}
```
or
```json
{
  "target":  "+84901234567",
  "channel": "WHATSAPP"
}
```

**Response `200`**
```json
{ "success": true, "message": "OTP sent successfully" }
```

**Errors:** `429 RATE_LIMIT_EXCEEDED` · `429 OTP_COOLDOWN`

---

### POST `/verify`

**Request**
```json
{
  "target":  "user@example.com",
  "channel": "EMAIL",
  "code":    "123456"
}
```

**Response `200`**
```json
{ "success": true, "data": { "verified": true } }
```

**Errors:** `400 OTP_INVALID` · `400 OTP_EXPIRED` · `429 OTP_MAX_ATTEMPTS`

---

## TOTP — `/api/v1/totp`
All endpoints require `Authorization: Bearer {accessToken}`.

### POST `/setup`
Generate a TOTP secret and QR code. User must scan in Google Authenticator
then call `/verify` to activate.

**Response `200`**
```json
{
  "success": true,
  "data": {
    "secret":      "BASE32SECRET",
    "qrCodeUrl":   "otpauth://totp/AuthenticateSystem:user@example.com?secret=...&issuer=AuthenticateSystem",
    "backupCodes": ["abc12", "def34", "ghi56", "jkl78", "mno90"]
  }
}
```

---

### POST `/verify`
Verify a TOTP code from Google Authenticator. Activates 2FA if not yet active.

**Request**
```json
{ "code": "123456" }
```

**Response `200`**
```json
{ "success": true, "message": "TOTP verified and enabled" }
```

**Errors:** `400 TOTP_INVALID_CODE`

---

### DELETE `/disable`
Disable 2FA. Requires current TOTP code to confirm.

**Request**
```json
{ "code": "123456" }
```

**Response `200`**
```json
{ "success": true, "message": "TOTP disabled" }
```

**Errors:** `400 TOTP_INVALID_CODE` · `400 TOTP_NOT_ENABLED`

---

## Role — `/api/v1/roles`
All endpoints require `ADMIN` role.

### POST `/assign`

**Request**
```json
{
  "userId":   "uuid",
  "roleName": "MODERATOR"
}
```

**Response `200`**
```json
{ "success": true, "message": "Role assigned" }
```

**Errors:** `404 USER_NOT_FOUND` · `404 ROLE_NOT_FOUND` · `409 ROLE_ALREADY_ASSIGNED`

---

### DELETE `/revoke`

**Request**
```json
{
  "userId":   "uuid",
  "roleName": "MODERATOR"
}
```

**Response `200`**
```json
{ "success": true, "message": "Role revoked" }
```

**Errors:** `404 USER_NOT_FOUND` · `400 CANNOT_REMOVE_LAST_ROLE`

---

### GET `/user/{userId}`

**Response `200`**
```json
{
  "success": true,
  "data": { "roles": ["USER", "MODERATOR"] }
}
```

---

## Error Code Reference

| Code | HTTP | Meaning |
|---|---|---|
| `USER_NOT_FOUND` | 404 | No user with given id or email |
| `USER_ALREADY_EXISTS` | 409 | Email already registered |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `TOKEN_EXPIRED` | 401 | JWT has expired |
| `TOKEN_INVALID` | 401 | JWT signature invalid or malformed |
| `TOKEN_REVOKED` | 401 | JWT has been blacklisted |
| `OTP_EXPIRED` | 400 | OTP TTL has elapsed |
| `OTP_INVALID` | 400 | OTP code does not match |
| `OTP_MAX_ATTEMPTS` | 429 | Too many failed OTP attempts |
| `OTP_COOLDOWN` | 429 | Resend requested before cooldown |
| `TOTP_NOT_ENABLED` | 400 | User has not activated 2FA |
| `TOTP_INVALID_CODE` | 400 | TOTP code is wrong |
| `ROLE_NOT_FOUND` | 404 | Role name does not exist |
| `ROLE_ALREADY_ASSIGNED` | 409 | User already has this role |
| `CANNOT_REMOVE_LAST_ROLE` | 400 | Every user must have at least one role |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `PROVIDER_NOT_SUPPORTED` | 400 | OAuth2 provider not configured |
| `INVALID_FILE_TYPE` | 400 | Avatar is not JPG/PNG/WEBP |
| `FILE_TOO_LARGE` | 400 | Avatar exceeds 5 MB |
| `VALIDATION_ERROR` | 422 | Request body failed Bean Validation |
| `INTERNAL_ERROR` | 500 | Unexpected server error |