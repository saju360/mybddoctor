# Implementation Plan: Android App UI Improvements

## Overview

This plan implements the LifePlus Healthcare Android app improvements in Kotlin + Jetpack Compose. Tasks are ordered to build foundational components first (reusable widgets, new ViewModels), then screen-level changes, then bug fixes and wiring. Each task builds on the previous so there is no orphaned code.

## Tasks

- [x] 1. Create reusable Settings UI components
  - [x] 1.1 Create `SettingsComponents.kt` with `SettingsSectionHeader`, `SettingsToggleItem`, `SettingsClickItem`, and `SettingsDropdownItem` composables
    - Create file at `ui/components/SettingsComponents.kt`
    - `SettingsSectionHeader`: displays a section title with consistent typography and spacing
    - `SettingsToggleItem`: label, optional subtitle, `Switch` using `Primary` color for checked state, `onCheckedChange` callback, `Modifier` param
    - `SettingsClickItem`: label, optional subtitle, leading `ImageVector` icon, trailing chevron, `onClick` callback, optional `badge` string, `Modifier` param
    - `SettingsDropdownItem`: label, selected option string, options list, `onOptionSelected` callback, `Modifier` param
    - All composables accept a `Modifier` parameter
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9_

  - [x] 1.2 Write unit tests for `SettingsToggleItem` and `SettingsClickItem` callbacks
    - Use `ComposeTestRule` to verify `onCheckedChange` is invoked with the correct boolean when the switch is toggled
    - Verify `onClick` is invoked when `SettingsClickItem` is tapped
    - _Requirements: 5.5, 5.6_

- [ ] 2. Create `SettingsViewModel` with DataStore persistence
  - [x] 2.1 Create `SettingsViewModel.kt` at `presentation/viewmodel/SettingsViewModel.kt`
    - Annotate with `@HiltViewModel`; inject `DataStore<Preferences>` (already provided by `AppModule`)
    - Expose `notificationsEnabled: StateFlow<Boolean>` reading from `NOTIFICATIONS_KEY` (default `true`)
    - Implement `setNotificationsEnabled(enabled: Boolean)` writing to DataStore via `viewModelScope.launch`
    - Define `NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")` in companion object
    - _Requirements: 4.4, 15.5_

  - [ ] 2.2 Write property test for `SettingsViewModel` DataStore persistence
    - **Property 4: Notification preference persists across reads**
    - **Validates: Requirements 4.4, 15.5**
    - Use an in-memory `TestDataStore` or `PreferenceDataStoreFactory` in a temp directory
    - For any boolean `v`, write via `setNotificationsEnabled(v)` then read `notificationsEnabled`; assert the emitted value equals `v`

  - [ ] 2.3 Write unit tests for `SettingsViewModel`
    - Test default value is `true` when no preference has been written
    - Test that `setNotificationsEnabled(false)` followed by a read returns `false`
    - _Requirements: 4.4_

- [ ] 3. Create `NotificationScheduler` utility
  - [ ] 3.1 Create `NotificationScheduler.kt` at `util/NotificationScheduler.kt`
    - Implement `scheduleReminder(context: Context, reminder: MedicineReminder)` using `AlarmManager`
    - Parse `reminder.nextTime` ("HH:mm") to compute the next alarm `Calendar` instance
    - Use `setExactAndAllowWhileIdle` on API 23+; fall back to `setAndAllowWhileIdle` if `SCHEDULE_EXACT_ALARM` is not granted on API 31+
    - Implement `cancelReminder(context: Context, reminderId: Long)` using `AlarmManager.cancel()`
    - Create `ReminderAlarmReceiver` (`BroadcastReceiver`) that posts a `NotificationCompat` notification via `NotificationManagerCompat`
    - Register the `"medicine_reminders"` notification channel in `LifePlusApp.onCreate()`
    - Register `ReminderAlarmReceiver` in `AndroidManifest.xml`
    - Add `POST_NOTIFICATIONS` and `SCHEDULE_EXACT_ALARM` permissions to `AndroidManifest.xml`
    - _Requirements: 8.7, 8.8_

  - [ ] 3.2 Write property test for reminder alarm state consistency
    - **Property 6: Reminder alarm state is consistent with reminder active state**
    - **Validates: Requirements 8.7, 8.8**
    - Use a test double or shadow `AlarmManager` (e.g., Robolectric `ShadowAlarmManager`)
    - For any `MedicineReminder` with `active == true`, after `scheduleReminder()` an alarm is registered for `reminder.id`
    - After `cancelReminder(reminder.id)`, no alarm remains registered for that ID

  - [ ] 3.3 Write unit tests for `NotificationScheduler` time parsing
    - Test that `parseReminderTime("08:30")` produces a `Calendar` with hour=8, minute=30
    - Test that `parseReminderTime("23:59")` produces a `Calendar` with hour=23, minute=59
    - _Requirements: 8.7_

