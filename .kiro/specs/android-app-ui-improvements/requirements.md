# Requirements Document

## Introduction

This document defines the requirements for improving the LifePlus Healthcare Android application. The app is built with Kotlin and Jetpack Compose using MVVM architecture. The improvements cover four areas: (1) UI/UX redesign of key screens (Splash, Intro/Onboarding, Forgot Password), (2) missing features and option widgets (Settings screen, preferences UI), (3) CRUD functionality completeness and correctness across all entity types, and (4) bug verification and fixes across all screens.

The goal is to deliver a polished, consistent, and fully functional healthcare app that meets user expectations for reliability, usability, and visual quality.

---

## Glossary

- **App**: The LifePlus Healthcare Android application.
- **SplashScreen**: The first screen shown on app launch, displaying the brand logo and name.
- **IntroScreen**: The multi-page onboarding screen shown to first-time users.
- **ForgotPasswordScreen**: The screen allowing users to reset their password via OTP.
- **SettingsScreen**: A dedicated full-screen settings page for app preferences.
- **ProfileScreen**: The user profile screen containing account info and navigation to settings.
- **CRUD**: Create, Read, Update, Delete — the four basic data operations.
- **Entity**: Any managed healthcare resource (Hospital, Clinic, Pharmacy, Ambulance, BloodBank, Diagnostic, BloodOrganization, Doctor, Donor).
- **ViewModel**: An MVVM ViewModel managing UI state and business logic.
- **OTP**: One-Time Password sent to the user's phone for identity verification.
- **InputField**: The reusable Compose text input component used across forms.
- **PrimaryButton**: The reusable Compose primary action button component.
- **AppBackground**: The reusable Compose background wrapper component.
- **NavController**: Jetpack Navigation component managing screen transitions.
- **ManageListingsScreen**: The screen where logged-in users manage their registered entities.
- **AddEntityScreen**: The screen for creating or editing a healthcare entity.
- **SnackbarHost**: The Material 3 component for displaying transient feedback messages.
- **ConfirmDialog**: A reusable AlertDialog for confirming destructive actions.
- **HealthRecord**: A user-uploaded medical document (prescription, lab report, etc.).
- **MedicineReminder**: A scheduled medicine dose reminder with name, dosage, and time.
- **Appointment**: A booked consultation slot with a doctor.
- **AuthViewModel**: The ViewModel managing authentication state (login, register, OTP, password reset).
- **LanguageViewModel**: The ViewModel managing app language preference (English/Bangla).

---

## Requirements

### Requirement 1: Splash Screen Redesign

**User Story:** As a user, I want to see a modern, branded splash screen when I open the app, so that I have a positive first impression and the app feels professional.

#### Acceptance Criteria

1. THE SplashScreen SHALL display the LifePlus brand logo centered on screen using the app's primary color gradient background.
2. WHEN the SplashScreen is displayed, THE SplashScreen SHALL animate the logo with a spring-based scale-in effect within 400ms of launch.
3. WHEN the logo animation completes, THE SplashScreen SHALL animate the app name "LifePlus" with a fade-in effect.
4. WHEN the app name animation completes, THE SplashScreen SHALL animate the tagline text with a fade-in effect.
5. THE SplashScreen SHALL display the app name as "LifePlus" (not "MediCare") to match the brand identity used throughout the rest of the app.
6. WHEN all animations complete, THE SplashScreen SHALL navigate to the correct destination (home, login, or intro) based on the user's authentication and onboarding state.
7. THE SplashScreen SHALL complete its full animation sequence and navigate within 3500ms of launch.
8. IF the device is running Android 12 or higher, THE SplashScreen SHALL use the Android 12 SplashScreen API for the initial system splash, then transition to the custom animated screen.
9. THE SplashScreen SHALL display a version number or build indicator in small text at the bottom of the screen.
10. THE SplashScreen SHALL apply edge-to-edge display with no system bar padding, filling the entire screen.

---

