---
name: Bug report
about: Something is not working as expected
title: "fix: "
labels: ["bug"]
assignees: []
---

## Description
<!-- A clear and concise description of what the bug is -->

## Steps to reproduce
1. Call `POST /api/v1/...` with body `{...}`
2. See error

## Expected behaviour
<!-- What should have happened -->

## Actual behaviour
<!-- What actually happened — include status code and response body -->

```json
{
  "success": false,
  "code": "...",
  "message": "..."
}
```

## Environment
- Spring Boot version:
- Java version:
- OS:
- Profile (`dev` / `prod`):

## Logs
<!-- Paste relevant log lines — mask any credentials or personal data before posting -->

```
[traceId=xxx] ERROR ...
```

## Possible cause
<!-- Optional: your hypothesis about what is wrong -->