- [ ] 4. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement `SettingsScreen`
  - [ ] 5.1 Create `SettingsScreen.kt` at `ui/screens/settings/SettingsScreen.kt`
    - Use `PremiumTopBar` with a back navigation button (`onNavigateBack` lambda)
    - Inject `LanguageViewModel` and `SettingsViewModel` via `hiltViewModel()`
    - Section "Account": language toggle using `SettingsToggleItem` (EN/BN) wired to `LanguageViewModel`; push notifications toggle wired to `SettingsViewModel.notificationsEnabled`
    - Section "Appearance": dark mode `SettingsClickItem` with "Coming Soon" badge
    - Section "About": app version from `BuildConfig.VERSION_NAME` via `SettingsClickItem`; Privacy Policy, Terms of Service, Rate the App, Contact Support as `SettingsClickItem` entries
    - Wrap all external `startActivity` calls in `try/catch(ActivityNotFoundException)`
    - Apply `statusBarsPadding()` on the top bar
    - Language change applies immediately via `LanguageViewModel` without restart
    - _Requirements: 4.1, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10, 4.11, 4.12_

  - [ ] 5.2 Add `settings` route to `NavHost` in `MainActivity.kt`
    - Add `composable("settings") { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }` inside the `NavHost`
    - _Requirements: 4.1, 4.2, 12.1_

- [ ] 6. Fix `ProfileScreen` — replace settings dialog with navigation
  - Modify `ui/screens/profile/ProfileScreen.kt`
  - Change the settings `IconButton` `onClick` from `showSettingsDialog = true` to `onNavigate("settings")`
  - Remove the `showSettingsDialog` state variable and the `AlertDialog` block entirely
  - _Requirements: 4.2, 15.4_

- [ ] 7. Redesign `SplashScreen`
  - Modify `ui/screens/splash/SplashScreen.kt`
  - Replace hardcoded `"MediCare"` with `stringResource(R.string.app_name)` (value: "LifePlus")
  - Add `BuildConfig.VERSION_NAME` displayed in small text at the bottom of the screen
  - Apply `fillMaxSize()` with no system bar padding (edge-to-edge, `WindowCompat.setDecorFitsSystemWindows(window, false)`)
  - Verify spring-based scale-in animation for logo completes within 400ms, followed by app name fade-in, then tagline fade-in
  - Verify total animation + navigation completes within 3500ms
  - Add `installSplashScreen()` call in `MainActivity.kt` `onCreate()` for Android 12+ API integration
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 15.1_

- [ ] 8. Enhance `IntroScreen`
  - Modify `ui/screens/intro/IntroScreen.kt`
  - Add `fallbackIcon: ImageVector` field to the `IntroPage` data class
  - Wrap each `LottieAnimation` in a `Box`; if `composition == null`, render the fallback `Icon` at 120dp with `Primary` tint
  - Add a "Continue as Guest" `TextButton` below the primary action button (outlined style, secondary color)
  - "Continue as Guest" calls `viewModel.completeOnboarding()` and navigates to home as guest (same behavior as "Skip")
  - Verify `statusBarsPadding()` and `navigationBarsPadding()` are applied
  - Verify animated dot progress indicator uses `animateDpAsState` (8dp inactive → 28dp active)
  - Verify LifePlus logo and brand name appear in the header row on every page
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11, 2.12_