### Requirement 2: Intro/Onboarding Screen Enhancement

**User Story:** As a first-time user, I want an engaging onboarding experience that clearly explains the app's key features, so that I understand the value of LifePlus before signing up.

#### Acceptance Criteria

1. THE IntroScreen SHALL display exactly 4 onboarding pages covering: healthcare access, emergency support, medical records, and health tools.
2. WHEN a user swipes or taps "Continue", THE IntroScreen SHALL animate the transition to the next page using a horizontal slide animation.
3. THE IntroScreen SHALL display an animated Lottie illustration on each page that loops continuously.
4. IF the Lottie animation URL fails to load, THE IntroScreen SHALL display a fallback static icon relevant to the page topic.
5. THE IntroScreen SHALL display a page progress indicator showing the current page position using animated dot indicators.
6. WHEN the user is on the last page, THE IntroScreen SHALL replace the "Continue" button label with "Get Started".
7. WHEN the user taps "Get Started" on the last page, THE IntroScreen SHALL call `viewModel.completeOnboarding()` and navigate to the login screen.
8. WHEN the user taps "Skip" on any page except the last, THE IntroScreen SHALL call `viewModel.completeOnboarding()` and navigate to the home screen as a guest.
9. THE IntroScreen SHALL display a "Continue as Guest" option that is visually distinct from the primary action button.
10. THE IntroScreen SHALL apply `statusBarsPadding()` and `navigationBarsPadding()` to avoid content overlap with system bars.
11. WHEN the pager state changes, THE IntroScreen SHALL animate the progress dot width from 8dp (inactive) to 28dp (active) using `animateDpAsState`.
12. THE IntroScreen SHALL display the LifePlus logo and brand name in the header row on every page.

---

### Requirement 3: Forgot Password Screen Redesign

**User Story:** As a user who has forgotten their password, I want a clear, step-by-step password reset flow, so that I can regain access to my account without confusion.

#### Acceptance Criteria

1. THE ForgotPasswordScreen SHALL present the password reset flow in two clearly labeled steps: "Step 1: Request OTP" and "Step 2: Set New Password".
2. WHEN the user is on Step 1, THE ForgotPasswordScreen SHALL display only the phone number input field and the "Send OTP" button.
3. WHEN the OTP is successfully sent (`state.otpSent == true`), THE ForgotPasswordScreen SHALL transition to Step 2 and display the OTP input, new password input, and confirm password input fields.
4. THE ForgotPasswordScreen SHALL use `AuthBackground` as its background wrapper for visual consistency with the Login and Register screens.
5. THE ForgotPasswordScreen SHALL display a back navigation button that returns the user to the Login screen.
6. WHEN the user taps "Send OTP", THE ForgotPasswordScreen SHALL show a loading indicator on the button while `state.isLoading` is true.
7. IF `state.error` is not null, THE ForgotPasswordScreen SHALL display the error message in red text below the action button.
8. WHEN `state.passwordResetSuccess` becomes true, THE ForgotPasswordScreen SHALL automatically navigate back to the Login screen.
9. THE ForgotPasswordScreen SHALL validate that the new password and confirm password fields match before calling `viewModel.forgotPasswordReset()`, and SHALL display an inline error message if they do not match.
10. THE ForgotPasswordScreen SHALL mask password input fields by default and provide a visibility toggle icon for each password field.
11. THE ForgotPasswordScreen SHALL display a "Resend OTP" option on Step 2 that is enabled only after a 60-second cooldown timer.
12. THE ForgotPasswordScreen SHALL display a step progress indicator (e.g., "1 of 2" / "2 of 2") at the top of the screen.

---

### Requirement 4: Dedicated Settings Screen

**User Story:** As a user, I want a dedicated settings screen accessible from my profile, so that I can manage app preferences in a clear and organized way rather than through a dialog popup.

#### Acceptance Criteria

