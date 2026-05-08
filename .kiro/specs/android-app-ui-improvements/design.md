# Design Document: Android App UI Improvements

## Overview

This document describes the technical design for improving the LifePlus Healthcare Android application. The work spans four areas: (1) UI/UX redesign of Splash, Intro, and Forgot Password screens; (2) a new dedicated Settings screen with reusable preference components; (3) CRUD completeness and correctness across all entity types, health records, reminders, and appointments; and (4) targeted bug fixes.

The app is built with Kotlin + Jetpack Compose, MVVM architecture, Hilt DI, Retrofit networking, Room for local storage, and DataStore for preferences. The Compose BOM is `2024.06.00` (Compose 1.6.x), which includes the stable `androidx.compose.foundation.layout.FlowRow` API.

---

## Architecture

The app follows a layered MVVM architecture:

```
UI Layer (Composables)
    ↕ StateFlow / events
Presentation Layer (ViewModels)
    ↕ suspend functions / Flow
Data Layer (Repositories → Retrofit / Room / DataStore)
```

Key patterns in use:
- ViewModels expose `state: StateFlow<UiState<T>>` (with `data`, `isLoading`, `error` fields) and `action: StateFlow<ActionUiState>` (with `isLoading`, `isSuccess`, `error` fields).
- Composables collect state with `collectAsState()` and call ViewModel functions for user actions.
- Navigation is handled by a single `NavController` in `MainActivity.kt` with string routes.
- Hilt provides DI; composables use `hiltViewModel()`.
- DataStore Preferences is used for persisting user settings (language, notification preference).

### Navigation Graph (additions)

The `settings` route is added to the main `NavHost` in `MainActivity.kt`:

```
splash → intro → login / home
login → home / forgot_password / register
forgot_password → login
home (MainScreen with bottom nav)
  ├── home_tab
  ├── search_tab
  ├── health_tab
  └── profile_tab → settings (NEW)
settings → (back to profile_tab)
appointments
health_records
reminders
manage_listings → add_entity/{type}?entityId={id}
...
```

---

## Components and Interfaces

### 1. SplashScreen (modified)

**File:** `ui/screens/splash/SplashScreen.kt`

**Changes:**
- Fix hardcoded `appName = "MediCare"` → read from `stringResource(R.string.app_name)` (which is "LifePlus").
- Add `BuildConfig.VERSION_NAME` display at the bottom.
- Apply `fillMaxSize()` with no system bar padding (edge-to-edge).
- Add Android 12 SplashScreen API integration in `MainActivity.kt` via `installSplashScreen()`.

**Interface:**
```kotlin
@Composable
fun SplashScreen(onNavigateNext: () -> Unit)
```

The navigation destination is determined by `AuthViewModel` state (isLoggedIn, hasCompletedOnboarding) — no change to existing logic.

---

### 2. IntroScreen (modified)

**File:** `ui/screens/intro/IntroScreen.kt`

**Changes:**
- Add Lottie fallback: wrap `LottieAnimation` in a `Box`; if `composition == null` after a timeout, show a fallback `Icon` relevant to the page.
- Add a "Continue as Guest" `TextButton` below the primary action button, visually distinct (outlined style, secondary color).
- The existing Skip button already calls `viewModel.completeOnboarding()` and navigates to guest home — the "Continue as Guest" button on the last page mirrors this behavior.

**Lottie Fallback Pattern:**
```kotlin
if (composition != null) {
    LottieAnimation(composition, { progress }, modifier)
} else {
    Icon(page.fallbackIcon, contentDescription = null, tint = Primary, modifier = Modifier.size(120.dp))
}
```

`IntroPage` data class gains a `fallbackIcon: ImageVector` field.

---

### 3. ForgotPasswordScreen (redesigned)

**File:** `ui/screens/auth/ForgotPasswordScreen.kt`

