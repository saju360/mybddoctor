import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./components/Toast";
import RequireAdmin from "./components/RequireAdmin";
import Layout from "./components/Layout";

import LoginPage          from "./pages/LoginPage";
import DashboardPage      from "./pages/DashboardPage";
import ApprovalsPage      from "./pages/ApprovalsPage";
import UsersPage          from "./pages/UsersPage";
import DonorsPage         from "./pages/DonorsPage";
import BloodRequestsPage  from "./pages/BloodRequestsPage";
import HospitalsPage      from "./pages/HospitalsPage";
import DoctorsPage        from "./pages/DoctorsPage";
import ClinicsPage        from "./pages/ClinicsPage";
import AmbulancesPage     from "./pages/AmbulancesPage";
import PharmaciesPage     from "./pages/PharmaciesPage";
import DiagnosticsPage    from "./pages/DiagnosticsPage";
import BloodBanksPage     from "./pages/BloodBanksPage";
import BloodOrgsPage      from "./pages/BloodOrgsPage";
import EmergencyPage      from "./pages/EmergencyPage";
import AppointmentsPage   from "./pages/AppointmentsPage";
import TelemedicinePage   from "./pages/TelemedicinePage";
import RemindersPage      from "./pages/RemindersPage";
import HealthRecordsPage  from "./pages/HealthRecordsPage";
import SettingsPage       from "./pages/SettingsPage";
import SlidesPage         from "./pages/SlidesPage";
import WalkthroughPage    from "./pages/WalkthroughPage";
import NotificationsPage  from "./pages/NotificationsPage";
import AdsSettingsPage    from "./pages/AdsSettingsPage";
import ReviewsPage        from "./pages/ReviewsPage";
import ChatMonitorPage    from "./pages/ChatMonitorPage";

function AdminRoutes() {
  return (
    <RequireAdmin>
      <Layout>
        <Routes>
          <Route path="/"               element={<DashboardPage />} />
          <Route path="/approvals"      element={<ApprovalsPage />} />
          <Route path="/users"          element={<UsersPage />} />
          <Route path="/donors"         element={<DonorsPage />} />
          <Route path="/blood-requests" element={<BloodRequestsPage />} />
          <Route path="/hospitals"      element={<HospitalsPage />} />
          <Route path="/doctors"        element={<DoctorsPage />} />
          <Route path="/clinics"        element={<ClinicsPage />} />
          <Route path="/ambulances"     element={<AmbulancesPage />} />
          <Route path="/pharmacies"     element={<PharmaciesPage />} />
          <Route path="/diagnostics"    element={<DiagnosticsPage />} />
          <Route path="/blood-banks"    element={<BloodBanksPage />} />
          <Route path="/blood-orgs"     element={<BloodOrgsPage />} />
          <Route path="/emergency"      element={<EmergencyPage />} />
          <Route path="/appointments"   element={<AppointmentsPage />} />
          <Route path="/telemedicine"   element={<TelemedicinePage />} />
          <Route path="/reminders"      element={<RemindersPage />} />
          <Route path="/health-records" element={<HealthRecordsPage />} />
          <Route path="/slides"         element={<SlidesPage />} />
          <Route path="/walkthrough"    element={<WalkthroughPage />} />
          <Route path="/notifications"  element={<NotificationsPage />} />
          <Route path="/reviews"        element={<ReviewsPage />} />
          <Route path="/chat-monitor"   element={<ChatMonitorPage />} />
          <Route path="/ads-settings"   element={<AdsSettingsPage />} />
          <Route path="/settings"       element={<SettingsPage />} />
          <Route path="*"               element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </RequireAdmin>
  );
}

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/*"     element={<AdminRoutes />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
