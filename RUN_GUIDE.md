# LifePlus Healthcare — Complete Run Guide

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| XAMPP | 8.x | https://www.apachefriends.org |
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org (or use IDE) |
| Node.js | 18+ | https://nodejs.org |
| Android Studio | Hedgehog+ | https://developer.android.com/studio |

---

## Step 1 — Start XAMPP MySQL

1. Open **XAMPP Control Panel**
2. Click **Start** next to **MySQL**
3. MySQL will run on `localhost:3306`
4. Default credentials: `root` / *(empty password)*

> The database `healthcare_bd` will be **auto-created** by Flyway on first backend start.

---

## Step 2 — Start the Spring Boot Backend

### Option A — Using IntelliJ IDEA / Eclipse
1. Open `backend-api/` as a Maven project
2. Run `HealthcareApplication.java`

### Option B — Command Line (if Maven is on PATH)
```bash
cd backend-api
mvn spring-boot:run
```

### Option C — Build JAR first
```bash
cd backend-api
mvn clean package -DskipTests
java -jar target/backend-api-0.0.1-SNAPSHOT.jar
```

### Verify backend is running
Open: http://localhost:8080/actuator/health
Expected: `{"status":"UP"}`

### Swagger API docs
Open: http://localhost:8080/swagger-ui.html

---

## Step 3 — Create the Admin User

The backend has no admin user by default. You need to insert one directly into MySQL.

### Via phpMyAdmin (easiest)
1. Open http://localhost/phpmyadmin
2. Select database `healthcare_bd`
3. Click **SQL** tab
4. Run this query:

```sql
INSERT INTO users (created_at, updated_at, full_name, phone, email, password_hash, preferred_language, active)
VALUES (
  NOW(), NOW(),
  'Admin User',
  '01700000000',
  'admin@lifeplus.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyc5mXa6K',  -- password: Admin@1234
  'en',
  1
);
```

> The hash above is BCrypt for `Admin@1234`. Change it after first login.

### Via MySQL CLI
```bash
mysql -u root healthcare_bd
```
Then paste the INSERT above.

---

## Step 4 — Start the Admin Web Panel

```bash
cd admin-web
npm install
npm run dev
```

Open: http://localhost:5173

**Login credentials:**
- Phone: `01700000000`
- Password: `Admin@1234`

> Only accounts with `ADMIN` role can log in. The login will fail for regular users.

---

## Step 5 — Run the Android App

### In Android Emulator
1. Open `android-app/` in Android Studio
2. Wait for Gradle sync to complete
3. Select an emulator (API 24+)
4. Click **Run ▶**

The app connects to `http://10.0.2.2:8080/api/v1/` which maps to your PC's localhost.

### On a Physical Device (same Wi-Fi)
1. Find your PC's local IP: `ipconfig` → look for IPv4 (e.g. `192.168.1.5`)
2. Edit `android-app/app/build.gradle`:
   ```groovy
   buildConfigField "String", "API_BASE_URL", "\"http://192.168.1.5:8080/api/v1/\""
   ```
3. Rebuild and run

---

## Step 6 — Verify Everything Works

### Backend → MySQL
- Open http://localhost:8080/api/v1/hospitals
- Should return `[]` (empty array, not an error)

### Admin Web → Backend
- Log in at http://localhost:5173
- Dashboard should show all stat cards with `0` counts
- Go to **Settings** — should show the default settings

### Android → Backend
- Open the app
- Tap **Emergency** — should submit without login
- Tap **Hospital** — should show empty list (no data yet)
- Tap **Sign In** — login with any phone/password (demo mode)

---

## Step 7 — Add Sample Data via Admin Panel

1. Go to **Hospitals** → click **+ Add Hospital**
2. Fill in: Name, District, Upazila → Save
3. Go to **Doctors** → Add a doctor with the Hospital ID
4. Go to **Dashboard Slides** → Add a slide with an image URL
5. Go to **Walkthrough** → Edit the default slides