**Key fixes:**
- OTP/password fields are gated behind `otpRequested` state (driven by `state.otpSent`).
- "Back to Login" is a single `IconButton` + `Text` in the top bar, not a non-functional `Text` + separate `PrimaryButton`.
- Password fields use `PasswordVisualTransformation` with a trailing `IconButton` to toggle visibility.
- Resend OTP cooldown: a `LaunchedEffect` starts a 60-second countdown when `otpRequested` becomes true; the Resend button is disabled until the countdown reaches 0.
- Step progress indicator: a `LinearProgressIndicator` or step dots showing "Step 1 of 2" / "Step 2 of 2".
- Password match validation: checked client-side before calling `viewModel.forgotPasswordReset()`.

**State managed locally:**
```kotlin
var otpRequested by remember { mutableStateOf(false) }
var showNewPassword by remember { mutableStateOf(false) }
var showConfirmPassword by remember { mutableStateOf(false) }
var resendCooldown by remember { mutableStateOf(0) }  // seconds remaining
var passwordMatchError by remember { mutableStateOf<String?>(null) }
```

**Cooldown timer:**
```kotlin
LaunchedEffect(otpRequested) {
    if (otpRequested) {
        resendCooldown = 60
        while (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }
}
```

---

### 4. SettingsScreen (new)

**File:** `ui/screens/settings/SettingsScreen.kt`

**Route:** `"settings"`

**ViewModel dependency:** `LanguageViewModel`, `SettingsViewModel` (new, wraps DataStore reads/writes for notification preference and other settings).

**Sections:**

| Section | Items |
|---|---|
| Account | Language toggle (EN/BN), Push Notifications toggle |
| Appearance | Dark Mode toggle (Coming Soon badge) |
| About | App Version (from BuildConfig), Privacy Policy, Terms of Service, Rate the App, Contact Support |

**Navigation actions:**
- Privacy Policy → `Intent(ACTION_VIEW, Uri.parse(PRIVACY_URL))`
- Terms of Service → `Intent(ACTION_VIEW, Uri.parse(TERMS_URL))`
- Rate the App → `Intent(ACTION_VIEW, Uri.parse("market://details?id=..."))` with fallback to Play Store URL
- Contact Support → `Intent(ACTION_SENDTO, Uri.parse("mailto:support@lifeplus.health"))`

**Interface:**
```kotlin
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    languageViewModel: LanguageViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
)
```

---

### 5. SettingsComponents (new)

**File:** `ui/components/SettingsComponents.kt`

Four reusable composables:

```kotlin
@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier)

@Composable
fun SettingsToggleItem(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun SettingsClickItem(
    label: String,
    subtitle: String? = null,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null  // for "Coming Soon" etc.
)

@Composable
fun SettingsDropdownItem(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

`SettingsToggleItem` uses `SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryLight)`.

---

### 6. SettingsViewModel (new)

**File:** `presentation/viewmodel/SettingsViewModel.kt`

Wraps DataStore Preferences to persist:
- `notifications_enabled: Boolean` (default `true`)

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    val notificationsEnabled: StateFlow<Boolean> = dataStore.data
        .map { it[NOTIFICATIONS_KEY] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[NOTIFICATIONS_KEY] = enabled }
        }
    }

    companion object {
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    }
}
```

The DataStore instance is already provided via Hilt (existing `AppModule` provides it for `LanguageViewModel`).

---

### 7. NotificationScheduler (new)

**File:** `util/NotificationScheduler.kt`

Uses `AlarmManager` for exact-time reminders (requires `SCHEDULE_EXACT_ALARM` permission on Android 12+, with fallback to `setAndAllowWhileIdle` for older APIs).

```kotlin
object NotificationScheduler {
    fun scheduleReminder(context: Context, reminder: MedicineReminder)
    fun cancelReminder(context: Context, reminderId: Long)
}
```

A `BroadcastReceiver` (`ReminderAlarmReceiver`) handles the alarm and posts a `NotificationCompat` notification via `NotificationManagerCompat`.

**Channel:** `"medicine_reminders"` channel created in `LifePlusApp.onCreate()`.

**Permission handling:** On Android 13+, `POST_NOTIFICATIONS` permission is requested at runtime before scheduling the first reminder.

---

### 8. ManageListingsScreen — Delete Confirmation (modified)