- [ ] 9. Redesign `ForgotPasswordScreen`
  - [ ] 9.1 Extract pure validation functions into a testable unit
    - Create `fun validatePasswordMatch(newPassword: String, confirmPassword: String): PasswordMatchResult` (returns `error: String?`, `shouldSubmit: Boolean`)
    - Create `fun isResendEnabled(cooldown: Int): Boolean` (returns `cooldown == 0`)
    - Place these in a file accessible to both the composable and tests (e.g., `ui/screens/auth/ForgotPasswordValidation.kt`)
    - _Requirements: 3.9, 3.11_

  - [ ] 9.2 Write property test for password mismatch blocking submission
    - **Property 2: Password mismatch blocks submission and produces an error**
    - **Validates: Requirements 3.9**
    - Use Kotest `checkAll(Arb.string(), Arb.string())` with `assume(p1 != p2)`
    - Assert `result.error != null` and `result.shouldSubmit == false`

  - [ ] 9.3 Write property test for resend OTP cooldown enabled state
    - **Property 8: Resend OTP button enabled state is determined by cooldown value**
    - **Validates: Requirements 3.11**
    - Use Kotest `checkAll(Arb.int(1..60))` and assert `isResendEnabled(cooldown) == false`
    - Assert `isResendEnabled(0) == true`

  - [ ] 9.4 Rewrite `ForgotPasswordScreen.kt` composable
    - Add local state: `otpRequested`, `showNewPassword`, `showConfirmPassword`, `resendCooldown`, `passwordMatchError`
    - Gate OTP/password fields behind `otpRequested` (set to `true` when `state.otpSent == true`)
    - Replace non-functional `Text` + separate `PrimaryButton` back navigation with a single `IconButton` + `Text` in the top bar using `AuthBackground`
    - Add step progress indicator ("Step 1 of 2" / "Step 2 of 2") at the top
    - Password fields use `PasswordVisualTransformation` with trailing `IconButton` visibility toggle
    - Resend OTP `LaunchedEffect` starts 60-second countdown when `otpRequested` becomes true; Resend button disabled until `resendCooldown == 0`
    - On submit, call `validatePasswordMatch()` before calling `viewModel.forgotPasswordReset()`; display inline error if mismatch
    - Show loading indicator on "Send OTP" button while `state.isLoading`
    - Display `state.error` in red text below the action button
    - Navigate back to Login when `state.passwordResetSuccess` becomes true
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12, 15.2, 15.3_

  - [ ] 9.5 Write Compose UI test for `ForgotPasswordScreen` OTP field visibility
    - **Property 1: OTP step visibility is gated by `otpSent` state**
    - **Validates: Requirements 3.2, 3.3, 15.2**
    - Assert OTP, new password, and confirm password fields are NOT visible when `state.otpSent == false`
    - Assert those fields ARE visible when `state.otpSent == true`

- [ ] 10. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Fix `ManageListingsScreen` — delete confirmation dialog
  - Modify `ui/screens/profile/ManageListingsScreen.kt`
  - Add `var showDeleteConfirm by remember { mutableStateOf(false) }` local state to `PremiumManageCard`
  - Delete `IconButton` sets `showDeleteConfirm = true` instead of calling `onDelete()` directly
  - Show `AlertDialog` when `showDeleteConfirm == true` with "Delete" (calls `onDelete(); showDeleteConfirm = false`) and "Cancel" buttons
  - "Delete" button text uses `ErrorColor`
  - _Requirements: 6.6, 15.6_

- [ ] 12. Fix `AppointmentsScreen` — `FlowRow` replacement and cancel confirmation
  - Modify `ui/screens/features/AppointmentsScreen.kt`
  - Replace custom `FlowRow` import with `androidx.compose.foundation.layout.FlowRow` (stable in Compose 1.6 / BOM 2024.06.00)
  - Add `var showCancelConfirm by remember { mutableStateOf(false) }` local state to `PremiumAppointmentCard`
  - Cancel `IconButton` on PENDING appointment cards sets `showCancelConfirm = true`
  - Show `AlertDialog` when `showCancelConfirm == true` with "Cancel Appointment" and "Keep" buttons
  - "Cancel Appointment" calls `viewModel.cancel()` and dismisses the dialog
  - _Requirements: 9.6, 15.7_