The Android app will reflect changes immediately on next load.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Android App (port: N/A)                                    │
│  • Retrofit → http://10.0.2.2:8080/api/v1/                 │
│  • JWT stored in EncryptedSharedPreferences                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/REST
┌──────────────────────▼──────────────────────────────────────┐
│  Spring Boot Backend (port: 8080)                           │
│  • JWT authentication                                       │
│  • Flyway migrations                                        │
│  • 20+ REST controllers                                     │
└──────────────────────┬──────────────────────────────────────┘
                       │ JDBC
┌──────────────────────▼──────────────────────────────────────┐
│  MySQL (XAMPP, port: 3306)                                  │
│  • Database: healthcare_bd                                  │
│  • Auto-created on first boot                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Admin Web Panel (port: 5173)                               │
│  • React + Vite                                             │
│  • Axios → http://localhost:8080/api/v1/                    │
│  • JWT stored in localStorage                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Environment Variables

### Backend (`backend-api/src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/healthcare_bd?createDatabaseIfNotExist=true
    username: root
    password:          # empty for XAMPP default
```

### Admin Web (`admin-web/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```
> Copy `.env.example` to `.env` and edit if needed.

### Android (`android-app/app/build.gradle`)
```groovy
buildConfigField "String", "API_BASE_URL", "\"http://10.0.2.2:8080/api/v1/\""
```

---

## Common Issues & Fixes

### "Cannot connect to server" in Android
- Make sure backend is running on port 8080
- For emulator: use `10.0.2.2` (not `localhost`)
- For physical device: use your PC's LAN IP

### "Access denied. Admin role required." on web login
- The user exists but doesn't have ADMIN role
- Re-run the INSERT SQL above to create a proper admin user

### Flyway migration error on startup
- Check MySQL is running on port 3306
- Check credentials in `application.yml`
- If schema is corrupted: drop `healthcare_bd` database and restart

### Gradle sync fails in Android Studio
- Make sure `android-app/gradle/wrapper/gradle-wrapper.jar` exists
- Click **File → Invalidate Caches → Invalidate and Restart**
- Wait for Gradle 8.7 to download (~150 MB, one-time)

### "Table 'app_settings' doesn't exist"
- Flyway V3 migration hasn't run yet
- Restart the backend — Flyway runs automatically on startup

---

## Admin Panel Features

| Feature | Description |
|---------|-------------|
| **Dashboard** | Live stats for all entities + quick approval widget |
| **Approvals** | Approve/reject with filter tabs + bulk actions |
| **Users** | Create users, toggle active, reset passwords, bulk delete |
| **Hospitals/Doctors/etc.** | Full CRUD + bulk insert (JSON) + CSV export |
| **Dashboard Slides** | Upload image URLs for Android home screen slideshow |
| **Walkthrough** | Manage onboarding slides shown on first app launch |
| **Settings** | Toggle features, set SLA, control Android behavior |

---

## Android App Features

| Feature | Auth Required | Notes |
|---------|--------------|-------|
| Emergency Help | ❌ No | Always accessible |
| Blood Request | ❌ No | Guest-friendly |
| Browse Hospitals/Doctors/etc. | ❌ No | Read-only |
| First Aid Guide | ❌ No | Fully offline |
| Vaccination Schedule | ❌ No | Fully offline |
| BMI Calculator | ❌ No | Fully offline |
| Donor Registration | ✅ Yes | Requires login |
| Book Appointment | ✅ Yes | Requires login |
| Telemedicine | ✅ Yes | Requires login |
| Health Records | ✅ Yes | Requires login |
| Medicine Reminders | ✅ Yes | Requires login |
| Blood Organization | ✅ Yes + Approval | Admin must approve |

---

## Production Deployment Notes

1. **Change JWT secret** in `application.yml`:
   ```yaml
   app.jwt.secret: ${JWT_SECRET:your-very-long-random-secret-here}
   ```

2. **Use HTTPS** — set `usesCleartextTraffic: false` in Android release build

3. **Change admin password** after first login

4. **Set up proper MySQL user** instead of root:
   ```sql
   CREATE USER 'healthcare_user'@'localhost' IDENTIFIED BY 'strong_password';
   GRANT ALL ON healthcare_bd.* TO 'healthcare_user'@'localhost';
   ```

5. **Build Android release APK**:
   ```bash
   cd android-app
   ./gradlew assembleRelease
   ```
   APK: `app/build/outputs/apk/release/app-release.apk`
