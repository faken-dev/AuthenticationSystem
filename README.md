# AuthenticateSystem

A production ready authenticate and user management system built with Spring Boot, featuring JWT, OAuth2 (Google & Facebook), Otp via email and WhatsApp SmS, TOTP (Google Authenticator), role based access control.

---

## Features

| Feature | Description |
| --- | --- |
| JWT Authentication | Access token (15 min) + refresh token (30 days), Redis blacklist on logout |
| OAuth2 Social login | Google and Facebook via Spring Security OAuth2 |
| OTP verification | 6 digit OTP  via email(Sendgrid) or send SmS to Whatsapp (Twillo) |
| TOTP/ 2FA | Google Authenticator support with QR code setup (ZXing) |
| Role Based Access | 'User', 'Admin', 'Moderator' roles with method level security |
| Rate limiting | Bucket4j + Redis  brute force protection on login and OTP endpoints |
| DB Migration | Flyway vesioned migrations, no 'ddl auto' in production |
| API Docs | SpringDoc Open API  - Swagger UI at '/swagger-ui.html'|

---

## Tech Stack

- **Runtime** - Java 21,   Spring Boot
- **Security** - Spring Security, JJWT, OAuth2 Client
- **Database** - PostgreSQL, Spring Data JPA, Flyway
- **Cache** - Redis, Bucket4j
- **Notification** - SendGrid(email), Twillo(WhatsApp)
- **Mapping** MapStruct

---

## Project Structure
```
src/main/java/com/AuthenticateSystem/
|__ common/
|__ config/
|__ domain/
|     |__ auth/
|     |__ user/
|     |__ role/
|     |__ otp/
|     |__ totp/
|__ infrastructure/
```

---

## Getting Started

### Prerequisites

- Java 21+
- Docker (for PostgreSQL, Redis, Jaeger)
- A SendGrid account and API key
- A Twilio account with WhatsApp Sandbox enabled

### 1. Clone the repository

```bash
git clone https://github.com/faken-dev/AuthenticationSystem.git
cd AuthenticateSystem
```

### 2. Set up environment variables

```bash
cp .env.example .env
```

### 3. Run the application

## API Overview

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register with email + password |
| `POST` | `/api/v1/auth/login` | Public | Login, receive JWT pair |
| `POST` | `/api/v1/auth/logout` | Bearer | Revoke tokens |
| `POST` | `/api/v1/auth/refresh` | Bearer | Issue new access token |
| `GET` | `/api/v1/user/me` | Bearer | Get own profile |
| `PUT` | `/api/v1/user/me` | Bearer | Update profile |
| `POST` | `/api/v1/user/me/avatar` | Bearer | Upload avatar to Cloudinary |
| `POST` | `/api/v1/otp/send` | Bearer | Send OTP (EMAIL or WHATSAPP) |
| `POST` | `/api/v1/otp/verify` | Bearer | Verify OTP code |
| `POST` | `/api/v1/totp/setup` | Bearer | Get TOTP secret + QR code |
| `POST` | `/api/v1/totp/verify` | Bearer | Verify TOTP code from authenticator |
| `DELETE` | `/api/v1/totp/disable` | Bearer | Disable 2FA |
| `GET` | `/oauth2/authorize/google` | Public | Initiate Google OAuth2 flow |
| `GET` | `/oauth2/authorize/facebook` | Public | Initiate Facebook OAuth2 flow |
| ... | ... |

Full interactive docs: `http://localhost:8080/swagger-ui.html`
 
---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
