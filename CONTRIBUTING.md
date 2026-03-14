# Contributing to AuthenticateSystem

Thank you for taking the time to contribute. This document explains how to report issues, propose changes, and submit pull requests.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How to Report a Bug](#how-to-report-a-bug)
- [How to Request a Feature](#how-to-request-a-feature)
- [Development Setup](#development-setup)
- [Branch Naming](#branch-naming)
- [Commit Convention](#commit-convention)
- [Pull Request Process](#pull-request-process)
- [Code Style](#code-style)
- [Security Vulnerabilities](#security-vulnerabilities)

---

## Code of Conduct

Be respectful, constructive, and inclusive. Harassment of any kind will not be tolerated.

---

## How to Report a Bug

1. Search existing [issues](../../issues) first to avoid duplicates.
2. Open a new issue using the **Bug Report** template.
3. Include:
    - Steps to reproduce
    - Expected vs. actual behaviour
    - Spring Boot version, Java version, OS
    - Relevant log output (mask any credentials before pasting)

---

## How to Request a Feature

1. Open an issue using the **Feature Request** template.
2. Describe the problem the feature solves, not just the solution.
3. Features that fit the project scope (auth, user management, notifications) are prioritised.

---

## Development Setup

```bash
# 1. Fork and clone
git clone https://github.com/your-username/AuthenticateSystem.git
cd AuthenticateSystem

# 2. Create your env file
cp .env.example .env
# Fill in the required values

# 3. Start infrastructure
docker compose up -d

# 4. Run the app
./mvnw spring-boot:run

# 5. Run tests
./mvnw test
```

> Docker must be running for Testcontainers-based integration tests.

---

## Branch Naming

| Type | Pattern | Example |
|---|---|---|
| Feature | `feat/short-description` | `feat/totp-backup-codes` |
| Bug fix | `fix/short-description` | `fix/otp-redis-ttl` |
| Refactor | `refactor/short-description` | `refactor/jwt-service` |
| Documentation | `docs/short-description` | `docs/api-overview` |
| Chore | `chore/short-description` | `chore/update-deps` |

Always branch off `master` and keep branches short-lived.

---

## Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/).

```
<type>(<scope>): <short summary>

[optional body]

[optional footer]
```

**Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`

**Scopes:** `auth`, `user`, `otp`, `totp`, `role`, `jwt`, `oauth2`, `notification`, `ratelimit`, `config`

**Examples:**

```
feat(otp): add WhatsApp channel via Twilio

fix(jwt): handle expired token in refresh endpoint

refactor(auth): extract token issuance into TokenService

docs(readme): add environment variable table

test(otp): add integration test for verify endpoint
```

- Use the imperative mood: "add" not "added" or "adds"
- Keep the summary under 72 characters
- Reference issues in the footer: `Closes #42`

---

## Pull Request Process

1. Make sure all tests pass locally before opening a PR.
2. Fill in the PR template completely.
3. Keep PRs focused — one concern per PR.
4. Link the related issue: `Closes #<issue-number>`.
5. At least one review approval is required before merging.
6. Squash commits when merging unless the commit history tells a clear story.

**PR title** must follow the same Conventional Commits format as commit messages.

---

## Code Style

- Follow standard Java conventions (Google Java Style Guide as baseline).
- All new public classes and methods must have Javadoc.
- No hardcoded strings — use `constants/` classes.
- No `System.out.println` — use `@Slf4j` + `log.*`.
- Every new service method that touches the database must be `@Transactional`.
- New endpoints must be documented with `@Operation` (SpringDoc).
- DTOs must use Bean Validation annotations (`@NotBlank`, `@Email`, etc.).

---

## Security Vulnerabilities

**Do not open a public issue for security vulnerabilities.**

Please report them privately by emailing `security@authenticatesystem.com` with:

- Description of the vulnerability
- Steps to reproduce
- Potential impact

We will acknowledge receipt within 48 hours and aim to release a fix within 14 days.