1. THE App SHALL provide a dedicated `SettingsScreen` composable as a full-screen destination in the navigation graph.
2. WHEN the user taps the settings icon on the ProfileScreen, THE App SHALL navigate to the `SettingsScreen` route instead of showing an AlertDialog.
3. THE SettingsScreen SHALL display a language toggle option that switches between English and Bangla, persisted via `LanguageViewModel`.
4. THE SettingsScreen SHALL display a push notifications toggle that persists the user's preference to local storage.
5. THE SettingsScreen SHALL display a dark mode toggle option (UI only, with a "Coming Soon" badge if not yet implemented).
6. THE SettingsScreen SHALL display the app version number retrieved from the BuildConfig.
7. THE SettingsScreen SHALL display a "Privacy Policy" option that opens the policy URL in the device browser.
8. THE SettingsScreen SHALL display a "Terms of Service" option that opens the terms URL in the device browser.
9. THE SettingsScreen SHALL display a "Rate the App" option that opens the Google Play Store listing.
10. THE SettingsScreen SHALL display a "Contact Support" option that opens an email intent pre-filled with the support address.
11. THE SettingsScreen SHALL use `PremiumTopBar` with a back navigation button.
12. WHEN the user changes the language setting, THE SettingsScreen SHALL apply the language change immediately without requiring an app restart.

---

### Requirement 5: Option Widgets and Preference UI Components

**User Story:** As a developer, I want a set of reusable settings/preference UI components, so that consistent option widgets can be used across the Settings screen and other preference areas.

#### Acceptance Criteria

1. THE App SHALL provide a reusable `SettingsToggleItem` composable that displays a label, optional subtitle, and a `Switch` widget.
2. THE App SHALL provide a reusable `SettingsClickItem` composable that displays a label, optional subtitle, a leading icon, and a trailing chevron arrow.
3. THE App SHALL provide a reusable `SettingsSectionHeader` composable that displays a section title with consistent typography and spacing.
4. THE App SHALL provide a reusable `SettingsDropdownItem` composable that displays a label and a dropdown selector for choosing from a list of options.
5. WHEN a `SettingsToggleItem` switch is changed, THE `SettingsToggleItem` SHALL invoke the provided `onCheckedChange` callback with the new boolean value.
6. WHEN a `SettingsClickItem` is tapped, THE `SettingsClickItem` SHALL invoke the provided `onClick` callback.
7. THE `SettingsToggleItem` SHALL use the app's `Primary` color for the checked track and thumb to maintain design consistency.
8. ALL settings widget composables SHALL accept a `Modifier` parameter for layout flexibility.
9. THE App SHALL group settings widgets into logical sections (Account, Appearance, Notifications, About) using `SettingsSectionHeader`.

---

### Requirement 6: CRUD Completeness for Entity Management

**User Story:** As a registered user, I want to fully create, view, update, and delete my healthcare entity listings, so that I can keep my registered services accurate and up to date.

#### Acceptance Criteria

1. THE AddEntityScreen SHALL pre-populate all form fields with existing entity data when `entityId` is not null and the entity data has loaded.
2. WHEN the user submits the form with `entityId` not null, THE AddEntityScreen SHALL call the appropriate ViewModel `update()` method, not `create()`.
3. WHEN the user submits the form with `entityId` null, THE AddEntityScreen SHALL call the appropriate ViewModel `create()` method.
4. THE AddEntityScreen SHALL validate that the `name` field is not blank before allowing form submission, and SHALL display an inline error message if blank.
5. THE AddEntityScreen SHALL validate that a district other than the placeholder "Select District" is chosen before allowing form submission, and SHALL display an inline error message if not selected.
6. WHEN a delete action is triggered from `ManageListingsScreen`, THE App SHALL display a `ConfirmDialog` asking the user to confirm the deletion before calling the ViewModel `delete()` method.
7. WHEN a CRUD operation succeeds, THE App SHALL display a `Snackbar` with a success message (e.g., "Hospital updated successfully").
8. WHEN a CRUD operation fails, THE App SHALL display a `Snackbar` with the error message from the ViewModel state.
9. THE ManageListingsScreen SHALL display a loading indicator while entity data is being fetched.
10. THE ManageListingsScreen SHALL display an error state with a retry button if entity data fails to load.
11. THE AddEntityScreen SHALL support all 7 entity types: hospital, clinic, pharmacy, ambulance, blood_bank, diagnostic, blood_org.
12. WHEN the AddEntityScreen form is submitted successfully, THE AddEntityScreen SHALL navigate back to the ManageListingsScreen.

