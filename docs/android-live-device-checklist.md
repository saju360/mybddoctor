# Android Live Device Checklist (Same Wi-Fi)

Date: 2026-05-06
Environment: PC backend `http://<PC_IP>:8080`, Android app debug build

## 1. Network Setup
- [ ] PC and phone are connected to the same Wi-Fi network.
- [ ] Backend is running and health endpoint returns `UP`: `http://<PC_IP>:8080/actuator/health`.
- [ ] Android build uses the same host in `LAN_API_HOST`.
- [ ] App opens without splash/login crash.

## 2. Auth + OTP Flow
- [ ] Login with demo user works.
- [ ] Login with wrong password shows error.
- [ ] Forgot Password request works.
- [ ] OTP verify works (demo OTP mode).
- [ ] Reset password works and login works with new password.
- [ ] Reset back to original password works.

## 3. Ownership Rules (Critical)
- [ ] USER creates a Pharmacy.
- [ ] USER can edit own Pharmacy.
- [ ] USER can delete own Pharmacy.
- [ ] Another non-admin user cannot edit/delete that Pharmacy.
- [ ] ADMIN can edit/delete that Pharmacy.

- [ ] USER creates Donor profile.
- [ ] USER can edit/delete own Donor profile.
- [ ] Another non-admin user cannot delete that Donor profile.
- [ ] ADMIN can delete that Donor profile.

Expected behavior:
- Owner + ADMIN = allowed
- Other roles = forbidden (`403`)

## 4. Admin Notification Control
- [ ] Admin web login works.
- [ ] `POST /notifications/fcm` action returns success.
- [ ] `POST /notifications/topic` action returns success.
- [ ] `POST /notifications/user/{id}` action returns success.
- [ ] `POST /notifications/broadcast/district/{district}` action returns success.
- [ ] `POST /notifications/sms` action returns queued/success.
- [ ] Non-admin cannot access notification send APIs (`403` expected).

## 5. CRUD Smoke (Android)
- [ ] Hospitals list loads.
- [ ] Doctors list loads.
- [ ] Pharmacies list loads.
- [ ] Diagnostics list loads.
- [ ] Blood organizations list loads.
- [ ] Reminders create/update/delete works.
- [ ] Health record create/delete works.

## 6. Stability
- [ ] No crash on login, profile, manage listings, donor screen.
- [ ] No null text crash on manage card subtitle/title.
- [ ] No data encoding break in visible bullets/text.
- [ ] Loading, error, empty states show correctly.

## 7. Build Artifacts
- [ ] Debug APK install/launch successful.
- [ ] App reconnects correctly after closing/reopening.

APK path:
- `android-app/app/build/outputs/apk/debug/app-debug.apk`

