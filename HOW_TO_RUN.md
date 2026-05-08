# LifePlus Healthcare — Step-by-Step Run Guide

---

## PART 1 — Setup Database (XAMPP)

### Step 1 — Install & Start XAMPP

1. Download XAMPP from https://www.apachefriends.org
2. Install it (default path: `C:\xampp`)
3. Open **XAMPP Control Panel** (run as Administrator)
4. Click **Start** next to **Apache**
5. Click **Start** next to **MySQL**

Both should show green "Running" status.

---

### Step 2 — Import the Database

**Method A — phpMyAdmin (Easiest)**

1. Open your browser → go to: **http://localhost/phpmyadmin**
2. Click **Import** tab (top menu)
3. Click **Choose File**
4. Select the file: `lifeplus_healthcare_db.sql` (in the project root folder)
5. Scroll down → click **Go** (Import button)
6. Wait for success message: *"Import has been successfully finished"*

**Method B — MySQL Command Line**

1. Open **Command Prompt** (cmd)
2. Run:
```cmd
"C:\xampp\mysql\bin\mysql.exe" -u root -e "source C:\path\to\lifeplus_healthcare_db.sql"
```
Replace `C:\path\to\` with the actual path to the SQL file.

**Verify it worked:**
- In phpMyAdmin, you should see database `healthcare_bd` with **22 tables**

---

## PART 2 — Start the Backend (Spring Boot)

### Step 3 — Open Backend in IDE

**Option A — IntelliJ IDEA (Recommended)**
1. Open IntelliJ IDEA
2. Click **File → Open**
3. Navigate to the `backend-api` folder → click **OK**
4. Wait for Maven to download dependencies (~2-3 minutes first time)
5. Find `HealthcareApplication.java` in:
   ```
   src/main/java/com/lifeplus/healthcare/HealthcareApplication.java
   ```
6. Right-click → **Run 'HealthcareApplication'**

**Option B — VS Code**
1. Install extension: **Extension Pack for Java**
2. Open `backend-api` folder
3. Open `HealthcareApplication.java`
4. Click the **▶ Run** button above `main()`

**Option C — Command Line** (if Java & Maven are on PATH)
```cmd
cd backend-api
mvn spring-boot:run
```

### Step 4 — Verify Backend is Running

Open browser → **http://localhost:8080/actuator/health**

You should see:
```json
{"status":"UP"}
```

If you see an error, check:
- XAMPP MySQL is running (port 3306)
- No other app is using port 8080

> **Important:** The backend will try to run Flyway migrations on startup.
> Since you already imported the SQL file manually, the `flyway_schema_history`
> table tells Spring Boot to skip migrations. ✅

---

## PART 3 — Start the Admin Web Panel

### Step 5 — Install Node.js

1. Download from https://nodejs.org (LTS version)
2. Install with default settings
3. Verify: open **Command Prompt** → type `node --version`
   Should show: `v18.x.x` or higher

### Step 6 — Setup Admin Web

1. Open **Command Prompt**
2. Navigate to the admin-web folder:
```cmd
cd "E:\Android Project\lifepluse apps\admin-web"
```
3. Install dependencies (first time only):
```cmd
npm install
```
Wait for it to finish (~1-2 minutes).

4. Create the environment file:
```cmd
copy .env.example .env
```
The `.env` file already has the correct URL: `http://localhost:8080/api/v1`

### Step 7 — Start the Admin Panel

```cmd
npm run dev
```

You should see:
```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

### Step 8 — Open Admin Panel in Browser

Go to: **http://localhost:5173**

You will see the login page.

**Login credentials:**
| Field    | Value              |
|----------|--------------------|
| Phone    | `01700000000`      |
| Password | `Admin@1234`       |

> ⚠️ **Change this password** after first login via Users page → Reset Password.

---

## PART 4 — Run Android App

### Step 9 — Open in Android Studio

1. Open **Android Studio**
2. Click **File → Open**
3. Navigate to `android-app` folder → click **OK**
4. Wait for Gradle sync (first time downloads ~150 MB)

### Step 10 — Run on Emulator

1. Click **Device Manager** (right panel)
2. Create a virtual device if none exists:
   - Click **+** → Phone → Pixel 6 → API 34 → Finish
3. Click the **▶ Run** button (or Shift+F10)
4. Select your emulator → OK

The app connects to `http://10.0.2.2:8080/api/v1/` which is the emulator's alias for your PC's localhost.

### Step 10b — Run on Physical Device (Optional)

1. Enable **Developer Options** on your phone:
   - Settings → About Phone → tap **Build Number** 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect phone via USB
4. Find your PC's IP address:
   ```cmd
   ipconfig
   ```
   Look for **IPv4 Address** (e.g. `192.168.1.5`)
5. Edit `android-app/app/build.gradle`:
   ```groovy
   buildConfigField "String", "API_BASE_URL", "\"http://192.168.1.5:8080/api/v1/\""
   ```
