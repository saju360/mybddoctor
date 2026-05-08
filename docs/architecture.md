# Architecture

## Components

- Android app (`android-app`) in Java.
- Spring Boot API (`backend-api`) with MySQL and Flyway migrations.
- Admin panel (`admin-web`) for approvals and master-data management.

## Dynamic Data Flow

1. Android and Admin apps call REST API.
2. API stores and queries data from MySQL.
3. Notifications are queued through API notification endpoints (FCM and SMS adapters).
4. All list/detail screens are API-driven with no hardcoded core records.
