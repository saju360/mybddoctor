# Demo Credentials + Exact Test Order

Date: 2026-05-06
Detected LAN IP (this PC): `192.168.0.115`
Backend base URL for phone: `http://192.168.0.115:8080/api/v1`
Demo OTP master code: `123456`

## 1) Role-wise Demo Credentials

| Role | Phone | Password |
|---|---|---|
| ADMIN | 01700000001 | Admin@12345 |
| USER | 01700000002 | User@12345 |
| HOSPITAL_ADMIN | 01700000003 | Hospital@12345 |
| BLOOD_ORG_ADMIN | 01700000004 | BloodOrg@12345 |
| AMBULANCE_PROVIDER | 01700000005 | Ambulance@12345 |

## 2) Pre-Run Setup (must do first)

1. Start backend and confirm health:
   - `http://192.168.0.115:8080/actuator/health` returns `{"status":"UP"}`
2. Make sure phone and PC are on same Wi-Fi.
3. Build Android app with LAN host:
   - `cd android-app`
   - `.\gradlew.bat assembleDebug -PLAN_API_HOST=192.168.0.115`
4. Install APK:
   - `android-app/app/build/outputs/apk/debug/app-debug.apk`

## 3) Exact Manual Test Order

### A) Auth + OTP
1. Login as `USER` → should succeed.
2. Logout.
3. Forgot Password for `01700000002`.
4. Enter OTP `123456`.
5. Reset password to a temporary new password.
6. Login with new password → should succeed.
7. Reset back to `User@12345` for stable demo baseline.

### B) Ownership Rule Verification
1. Login as `USER`.
2. Create one Pharmacy (`Owner Test Pharmacy`).
3. Edit same Pharmacy → should succeed.
4. Logout.
5. Login as `HOSPITAL_ADMIN`.
6. Try editing/deleting USER’s Pharmacy → must fail (`403` behavior from backend).
7. Logout.
8. Login as `BLOOD_ORG_ADMIN`.
9. Try deleting USER’s Pharmacy → must fail.
10. Logout.
11. Login as `ADMIN`.
12. Edit/delete USER’s Pharmacy → should succeed.

### C) Donor Ownership Check
1. Login as `USER`.
2. Create Donor profile.
3. Logout and login as `HOSPITAL_ADMIN`.
4. Try deleting USER donor profile → must fail.
5. Login as `USER` again and delete own donor profile → should succeed.

### D) Admin Web Control Checks
1. Open admin web login.
2. Login as `ADMIN`.
3. Open these pages and verify load: Dashboard, Approvals, Users, Notifications, Settings.
4. In Notifications page, test:
   - FCM token send
   - Topic send
   - User-targeted send
   - District broadcast send
   - SMS queue send
5. Logout, login as non-admin (`USER`) on admin web → access should be denied.

## 4) Expected Security Outcomes

1. Owner + ADMIN can update/delete owner resources.
2. Non-owner non-admin cannot update/delete owner resources.
3. `/users` list is ADMIN-only.
4. `/approvals` and notification send endpoints are ADMIN-only.

## 5) Quick Fail Indicators

1. Non-admin can open admin dashboard.
2. Non-owner can edit/delete another user’s resource.
3. OTP flow fails with demo code `123456`.
4. App cannot load data on same Wi-Fi despite correct LAN host.
