# ADR-004 — SendGrid for email, Twilio for WhatsApp OTP

**Status:** Accepted
**Date:** 2026-03-13
 
---

## Context

OTP codes need to be delivered to users via two channels: email and WhatsApp.
The delivery services must be reliable, have good deliverability, and support
template-based messaging to avoid spam filters.

## Decision

- **Email OTP:** SendGrid with Dynamic Templates. Template content is managed on
  the SendGrid dashboard — no HTML in application code.
- **WhatsApp OTP:** Twilio WhatsApp Business API. Sandbox number in dev,
  Meta-approved template in production.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Spring Boot Mail (SMTP) | Lower deliverability than a dedicated ESP. No template management. Gmail SMTP has strict sending limits. |
| AWS SES | More complex IAM setup. SendGrid has a more generous free tier and simpler SDK. |
| SMS instead of WhatsApp | WhatsApp has higher open rates in Southeast Asia. Twilio supports both SMS and WhatsApp with the same SDK. |
| Self-hosted SMTP | Operational overhead, IP reputation management, blacklist risk. |

## Consequences

- **Positive:** Both services have official Java SDKs with good error reporting.
- **Positive:** Template changes do not require a deployment.
- **Negative:** Two external service dependencies. If either is down, that OTP channel is unavailable. Mitigated by the dual-channel design — users can switch between email and WhatsApp.
- **Negative:** Twilio WhatsApp requires Meta approval for production templates. Dev uses Sandbox which requires manual opt-in from the recipient's phone.

---