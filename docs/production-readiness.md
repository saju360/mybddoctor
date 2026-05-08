# Production Readiness Checklist

## Backend

- Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` as environment variables.
- Enable HTTPS at reverse proxy and allow only trusted origins in CORS.
- Run Flyway migrations against staging before production.
- Monitor `/actuator/health` and `/actuator/metrics`.
- Configure log aggregation and alerting for 4xx/5xx spikes.

## Android

- Replace mock auth toggle with real token-based auth flow.
- Store access/refresh tokens in encrypted storage.
- Add crash reporting (Firebase Crashlytics) and analytics.
- Add offline sync retry policy for emergency and blood request actions.

## Admin Panel

- Protect admin login with strong authentication and role checks.
- Add pagination/filtering for large approval queues.
- Add audit trail for approve/reject actions.

## Security

- Enforce rate limiting per IP/user.
- Add WAF and bot protection for auth and join endpoints.
- Add automated dependency scanning in CI.