---

### Requirement 7: CRUD Completeness for Health Records

**User Story:** As a user, I want to create, view, and delete my health records, so that I can maintain a complete and organized medical history.

#### Acceptance Criteria

1. THE HealthRecordsScreen SHALL display all health records belonging to the logged-in user, loaded via `HealthRecordViewModel`.
2. WHEN the user taps the upload FAB, THE HealthRecordsScreen SHALL display the `AddRecordDialog` for entering record details.
3. THE `AddRecordDialog` SHALL allow the user to select a document image from the device gallery.
4. WHEN the user confirms the `AddRecordDialog` with a non-blank title, THE HealthRecordsScreen SHALL call `viewModel.create()` with the title, type, and image URI.
5. WHEN the user taps the delete icon on a record, THE HealthRecordsScreen SHALL display a confirmation dialog before calling `viewModel.delete()`.
6. THE HealthRecordsScreen SHALL display a record type filter (All, Lab, Prescription, Vaccination) to allow users to filter their records.
7. WHEN a health record has an associated image URL, THE HealthRecordsScreen SHALL display the image within the record card using `AsyncImage`.
8. IF the user is not logged in, THE HealthRecordsScreen SHALL display a prompt to log in instead of the records list.

---

### Requirement 8: CRUD Completeness for Medicine Reminders

**User Story:** As a user, I want to create, toggle, and delete medicine reminders, so that I never miss a scheduled dose.

#### Acceptance Criteria

1. THE RemindersScreen SHALL display all medicine reminders for the logged-in user, loaded via `ReminderViewModel`.
2. WHEN the user taps the FAB, THE RemindersScreen SHALL display the `AddReminderDialog`.
3. THE `AddReminderDialog` SHALL provide a time picker for selecting the reminder time instead of a plain text input field.
4. WHEN the user confirms the `AddReminderDialog` with a non-blank medicine name, THE RemindersScreen SHALL call `viewModel.create()`.
5. WHEN the user toggles the switch on a reminder card, THE RemindersScreen SHALL call `viewModel.toggle()` with the updated reminder.
6. WHEN the user taps the delete icon on a reminder card, THE RemindersScreen SHALL display a confirmation dialog before calling `viewModel.delete()`.
7. WHEN a reminder is active, THE App SHALL schedule a local device notification for the reminder time using Android's `AlarmManager` or `WorkManager`.
8. WHEN a reminder is toggled off, THE App SHALL cancel the corresponding scheduled local notification.

---

### Requirement 9: CRUD Completeness for Appointments

**User Story:** As a user, I want to book, view, and cancel appointments, so that I can manage my medical consultations efficiently.

#### Acceptance Criteria

1. THE AppointmentsScreen SHALL display all appointments for the logged-in user in a list, loaded via `AppointmentViewModel`.
2. WHEN the user taps the FAB, THE AppointmentsScreen SHALL navigate to the doctor browse screen to select a doctor for booking.
3. WHEN a doctor is selected from the browse screen, THE AppointmentsScreen SHALL display the booking form pre-filled with the doctor's name and specialty.
4. THE booking form SHALL allow the user to select a date using a `DatePickerDialog` and a time slot from a predefined list.
5. WHEN the user confirms the booking, THE AppointmentsScreen SHALL display the `PaymentDialog` before calling `viewModel.book()`.
6. WHEN the user taps "Cancel Appointment" on a PENDING appointment card, THE AppointmentsScreen SHALL display a confirmation dialog before calling `viewModel.cancel()`.
7. THE AppointmentsScreen SHALL display appointments grouped by status: upcoming (PENDING/CONFIRMED) and past (COMPLETED/CANCELLED).
8. WHEN an appointment is successfully booked, THE AppointmentsScreen SHALL display a success Snackbar and refresh the appointments list.

