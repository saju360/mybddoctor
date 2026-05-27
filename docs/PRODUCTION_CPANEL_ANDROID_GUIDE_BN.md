# LifePlus প্রোডাকশন ডিপ্লয়মেন্ট গাইড (cPanel + Android)

এই গাইড ফলো করলে নিচের ৩টি অংশ production-ready হবে:
- `backend-api` (Spring Boot API)
- `admin-web` (cPanel এ static web panel)
- `android-app` (release APK/AAB)

লক্ষ্য: Web admin panel upload করার পরও Android + Web একই backend/database থেকে ঠিকমতো data flow চালাবে।

## 1) Recommended Production Architecture

আলাদা subdomain ব্যবহার করুন:
- `https://admin.yourdomain.com` -> `admin-web` static files (cPanel document root)
- `https://api.yourdomain.com` -> `backend-api` (Java process + reverse proxy)

কারণ: Android app এবং Web admin দুইটাই একই API host (`api.yourdomain.com`) হিট করবে।

## 2) cPanel Prerequisites

হোস্টিংয়ে যা দরকার:
- Java 17 runtime (অথবা SSH access সহ JAR run করার সুবিধা)
- MySQL/MariaDB database
- উভয় subdomain-এ SSL certificate (HTTPS)
- SSH terminal access (strongly recommended)

যদি cPanel Java app support না করে, তাহলে `backend-api` VPS/Cloud এ host করুন, `admin-web` cPanel এ রাখুন।

## 3) Database Setup (cPanel MySQL)

1. cPanel MySQL Wizard থেকে DB create করুন।
2. DB user create করুন।
3. User-কে DB-তে সব permission দিন (All Privileges)।
4. নিচের credentials ready রাখুন:

- `DB_URL=jdbc:mysql://HOST:3306/DB_NAME?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME=...`
- `DB_PASSWORD=...`

## 4) Backend API Production Setup

Production profile ফাইল আগে থেকেই আছে:
- `backend-api/src/main/resources/application-prod.yml`

### 4.1 লোকাল থেকে JAR build

```bash
cd backend-api
mvn -DskipTests clean package
```

Output:
- `backend-api/target/backend-api-0.0.1-SNAPSHOT.jar`

### 4.2 Server এ upload ও run

JAR upload করুন (উদাহরণ: `~/apps/lifeplus-api/`) তারপর run করুন:

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

### 4.3 API subdomain reverse proxy

`api.yourdomain.com` কে server-এর local app port `8080` এ point/proxy করুন।

Health check:
- `https://api.yourdomain.com/actuator/health`

## 5) Admin Web Deployment (cPanel)

### 5.1 Production build config

`admin-web/.env.production` তৈরি/আপডেট করুন:

```env
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

তারপর build:

```bash
cd admin-web
npm install
npm run build
```

### 5.2 Upload

`admin-web/dist/` এর ভিতরের content upload করুন:
- `public_html` (main domain হলে)
- অথবা `admin.yourdomain.com` এর document root

## 6) Android Release Setup

### 6.1 Release API endpoint

`android-app/local.properties` (বা Gradle `-PAPI_BASE_URL`) এ সেট করুন:

```properties
API_BASE_URL=https://api.yourdomain.com/api/v1/
ADMOB_APP_ID_RELEASE=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
```

খেয়াল করুন: `API_BASE_URL` এর শেষে `/` থাকতে হবে।

### 6.2 Signing config

Copy করুন:
- `android-app/keystore.properties.example` -> `android-app/keystore.properties`

Real values দিন:
- `storeFile`
- `storePassword`
- `keyAlias`
- `keyPassword`

Note: keystore কখনও git এ commit করবেন না।

### 6.3 Release build

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
- [ ] `JWT_SECRET` strong random string এ change করা হয়েছে
- [ ] API এবং Admin দুই subdomain-এ HTTPS active
- [ ] Admin web production API URL-এ point করছে
- [ ] Android release production API URL-এ point করছে
- [ ] Login/Register/Forgot Password টেস্ট করা হয়েছে
- [ ] Donor/Blood Request/Reminder/Health Record CRUD টেস্ট করা হয়েছে
- [ ] Owner/Admin permission rules টেস্ট করা হয়েছে
- [ ] Signed-in user notifications endpoint টেস্ট করা হয়েছে
- [ ] Release build-এ test AdMob ID বাদ দেওয়া হয়েছে

## 8) Quick Troubleshooting

- API থেকে `403`:
  - Bearer token এবং role-based access policy check করুন।
- Web এ `Failed to load data`:
  - `VITE_API_BASE_URL`, SSL, এবং API reachability verify করুন।
- Android connect করতে পারছে না:
  - `API_BASE_URL` এ `/api/v1/` এবং ending slash `/` আছে কি না দেখুন।
- cPanel এ backend start হচ্ছে না:
  - Java runtime unavailable হলে backend VPS/Cloud এ shift করুন।

---

## 9) Mandatory Data Flow Validation (Android + Web)

Go-live এর আগে এই cross-check অবশ্যই করুন:

1. Admin panel (`admin.yourdomain.com`) থেকে একটি donor create করুন।
2. Android app থেকে donors list refresh করে নতুন donor দেখুন।
3. Android app থেকে blood request create করুন।
4. Admin panel থেকে blood requests এ entry এসেছে কি না দেখুন।
5. Android এ login করে protected endpoints (যেমন profile/notifications) test করুন।
6. Backend health endpoint এবং DB write/read logs দেখে নিশ্চিত হন request same production DB-তে যাচ্ছে।

এই ৬টি test pass করলে web upload-এর পরও Android data flow ঠিক আছে বলে ধরা যাবে।
