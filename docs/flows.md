# System Flows — AuthenticateSystem

This document describes the four core flows as sequence diagrams.
The visual versions are rendered in the project wiki and in the Claude conversation log.

---

## Flow 1 — Login (email + password + TOTP)

```
Client → POST /api/v1/auth/login {email, password}
  AuthController → AuthService.login()
    AuthService → UserRepository.findByEmail()
    UserRepository → User entity
    AuthService → BCrypt.verify(password, hash)
      [wrong] → throw AppException(INVALID_CREDENTIALS) → 401
    AuthService → check totpEnabled
      [true]  → return 202 TOTP_REQUIRED

Client → POST /api/v1/auth/login/totp {tempToken, code}
  AuthService → TotpService.validateCode(secret, code)
    [wrong] → throw AppException(TOTP_INVALID_CODE) → 400
  AuthService → TokenService.generateTokenPair(user)
    TokenService → save RefreshToken in DB
  ← 200 {accessToken, refreshToken, expiresIn}
```

**Key decisions:**
- Password checked with BCrypt before anything else — fail fast.
- TOTP is a second step, not embedded in the first request.
- If TOTP is disabled, tokens are issued immediately after password check.

---

## Flow 2 — OAuth2 Social Login (Google / Facebook)

```
Client → GET /oauth2/authorize/google
  Spring OAuth2 → redirect to Google consent screen
  User consents
  Google → GET /oauth2/callback/google?code=xxx

Spring OAuth2 → exchange code for access token (Google API)
Spring OAuth2 → fetch userinfo (email, name, picture)
  → OAuth2UserService.loadUser(OAuth2UserRequest)
    → OAuth2UserInfoFactory.getByProvider("google", attributes)
    → GoogleOAuth2UserInfo.getEmail(), getName(), getAvatarUrl()
    → UserRepository.findByEmail()
      [found]  → update name/avatar if changed
      [not found] → create new User(provider=GOOGLE, role=USER)
    ← UserPrincipal

OAuth2SuccessHandler.onAuthenticationSuccess()
  → TokenService.generateTokenPair(userPrincipal)
  → redirect to frontend: https://frontend.com/oauth2/redirect?token=xxx
```

**Key decisions:**
- Email collision (same email, different provider): link to existing account, update provider field.
- Tokens are issued in `OAuth2SuccessHandler`, not inside the service — keeps concerns separate.
- Frontend extracts token from redirect URL query param and stores it.

---

## Flow 3 — OTP Send + Verify

### Send
```
Client → POST /api/v1/otp/send {target, channel: EMAIL|WHATSAPP}
  OtpController → RateLimitService.check("otp-send", userId)
    [exceeded] → 429 RATE_LIMIT_EXCEEDED
  OtpController → OtpService.sendOtp(target, channel)
    OtpService → Redis GET otp:cooldown:{target}:{channel}
      [exists] → 429 OTP_COOLDOWN
    OtpService → OtpUtils.generate() → 6-digit code
    OtpService → Redis SET otp:{channel}:{target} = SHA256(code)  TTL=300s
    OtpService → Redis SET otp:cooldown:{target}:{channel}        TTL=60s
    OtpService → NotificationService.send(channel, target, code)
      [EMAIL]    → SendGridService → SendGrid API
      [WHATSAPP] → TwilioService   → Twilio API
  ← 200 {message: "OTP sent"}
```

### Verify
```
Client → POST /api/v1/otp/verify {target, channel, code}
  OtpController → RateLimitService.check("otp-verify", userId)
    [exceeded] → 429 RATE_LIMIT_EXCEEDED
  OtpController → OtpService.verifyOtp(target, channel, code)
    OtpService → Redis GET otp:{channel}:{target}
      [missing] → 400 OTP_EXPIRED
    OtpService → compare SHA256(code) == stored
      [wrong]   → incr otp:attempts:{target}, 400 OTP_INVALID
                  [attempts >= 5] → DEL key, 429 OTP_MAX_ATTEMPTS
      [correct] → DEL otp:{channel}:{target}, DEL otp:attempts:{target}
  ← 200 {verified: true}
```

**Key decisions:**
- OTP stored as SHA-256 hash — if Redis is compromised, codes are not readable.
- Single endpoint handles both EMAIL and WHATSAPP via `channel` field.
- Cooldown and attempt counter are separate Redis keys with independent TTLs.

---

## Flow 4 — Token Refresh + Logout

### Refresh
```
Client → POST /api/v1/auth/refresh {refreshToken}
  AuthController → TokenService.refresh(refreshToken)
    TokenService → RefreshTokenRepository.findByToken()
      [not found] → 401 TOKEN_INVALID
      [expired]   → delete from DB, 401 TOKEN_EXPIRED
    TokenService → delete old RefreshToken from DB   (rotate)
    TokenService → generate new AccessToken + RefreshToken
    TokenService → save new RefreshToken in DB
  ← 200 {accessToken, refreshToken, expiresIn}
```

### Logout
```
Client → POST /api/v1/auth/logout   Authorization: Bearer {accessToken}
  AuthController → TokenService.revoke(userId, accessToken)
    TokenService → parse JWT → extract jti + remaining TTL
    TokenService → Redis SET blacklist:token:{jti}  TTL = remaining seconds
    TokenService → RefreshTokenRepository.deleteAllByUserId(userId)
  ← 200 {message: "Logged out"}
```

**On every subsequent request:**
```
JwtAuthFilter → extract Bearer token
  → JwtService.isTokenValid(token)
  → Redis GET blacklist:token:{jti}
    [exists] → 401 TOKEN_REVOKED  (short-circuit, no DB query)
    [absent] → load UserPrincipal, set SecurityContext
```

**Key decisions:**
- Refresh token rotation: every use of a refresh token issues a brand new one and deletes the old one. A stolen refresh token can only be used once.
- Logout blacklists the specific access token by its `jti` claim (JWT ID), not by userId — avoids invalidating other sessions on other devices.
- Blacklist TTL = remaining lifetime of the token — entry auto-expires, no cleanup needed.