---

### Requirement 10: Input Validation Across All Forms

**User Story:** As a user, I want clear validation feedback on all forms, so that I understand what information is required and can correct mistakes before submitting.

#### Acceptance Criteria

1. THE LoginScreen SHALL validate that the phone number field is not blank before calling `viewModel.login()`, and SHALL display an inline error if blank.
2. THE RegisterScreen SHALL validate that the name, phone, and password fields are not blank before calling `viewModel.register()`.
3. THE RegisterScreen SHALL validate that the password and confirm password fields match, and SHALL display an inline error message if they do not.
4. THE RegisterScreen SHALL validate that the phone number matches the Bangladeshi format (starts with 01, 11 digits total), and SHALL display an inline error if invalid.
5. THE ForgotPasswordScreen SHALL validate that the phone number field is not blank before calling `viewModel.forgotPasswordRequest()`.
6. THE AddEntityScreen SHALL validate that the name field is not blank and a valid district is selected before allowing submission.
7. THE AddReminderDialog SHALL validate that the medicine name field is not blank before allowing confirmation.
8. WHEN a validation error occurs, THE App SHALL display the error message inline below the relevant input field using red text.
9. WHEN a form is submitted and `state.isLoading` is true, THE App SHALL disable all form input fields and the submit button to prevent duplicate submissions.

---

### Requirement 11: Consistent Error and Loading States

**User Story:** As a user, I want consistent feedback when data is loading or an error occurs, so that I always know the current state of the app.

#### Acceptance Criteria

1. WHEN any screen is loading data, THE App SHALL display a `CircularProgressIndicator` centered in the content area using the `Primary` color.
2. WHEN a network or server error occurs on a list screen, THE App SHALL display an error message and a "Retry" button.
3. WHEN the user taps "Retry", THE App SHALL re-invoke the relevant ViewModel load function.
4. WHEN a list screen has no data and is not loading, THE App SHALL display an empty state illustration with a descriptive message.
5. WHEN a CRUD action (create, update, delete) succeeds, THE App SHALL display a `Snackbar` with a success message.
6. WHEN a CRUD action fails, THE App SHALL display a `Snackbar` with the error message.
7. THE App SHALL not display both a loading indicator and an error state simultaneously on the same screen.

---

### Requirement 12: Navigation and Deep Link Correctness

**User Story:** As a user, I want all navigation actions to work correctly, so that I can move between screens without unexpected behavior or crashes.

#### Acceptance Criteria

1. WHEN the user taps the back button on any secondary screen, THE App SHALL navigate to the correct previous screen without duplicating back stack entries.
2. WHEN the user navigates from the SplashScreen, THE App SHALL remove the splash route from the back stack so the user cannot navigate back to it.
3. WHEN the user navigates from the IntroScreen to Login, THE App SHALL remove the intro route from the back stack.
4. WHEN the user logs out, THE App SHALL navigate to the Login screen and clear the entire back stack.
5. WHEN the user navigates to a feature screen that requires authentication and is not logged in, THE App SHALL redirect to the Login screen.
6. THE App SHALL handle the `blood_org` route by redirecting to `browse_blood_orgs` without creating a duplicate back stack entry.
7. WHEN the `AppointmentsScreen` is opened with `initialDoctorName` not null, THE AppointmentsScreen SHALL immediately display the booking form.
8. THE App SHALL not crash when navigating to `details` route with null or empty optional parameters.

---

### Requirement 13: UI Design Consistency

**User Story:** As a user, I want a visually consistent app experience across all screens, so that the app feels polished and professional.