**File:** `ui/screens/profile/ManageListingsScreen.kt`

`PremiumManageCard` gains a local `showDeleteConfirm: Boolean` state. The delete `IconButton` sets `showDeleteConfirm = true`. An `AlertDialog` is shown when true, with "Delete" (calls `onDelete()`) and "Cancel" buttons.

```kotlin
var showDeleteConfirm by remember { mutableStateOf(false) }

if (showDeleteConfirm) {
    AlertDialog(
        onDismissRequest = { showDeleteConfirm = false },
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete \"$title\"? This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                Text("Delete", color = ErrorColor)
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
        }
    )
}
```

---

### 9. AppointmentsScreen — Cancel Confirmation + FlowRow (modified)

**File:** `ui/screens/features/AppointmentsScreen.kt`

**Changes:**
- Replace custom `FlowRow` with `androidx.compose.foundation.layout.FlowRow` (available in Compose 1.6 via BOM 2024.06.00). Import: `import androidx.compose.foundation.layout.FlowRow`.
- Add cancel confirmation dialog in `PremiumAppointmentCard`: local `showCancelConfirm` state, same pattern as delete confirmation above.

---

### 10. HealthRecordsScreen — Delete Confirmation + Filter (modified)

**File:** `ui/screens/features/HealthRecordsScreen.kt`

**Changes:**
- Delete confirmation dialog on `PremiumRecordCard` (same pattern as ManageListings).
- Record type filter: a `FilterChipRow` composable with options `["All", "Lab", "Prescription", "Vaccination"]`. Selected filter is held in local state `var selectedFilter by remember { mutableStateOf("All") }`. The displayed list is filtered: `state.data.filter { selectedFilter == "All" || it.type == selectedFilter }`.

```kotlin
@Composable
fun RecordTypeFilterRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
)
```

Uses `FilterChip` from Material 3.

---

### 11. RemindersScreen — TimePickerDialog + Delete Confirmation + Notifications (modified)

**File:** `ui/screens/features/RemindersScreen.kt`

**Changes:**
- `AddReminderDialog` replaces the plain text `InputField` for time with a `TimePickerDialog` trigger. A `Surface` button shows the selected time; tapping it opens `android.app.TimePickerDialog`.
- Delete confirmation dialog on `PremiumReminderCard`.
- After `viewModel.create()` succeeds, call `NotificationScheduler.scheduleReminder(context, reminder)`.
- After `viewModel.toggle()` with `active = false`, call `NotificationScheduler.cancelReminder(context, reminder.id)`.
- After `viewModel.toggle()` with `active = true`, call `NotificationScheduler.scheduleReminder(context, reminder)`.

---

### 12. AddEntityScreen — Inline Validation + Edit Loading State (modified)

**File:** `ui/screens/manage/AddEntityScreen.kt`

**Changes:**
- Add `var nameError by remember { mutableStateOf<String?>(null) }` and `var districtError by remember { mutableStateOf<String?>(null) }`.
- On submit, validate before calling ViewModel; set error strings if invalid.
- Pass `isError` and `supportingText` to `InputField` for the name field.
- Show `CircularProgressIndicator` when `entityId != null && state.isLoading` (edit mode loading).
- The existing `currentActionState.isLoading` already disables the submit button.

**Validation logic:**
```kotlin
fun validateAndSubmit() {
    nameError = if (name.isBlank()) "Name is required" else null
    districtError = if (selectedDistrict == districts[0]) "Please select a district" else null
    if (nameError != null || districtError != null) return
    // proceed with create/update
}
```

---

### 13. ProfileScreen — Settings Navigation (modified)

**File:** `ui/screens/profile/ProfileScreen.kt`

**Change:** The settings `IconButton` onClick changes from `showSettingsDialog = true` to `onNavigate("settings")`. The `showSettingsDialog` state and the `AlertDialog` block are removed.

The `onNavigate` lambda in `MainScreen.kt` already routes non-tab routes to `onNavigateFeature`, which is handled in `MainActivity.kt`'s NavHost.

---

## Data Models

