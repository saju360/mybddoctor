# Live Phone Test Checklist (Firewall + Android Notification + OTP)

## A) Windows Firewall Setup
1. Open `Windows Defender Firewall with Advanced Security`.
2. Go to `Inbound Rules` -> `New Rule`.
3. Rule Type: `Port`.
4. Protocol: `TCP`, Specific local ports: `8080`.
5. Action: `Allow the connection`.
6. Profile: check `Private` (and `Domain` if needed).
7. Name: `LifePlus Backend 8080`.
8. Optional: repeat for `5173` if you want direct web access from phone browser.

Quick verify from PC:
- Run backend and ensure `http://localhost:8080/actuator/health` returns `UP`.

## B) Same Network Preconditions
1. PC and Android phone must be connected to the same router/Wi-Fi.
2. Use PC IPv4 address (example: `192.168.0.115`).
3. In Android app build config, API base URL must point to this IP.

## C) Backend Run
1. Start XAMPP MySQL.
2. Start backend: `run-backend.bat`.
3. Confirm backend health endpoint from PC.

## D) Android Install + Data Flow Test
1. Install latest debug APK on phone.
2. Open app and perform:
- Register user
- Login user
- Browse hospitals/doctors/blood orgs/donors
- Create + edit + delete donor
- Create + edit + delete blood organization
3. Confirm changes visible after refresh/reopen screen.

## E) Forgot Password + OTP Test
1. Open `Forgot Password` screen.
2. Enter phone and press `Send OTP`.
3. Enter OTP + new password + confirm password.
4. Reset password.
5. Login with new password.

Expected: reset success and old password should fail login.

## F) Notification Control Test (Web -> Android)
1. Login admin web.
2. Go to `Notifications` page.
3. Test in order:
- Send topic notification (`user_{id}`)
- Send user notification (`/user/{id}`)
- Send district broadcast (`blood` and `emergency` topic type)
4. Keep Android app foreground/background and verify receive behavior.

## G) Firebase Real Push Requirement
If device does not receive real push notifications:
1. Add Firebase service account JSON to:
`backend-api/src/main/resources/firebase-service-account.json`
2. Restart backend.
3. Re-test notification send.

Without service account, backend falls back to mock/log mode.