#### Acceptance Criteria

1. THE App SHALL use the `Primary` color (`#0EA5E9`) consistently for all primary action buttons, active icons, and interactive elements.
2. THE App SHALL use `RoundedCornerShape(20.dp)` or larger for all card surfaces to maintain the premium rounded aesthetic.
3. THE App SHALL use `AppBackground` or `AuthBackground` as the root composable on every screen to ensure consistent background styling.
4. THE App SHALL use `PremiumTopBar` on all secondary screens (non-tab screens) for consistent header styling with back navigation.
5. THE App SHALL use `TextPrimary`, `TextSecondary`, and `TextHint` color tokens consistently for text hierarchy across all screens.
6. THE App SHALL use `Surface` with `shadowElevation` of 2dp–8dp for card components to provide consistent depth.
7. THE App SHALL apply `statusBarsPadding()` on all screens that have a custom top bar to prevent content from appearing behind the status bar.
8. THE App SHALL apply `navigationBarsPadding()` on all screens with bottom content (FABs, bottom buttons) to prevent overlap with the navigation bar.
9. THE App SHALL use `FontWeight.ExtraBold` for screen titles and `FontWeight.Bold` for section headers consistently.
10. THE App SHALL use `ErrorColor` for all destructive action icons (delete buttons) and error text consistently.

---

### Requirement 14: Accessibility and Usability

**User Story:** As a user with accessibility needs, I want the app to be usable with assistive technologies, so that I can access healthcare services regardless of my abilities.

#### Acceptance Criteria

1. THE App SHALL provide `contentDescription` strings for all `Icon` composables that convey meaningful information.
2. THE App SHALL provide `contentDescription` strings for all `IconButton` composables.
3. THE App SHALL ensure all interactive elements have a minimum touch target size of 48dp × 48dp.
4. THE App SHALL ensure text contrast ratios meet WCAG AA standards (minimum 4.5:1 for normal text, 3:1 for large text) against their backgrounds.
5. THE App SHALL support system font size scaling so that text remains readable when the user increases the system font size.
6. WHEN the keyboard is open, THE App SHALL scroll form content so that the focused input field remains visible above the keyboard.
7. THE App SHALL not rely solely on color to convey state information (e.g., status badges SHALL include text labels in addition to color).

---

### Requirement 15: Bug Fixes — Known Issues

**User Story:** As a user, I want the app to work reliably without crashes or incorrect behavior, so that I can trust it for my healthcare needs.

#### Acceptance Criteria

1. THE SplashScreen SHALL display the app name as "LifePlus" instead of the hardcoded "MediCare" string.
2. THE ForgotPasswordScreen SHALL show the OTP, new password, and confirm password fields only after `state.otpSent` becomes true, not unconditionally from the start.
3. THE ForgotPasswordScreen SHALL display a clickable "Back to Login" text that navigates back, not a non-functional `Text` composable followed by a separate `PrimaryButton`.
4. THE ProfileScreen settings dialog SHALL be replaced by navigation to the dedicated `SettingsScreen`.
5. THE ProfileScreen push notifications toggle SHALL persist its state to local storage and not reset to `true` on every recomposition.
6. THE ManageListingsScreen delete action SHALL show a confirmation dialog before deleting, not delete immediately on icon tap.
7. THE AppointmentsScreen `FlowRow` implementation SHALL be replaced with the official `androidx.compose.foundation.layout.FlowRow` API (available in Compose 1.5+) to avoid layout measurement issues.
8. THE DashboardScreen `marqueeText` SHALL handle the Unicode character encoding issue in the blood request text (the `â€¢` artifact SHALL be replaced with the correct `•` bullet character).
9. WHEN the `AddEntityScreen` is opened in edit mode and entity data has not yet loaded, THE AddEntityScreen SHALL display a loading indicator instead of empty form fields.
10. THE `HealthRecordsScreen` record type filter SHALL be functional and filter the displayed records list, not just display as a UI element.
