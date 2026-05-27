# LifePlus Production Deployment Guide (cPanel + Android)

This guide prepares:
- `backend-api` for production
- `admin-web` for cPanel hosting
- `android-app` for release APK/AAB

## 1) Recommended Production Architecture

Use separate subdomains:
- `https://admin.yourdomain.com` -> `admin-web` static files (cPanel public_html)
- `https://api.yourdomain.com` -> `backend-api` (Java process + reverse proxy)

## 2) cPanel Prerequisites

Required in hosting:
- Java 17 runtime (or SSH access to run Spring Boot JAR)
- MySQL/MariaDB database access
- SSL certificates enabled for both subdomains
- SSH terminal access (strongly recommended)

If your cPanel does **not** support Java runtime, host `backend-api` on VPS/Cloud and keep only `admin-web` on cPanel.

## 3) Database Setup (cPanel MySQL)

1. Create DB + user from cPanel MySQL wizard.
2. Grant all privileges to the DB user.
3. Keep these values:
- `DB_URL=jdbc:mysql://HOST:3306/DB_NAME?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME=...`
- `DB_PASSWORD=...`

## 4) Backend API Production Setup

Production profile file is already added:
- `backend-api/src/main/resources/application-prod.yml`

### 4.1 Build JAR locally

```bash
cd backend-api
mvn -DskipTests clean package
```

Output:
- `backend-api/target/backend-api-0.0.1-SNAPSHOT.jar`

### 4.2 Upload and run on server

Upload JAR to server (for example: `~/apps/lifeplus-api/`), then run:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL='jdbc:mysql://HOST:3306/DB_NAME?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='DB_USER'
export DB_PASSWORD='DB_PASS'
export JWT_SECRET='CHANGE_TO_LONG_RANDOM_SECRET_32_PLUS'
export DEMO_OTP_ENABLED=false
export PORT=8080

nohup java -jar backend-api-0.0.1-SNAPSHOT.jar > backend.log 2>&1 &
```

### 4.3 Reverse proxy to api subdomain

Point `api.yourdomain.com` to local app port `8080` via cPanel proxy/reverse-proxy config.

Health check:
- `https://api.yourdomain.com/actuator/health`

## 5) Admin Web Deployment (cPanel)

### 5.1 Build

Create `admin-web/.env.production`:

```env
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

Then:

```bash
cd admin-web
npm install
npm run build
```

### 5.2 Upload

Upload contents of `admin-web/dist/` to:
- `public_html` (if main domain)
- or subdomain document root (`admin.yourdomain.com`)

## 6) Android Release Setup

## 6.1 API endpoint for release

Set in `android-app/local.properties` (or Gradle `-PAPI_BASE_URL`):

```properties
API_BASE_URL=https://api.yourdomain.com/api/v1/
ADMOB_APP_ID_RELEASE=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
```

## 6.2 Signing config

Copy:
- `android-app/keystore.properties.example` -> `android-app/keystore.properties`

Set real values:
- `storeFile`
- `storePassword`
- `keyAlias`
- `keyPassword`

Keep keystore outside git.

## 6.3 Build release

```bash
cd android-app
./gradlew :app:bundleRelease
./gradlew :app:assembleRelease
```

Artifacts:
- `android-app/app/build/outputs/bundle/release/app-release.aab`
- `android-app/app/build/outputs/apk/release/app-release.apk`

## 7) Final Go-Live Checklist

- [ ] `DEMO_OTP_ENABLED=false`
- [ ] JWT secret changed (strong random)
- [ ] HTTPS active on both subdomains
- [ ] Admin web points to production API URL
- [ ] Android release points to production API URL
- [ ] Test login/register/forgot-password
- [ ] Test CRUD for donor, blood request, reminders, records
- [ ] Test owner/admin permission checks
- [ ] Test notifications endpoint for signed-in user
- [ ] Remove test AdMob IDs from release

## 8) Quick Troubleshooting

- `403` on API from app:
  - Check Bearer token and role policy in backend security config.
- Web shows "Failed to load data":
  - Verify `VITE_API_BASE_URL` and CORS/SSL.
- Android cannot connect:
  - Ensure `API_BASE_URL` ends with `/api/v1/`.
- cPanel backend not starting:
  - Java runtime unavailable -> move backend to VPS and keep cPanel for web only.