- [ ] 13. Fix `HealthRecordsScreen` — delete confirmation and functional filter
  - [ ] 13.1 Extract `filterRecords` as a pure function
    - Create `fun filterRecords(records: List<HealthRecord>, filter: String): List<HealthRecord>` returning all records when `filter == "All"`, otherwise only records where `record.type == filter`
    - Place in `ui/screens/features/HealthRecordsUtils.kt` or alongside the screen file
    - _Requirements: 7.6, 15.10_

  - [ ] 13.2 Write property test for record type filter
    - **Property 7: Record type filter returns only matching records**
    - **Validates: Requirements 7.6, 15.10**
    - Use Kotest `checkAll(Arb.list(recordArb, 0..50), filterArb)` where `filterArb` picks from `["Lab", "Prescription", "Vaccination", "General"]`
    - Assert `filtered.all { it.type == filter }` and `filtered.size <= records.size`

  - [ ] 13.3 Update `HealthRecordsScreen.kt` with delete confirmation and filter UI
    - Add `var showDeleteConfirm by remember { mutableStateOf(false) }` to `PremiumRecordCard`
    - Delete icon sets `showDeleteConfirm = true`; `AlertDialog` confirms before calling `viewModel.delete()`
    - Add `RecordTypeFilterRow` composable using `FilterChip` from Material 3 with options `["All", "Lab", "Prescription", "Vaccination"]`
    - Hold `var selectedFilter by remember { mutableStateOf("All") }` in the screen
    - Pass `filterRecords(state.data, selectedFilter)` to the records list
    - _Requirements: 7.5, 7.6, 15.10_

- [ ] 14. Fix `RemindersScreen` — `TimePickerDialog`, delete confirmation, and notification scheduling
  - Modify `ui/screens/features/RemindersScreen.kt`
  - In `AddReminderDialog`, replace the plain text `InputField` for time with a `Surface` button showing the selected time; tapping it opens `android.app.TimePickerDialog`
  - Add `var showDeleteConfirm by remember { mutableStateOf(false) }` to `PremiumReminderCard`; delete icon shows confirmation dialog before calling `viewModel.delete()`
  - After `viewModel.create()` succeeds, call `NotificationScheduler.scheduleReminder(context, reminder)`
  - After `viewModel.toggle()` with `active = true`, call `NotificationScheduler.scheduleReminder(context, reminder)`
  - After `viewModel.toggle()` with `active = false`, call `NotificationScheduler.cancelReminder(context, reminder.id)`
  - Request `POST_NOTIFICATIONS` permission at runtime on Android 13+ before scheduling the first reminder
  - _Requirements: 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_

- [ ] 15. Fix `AddEntityScreen` — inline validation and edit-mode loading state
  - [ ] 15.1 Extract `validateEntityForm` as a pure function
    - Create `fun validateEntityForm(name: String, district: String, placeholder: String): EntityFormValidationResult` (returns `nameError: String?`, `districtError: String?`, `shouldSubmit: Boolean`)
    - Place in `ui/screens/manage/AddEntityValidation.kt`
    - _Requirements: 6.4, 6.5, 10.6_

  - [ ] 15.2 Write property test for entity form validation
    - **Property 5: Entity form validation blocks submission on invalid input**
    - **Validates: Requirements 6.4, 6.5, 10.6**
    - Use Kotest `checkAll(Arb.string().filter { it.isBlank() }, Arb.string())` — assert `nameError != null` and `shouldSubmit == false`
    - Use Kotest `checkAll(Arb.string().filter { it.isNotBlank() })` with `district = "Select District"` — assert `districtError != null` and `shouldSubmit == false`

  - [ ] 15.3 Update `AddEntityScreen.kt` with validation and loading state
    - Add `var nameError by remember { mutableStateOf<String?>(null) }` and `var districtError by remember { mutableStateOf<String?>(null) }`
    - On submit, call `validateEntityForm()` before calling ViewModel; set error strings if invalid; display inline errors via `supportingText` on `InputField`
    - Show `CircularProgressIndicator` when `entityId != null && state.isLoading` (edit mode loading)
    - Clear errors when the user modifies the relevant field
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 10.6, 15.9_