### MedicineReminder (existing, no change needed)

```kotlin
data class MedicineReminder(
    val id: Long,
    val medicineName: String,
    val dosage: String,
    val nextTime: String,   // stored as "HH:mm" or "hh:mm a"
    val active: Boolean
)
```

### HealthRecord (existing, no change needed)

```kotlin
data class HealthRecord(
    val id: Long,
    val title: String,
    val type: String,       // "Lab", "Prescription", "Vaccination", "General"
    val date: String,
    val imageUrl: String?
)
```

### Settings Preferences Keys (DataStore)

```kotlin
object PreferencesKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val LANGUAGE = stringPreferencesKey("language")  // existing
}
```

---

## File Structure

### New Files

```
android-app/app/src/main/java/com/lifeplus/healthcare/
├── ui/
│   ├── screens/
│   │   └── settings/
│   │       └── SettingsScreen.kt          (new)
│   └── components/
│       └── SettingsComponents.kt          (new)
├── presentation/
│   └── viewmodel/
│       └── SettingsViewModel.kt           (new)
└── util/
    └── NotificationScheduler.kt           (new)
```

### Modified Files

```
android-app/app/src/main/java/com/lifeplus/healthcare/
├── MainActivity.kt                        (add settings route, installSplashScreen)
├── ui/screens/
│   ├── splash/SplashScreen.kt             (fix app name, add version, edge-to-edge)
│   ├── intro/IntroScreen.kt               (Lottie fallback, "Continue as Guest" button)
│   ├── auth/ForgotPasswordScreen.kt       (step gating, back button, password toggle, cooldown)
│   ├── profile/ProfileScreen.kt           (settings icon → navigate, remove dialog)
│   ├── profile/ManageListingsScreen.kt    (delete confirmation dialog)
│   ├── features/AppointmentsScreen.kt     (FlowRow replacement, cancel confirmation)
│   ├── features/HealthRecordsScreen.kt    (delete confirmation, filter chips)
│   ├── features/RemindersScreen.kt        (TimePickerDialog, delete confirmation, notifications)
│   └── manage/AddEntityScreen.kt          (inline validation, edit loading state)
└── AndroidManifest.xml                    (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM permissions)
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

**Property Reflection:** After prework analysis, the following consolidations were made:
- Requirements 3.2 and 3.3 are two sides of the same visibility gate — combined into Property 1.
- Requirements 6.6, 7.5, 8.6, 9.6, and 15.6 all express the same "confirmation before delete/cancel" invariant — combined into Property 3.
- Requirements 4.4 and 15.5 both express the same DataStore persistence property — combined into Property 4.
- Requirements 7.6 and 15.10 both express the same filter correctness property — combined into Property 7.

---

### Property 1: OTP step visibility is gated by `otpSent` state

*For any* `ForgotPasswordScreen` rendering, the OTP input field, new password field, and confirm password field SHALL be visible if and only if `state.otpSent == true`. When `otpSent` is false, those fields SHALL NOT appear in the composition.

**Validates: Requirements 3.2, 3.3, 15.2**

---

### Property 2: Password mismatch blocks submission and produces an error

*For any* pair of non-equal strings `(newPassword, confirmPassword)` where `newPassword != confirmPassword`, the submit validation function SHALL return a non-null error message and SHALL NOT invoke `viewModel.forgotPasswordReset()`.

**Validates: Requirements 3.9**

---

### Property 3: Destructive actions require explicit confirmation before execution

*For any* delete or cancel action triggered from `PremiumManageCard`, `HealthRecordsScreen`, `RemindersScreen`, or `AppointmentsScreen`, the corresponding ViewModel `delete()` or `cancel()` method SHALL NOT be called until the user explicitly confirms in the confirmation dialog. Tapping the action icon alone SHALL only show the dialog.

**Validates: Requirements 6.6, 7.5, 8.6, 9.6, 15.6**

---

### Property 4: Notification preference persists across reads

*For any* boolean value `v` written to DataStore via `SettingsViewModel.setNotificationsEnabled(v)`, a subsequent read of `notificationsEnabled` from the same DataStore instance SHALL return `v`.

**Validates: Requirements 4.4, 15.5**

---

### Property 5: Entity form validation blocks submission on invalid input

*For any* `AddEntityScreen` submission attempt where `name.isBlank()` is true or `selectedDistrict == "Select District"`, the ViewModel `create()` or `update()` method SHALL NOT be called, and the corresponding inline error state SHALL be non-null.

**Validates: Requirements 6.4, 6.5, 10.6**

---

### Property 6: Reminder alarm state is consistent with reminder active state

*For any* `MedicineReminder` after `scheduleReminder()` is called with `active == true`, an alarm SHALL be registered for `reminder.id`. After `cancelReminder()` is called for that same `reminder.id`, no alarm SHALL remain registered for it.

**Validates: Requirements 8.7, 8.8**

---

### Property 7: Record type filter returns only matching records

*For any* list of `HealthRecord` objects and any filter value `f` where `f != "All"`, the result of `filterRecords(records, f)` SHALL contain only records where `record.type == f`, and the result size SHALL be less than or equal to the input list size.

**Validates: Requirements 7.6, 15.10**

---

### Property 8: Resend OTP button enabled state is determined by cooldown value

*For any* integer `cooldown` in the range `[1, 60]`, the `isResendEnabled(cooldown)` function SHALL return `false`. For `cooldown == 0`, it SHALL return `true`.

**Validates: Requirements 3.11**

---

## Error Handling

### Network Errors
- All list screens observe `state.error`. When non-null and `state.data.isEmpty()`, display an error card with a "Retry" button that re-invokes the ViewModel load function.
- CRUD action errors are observed on `action.error`. When non-null, display a `Snackbar` via `SnackbarHostState`.

### Notification Scheduling Errors
- On Android 12+, if `SCHEDULE_EXACT_ALARM` permission is not granted, `NotificationScheduler` falls back to `setAndAllowWhileIdle()` (inexact alarm). No crash.
- On Android 13+, if `POST_NOTIFICATIONS` permission is denied, reminders are created in the database but no system notification is posted. The UI shows a one-time banner prompting the user to grant the permission.

### Lottie Load Failures
- `rememberLottieComposition` returns `null` if the URL is unreachable. The composable checks `composition != null` before rendering `LottieAnimation`; otherwise renders the fallback `Icon`.

### Settings Intent Failures
- All `startActivity(Intent(...))` calls for external URLs are wrapped in a `try/catch(ActivityNotFoundException)` to prevent crashes on devices without a browser or email client.

### Form Validation
- Inline errors are shown below the relevant `InputField` using the `supportingText` parameter (Material 3 `OutlinedTextField` pattern).
- Errors are cleared when the user modifies the field value.

---

## Testing Strategy

### Unit Tests

Unit tests cover pure logic that does not require Android framework components:

- `ForgotPasswordScreen` password match validation logic (pure function).
- `AddEntityScreen` form validation logic (pure function).
- `SettingsViewModel` DataStore read/write operations (using `TestDataStore` or in-memory DataStore).
- `NotificationScheduler` alarm ID generation and time parsing logic.
- `HealthRecordsScreen` filter logic: given a list of records and a filter string, verify the output is the correct subset.

### Integration Tests

Integration tests verify component wiring and Android framework interactions:

- `SettingsViewModel` correctly reads and writes to DataStore Preferences.
- `NotificationScheduler.scheduleReminder()` registers an alarm with `AlarmManager` (verified via `AlarmManager.getNextAlarmClock()` or a test double).
- Navigation from `ProfileScreen` settings icon reaches the `SettingsScreen` composable.

### UI / Compose Tests

Compose UI tests using `ComposeTestRule`:

- `ForgotPasswordScreen`: OTP fields are not visible before OTP is sent; they appear after `state.otpSent = true`.
- `PremiumManageCard`: tapping delete shows the confirmation dialog; tapping "Cancel" dismisses it without calling `onDelete`.
- `AddEntityScreen`: submitting with a blank name shows the inline error text.
- `SettingsToggleItem`: toggling the switch invokes `onCheckedChange` with the correct boolean.
- `RecordTypeFilterRow`: selecting a filter chip updates the displayed records list.

### Property-Based Tests

Property-based testing is applied to the pure logic functions identified in the Correctness Properties section. The project uses Kotlin, so **[Kotest](https://kotest.io/)** with its `PropTest` module is the recommended PBT library (it integrates cleanly with JUnit 5 and Kotlin coroutines).

Each property test is configured to run a minimum of 100 iterations.

**Tag format:** `// Feature: android-app-ui-improvements, Property {N}: {property_text}`