6. Click **Sync Now** in Android Studio
7. Run the app

---

## PART 5 — Quick Verification Checklist

After everything is running, verify these work:

### Backend API
- [ ] http://localhost:8080/actuator/health → `{"status":"UP"}`
- [ ] http://localhost:8080/api/v1/hospitals → `[]`
- [ ] http://localhost:8080/api/v1/settings → list of settings
- [ ] http://localhost:8080/swagger-ui.html → Swagger docs

### Admin Web Panel (http://localhost:5173)
- [ ] Login works with `01700000000` / `Admin@1234`
- [ ] Dashboard shows stat cards (all zeros initially)
- [ ] Settings page shows 10 default settings
- [ ] Walkthrough page shows 4 default slides
- [ ] Can add a Hospital → appears in list

### Android App
- [ ] Splash screen shows → routes to Intro slides
- [ ] Intro slides show (4 slides from backend)
- [ ] Emergency button works without login
- [ ] Hospital list loads (empty initially)
- [ ] Login works after adding a user via admin panel

---

## PART 6 — Add Your First Real Data

### Add a Hospital via Admin Panel
1. Go to **Hospitals** in sidebar
2. Click **+ Add Hospital**
3. Fill in: Name, District, Upazila
4. Click **Save**
5. Open Android app → tap **Hospital** → should show the hospital

### Add a Doctor
1. Go to **Doctors** → **+ Add Doctor**
2. Fill in: Full Name, Specialty, Hospital ID (use the ID from the hospital you just created)
3. Save

### Add Dashboard Slides (Android Home Screen)
1. Go to **Dashboard Slides** → **+ Add Slide**
2. Enter Title, Subtitle, and an Image URL
   (Use any public image URL, e.g. from Unsplash: `https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800`)
3. Save
4. Restart Android app → home screen shows the slideshow

### Manage Walkthrough Slides
1. Go to **Walkthrough** in sidebar
2. Edit the default slides or add new ones
3. On Android: clear app data (Settings → Apps → LifePlus → Clear Data)
4. Reopen app → new walkthrough slides appear

---

## PART 7 — Troubleshooting

### Problem: "Access denied" on admin login
**Cause:** The user doesn't have ADMIN role in the backend logic.
**Fix:** The backend checks `role === "ADMIN"` from the JWT. Since the demo login returns a fixed role, make sure you're using the admin user created by the SQL file.

### Problem: Backend fails to start — "Table already exists"
**Cause:** Flyway is trying to re-run migrations.
**Fix:** The SQL file already inserts records into `flyway_schema_history`. If you still get errors:
```sql
USE healthcare_bd;
DELETE FROM flyway_schema_history;
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES
  (1, '1', 'init schema',         'SQL', 'V1__init_schema.sql',         -1, 'manual', 0, TRUE),
  (2, '2', 'add missing columns', 'SQL', 'V2__add_missing_columns.sql', -1, 'manual', 0, TRUE),
  (3, '3', 'add admin tables',    'SQL', 'V3__add_admin_tables.sql',    -1, 'manual', 0, TRUE);
```

### Problem: "Communications link failure" — backend can't connect to MySQL
**Cause:** MySQL is not running or wrong port.
**Fix:**
1. Open XAMPP Control Panel
2. Make sure MySQL shows **Running** (green)
3. Check `backend-api/src/main/resources/application.yml`:
   ```yaml
   url: jdbc:mysql://127.0.0.1:3306/healthcare_bd
   username: root
   password:   # leave empty for XAMPP default
   ```

### Problem: Android app shows "No connection to server"
**Cause:** Backend not running or wrong IP.
**Fix:**
- Emulator: use `10.0.2.2` (not `localhost`)
- Physical device: use your PC's LAN IP (from `ipconfig`)
- Make sure Windows Firewall allows port 8080

### Problem: npm install fails
**Fix:**
```cmd
npm cache clean --force
npm install
```

### Problem: Gradle sync fails in Android Studio
**Fix:**
1. File → Invalidate Caches → Invalidate and Restart
2. Wait for Gradle 8.7 to download (one-time, ~150 MB)

---

## Summary — All URLs

| Service | URL | Notes |
|---------|-----|-------|
| phpMyAdmin | http://localhost/phpmyadmin | Database management |
| Backend API | http://localhost:8080 | Spring Boot |
| Swagger Docs | http://localhost:8080/swagger-ui.html | API documentation |
| Admin Panel | http://localhost:5173 | React web app |
| Android (emulator) | 10.0.2.2:8080 | Auto-configured |

## Summary — Default Credentials

| Service | Username/Phone | Password |
|---------|---------------|----------|
| phpMyAdmin | root | *(empty)* |
| Admin Panel | 01700000000 | Admin@1234 |
| Android App | any phone | any password (demo mode) |