- [ ] 16. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 17. Implement consistent error/loading states across list screens
  - For each list screen that does not yet have it (`ManageListingsScreen`, `HealthRecordsScreen`, `RemindersScreen`, `AppointmentsScreen`):
    - Show `CircularProgressIndicator` (centered, `Primary` color) when `state.isLoading && state.data.isEmpty()`
    - Show error card with message and "Retry" button when `state.error != null && state.data.isEmpty()`
    - "Retry" button re-invokes the ViewModel load function
    - Show empty state illustration + descriptive message when `!state.isLoading && state.data.isEmpty() && state.error == null`
    - Never display loading indicator and error state simultaneously
  - _Requirements: 6.9, 6.10, 11.1, 11.2, 11.3, 11.4, 11.7_

- [ ] 18. Implement Snackbar feedback for CRUD operations
  - For `ManageListingsScreen`, `HealthRecordsScreen`, `RemindersScreen`, `AppointmentsScreen`, and `AddEntityScreen`:
    - Add `SnackbarHostState` and `SnackbarHost` to each screen's scaffold
    - Observe `actionState.isSuccess` — show success Snackbar (e.g., "Deleted successfully", "Reminder created")
    - Observe `actionState.error` — show error Snackbar with the error message
  - _Requirements: 6.7, 6.8, 11.5, 11.6_

- [ ] 19. Fix input validation on auth screens
  - Modify `ui/screens/auth/LoginScreen.kt`: validate phone number is not blank before calling `viewModel.login()`; display inline error if blank
  - Modify `ui/screens/auth/RegisterScreen.kt`: validate name, phone, and password are not blank; validate password matches confirm password; validate phone matches Bangladeshi format (starts with "01", 11 digits); display inline errors
  - Disable all form inputs and submit button when `state.isLoading == true` on Login, Register, and ForgotPassword screens
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.9_

- [ ] 20. Fix navigation correctness
  - Verify `SplashScreen` navigation removes the splash route from the back stack (`popUpTo("splash") { inclusive = true }`)
  - Verify `IntroScreen` navigation to Login removes the intro route from the back stack
  - Verify logout clears the entire back stack and navigates to Login
  - Verify `blood_org` route redirects to `browse_blood_orgs` without a duplicate back stack entry
  - Verify `details` route handles null/empty optional parameters without crashing
  - Verify `AppointmentsScreen` shows booking form immediately when opened with `initialDoctorName != null`
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8_

- [ ] 21. Fix `DashboardScreen` marquee text encoding
  - Modify `DashboardScreen.kt`
  - Replace the `â€¢` Unicode artifact in the blood request marquee text with the correct `•` bullet character (`\u2022`)
  - _Requirements: 15.8_

- [ ] 22. Apply UI design consistency across all screens
  - Audit all screens for the following and fix any deviations:
    - `Primary` color (`#0EA5E9`) used for all primary action buttons, active icons, and interactive elements
    - `RoundedCornerShape(20.dp)` or larger on all card surfaces
    - `AppBackground` or `AuthBackground` as root composable on every screen
    - `PremiumTopBar` on all secondary (non-tab) screens
    - `TextPrimary`, `TextSecondary`, `TextHint` color tokens for text hierarchy
    - `Surface` with `shadowElevation` 2dp–8dp for card components
    - `statusBarsPadding()` on screens with a custom top bar
    - `navigationBarsPadding()` on screens with bottom content (FABs, bottom buttons)
    - `FontWeight.ExtraBold` for screen titles, `FontWeight.Bold` for section headers
    - `ErrorColor` for all destructive action icons and error text
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10_

- [ ] 23. Apply accessibility improvements across all screens
  - Add `contentDescription` to all `Icon` and `IconButton` composables that convey meaningful information
  - Ensure all interactive elements have a minimum touch target of 48dp × 48dp (use `Modifier.size(48.dp)` or `minimumInteractiveComponentSize()`)
  - Ensure status badges include text labels in addition to color
  - Verify form content scrolls when the keyboard is open (use `verticalScroll` + `imePadding()` on form columns)
  - _Requirements: 14.1, 14.2, 14.3, 14.7_

- [ ] 24. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests use **Kotest** with its `PropTest` module (minimum 100 iterations each), tagged with `// Feature: android-app-ui-improvements, Property {N}: {property_text}`
- Unit and Compose UI tests complement property tests for specific examples and edge cases
- `NotificationScheduler` requires Robolectric or a physical/emulator device for full alarm integration testing
- Android 12 SplashScreen API and `POST_NOTIFICATIONS` permission flow require manual verification on appropriate API levels