**Property 1 — OTP step visibility gated by `otpSent`:**
```kotlin
// Feature: android-app-ui-improvements, Property 1: OTP step visibility is gated by otpSent state
checkAll(Arb.boolean()) { otpSent ->
    val visibility = computeFieldVisibility(otpSent)
    if (!otpSent) {
        visibility.otpFieldVisible shouldBe false
        visibility.newPasswordFieldVisible shouldBe false
        visibility.confirmPasswordFieldVisible shouldBe false
    } else {
        visibility.otpFieldVisible shouldBe true
        visibility.newPasswordFieldVisible shouldBe true
        visibility.confirmPasswordFieldVisible shouldBe true
    }
}
```

**Property 2 — Password mismatch blocks submission:**
```kotlin
// Feature: android-app-ui-improvements, Property 2: password mismatch blocks submission and produces an error
checkAll(Arb.string(), Arb.string()) { p1, p2 ->
    assume(p1 != p2)
    val result = validatePasswordMatch(p1, p2)
    result.error shouldNotBe null
    result.shouldSubmit shouldBe false
}
```

**Property 5 — Form validation blocks submission on invalid input:**
```kotlin
// Feature: android-app-ui-improvements, Property 5: entity form validation blocks submission on invalid input
checkAll(Arb.string().filter { it.isBlank() }, Arb.string()) { blankName, district ->
    val result = validateEntityForm(name = blankName, district = district, placeholder = "Select District")
    result.nameError shouldNotBe null
    result.shouldSubmit shouldBe false
}
// Also test placeholder district with valid name
checkAll(Arb.string().filter { it.isNotBlank() }) { validName ->
    val result = validateEntityForm(name = validName, district = "Select District", placeholder = "Select District")
    result.districtError shouldNotBe null
    result.shouldSubmit shouldBe false
}
```

