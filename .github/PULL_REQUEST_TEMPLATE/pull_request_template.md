## Summary
<!-- One sentence: what does this PR do? -->

Closes #<!-- issue number -->

---

## Type of change
- [ ] `feat` — new feature
- [ ] `fix` — bug fix
- [ ] `refactor` — code change, no behaviour change
- [ ] `test` — add or update tests
- [ ] `docs` — documentation only
- [ ] `chore` — build, deps, config

## Affected area
- [ ] auth domain
- [ ] user domain
- [ ] role domain
- [ ] otp domain
- [ ] totp domain
- [ ] infrastructure / security (JWT, OAuth2)
- [ ] infrastructure / notification (SendGrid, Twilio)
- [ ] infrastructure / storage (Cloudinary)
- [ ] infrastructure / ratelimit (Bucket4j)
- [ ] configuration / env
- [ ] Flyway migration
- [ ] CI/CD
- [ ] documentation

---

## Changes
<!-- Bullet list of what changed and why -->
-
-

## DB migration
- [ ] This PR includes a Flyway migration
- [ ] Migration is backward-compatible (no column drops, no renames)
- [ ] No migration needed

---

## Testing
- [ ] Unit tests added / updated
- [ ] Integration tests added / updated (Testcontainers)
- [ ] Manually tested locally with `docker compose up -d`
- [ ] All existing tests pass (`./mvnw verify`)

**Test coverage for new code:** <!-- e.g. "AuthService.login — 3 unit tests, 2 integration tests" -->

---

## Security checklist
- [ ] No secrets or credentials in code or logs
- [ ] New endpoints have correct `@PreAuthorize` or are explicitly public in `SecurityConfig`
- [ ] Input validated with Bean Validation annotations
- [ ] SQL injections not possible (using JPA / parameterised queries only)
- [ ] No sensitive data added to JWT payload

---

## Review notes
<!-- Anything you want reviewers to pay special attention to -->