**Property 7 — Record type filter returns only matching records:**
```kotlin
// Feature: android-app-ui-improvements, Property 7: record type filter returns only matching records
val recordArb = Arb.bind(Arb.long(), Arb.string(), Arb.element("Lab", "Prescription", "Vaccination", "General"), Arb.string(), Arb.orNull(Arb.string())) {
    id, title, type, date, imageUrl -> HealthRecord(id, title, type, date, imageUrl)
}
val filterArb = Arb.element("Lab", "Prescription", "Vaccination", "General")
checkAll(Arb.list(recordArb, 0..50), filterArb) { records, filter ->
    val filtered = filterRecords(records, filter)
    filtered.all { it.type == filter } shouldBe true
    filtered.size shouldBeLessThanOrEqualTo records.size
}
```

**Property 8 — Resend OTP button enabled state determined by cooldown:**
```kotlin
// Feature: android-app-ui-improvements, Property 8: resend OTP button enabled state is determined by cooldown value
checkAll(Arb.int(1..60)) { cooldown ->
    isResendEnabled(cooldown) shouldBe false
}
// Cooldown == 0 means enabled
isResendEnabled(0) shouldBe true
```

### Manual / Exploratory Testing

The following areas require manual verification:

- Android 12 SplashScreen API integration (requires physical device or API 31+ emulator).
- `AlarmManager` exact alarm behavior across different Android versions (24, 31, 33).
- `POST_NOTIFICATIONS` runtime permission flow on Android 13+.
- Lottie fallback rendering when network is offline.
- Language switch applying immediately without restart.
- Edge-to-edge display on devices with notches and gesture navigation.
