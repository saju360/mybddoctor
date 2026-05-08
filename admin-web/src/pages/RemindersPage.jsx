import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",           label: "ID" },
  { key: "userId",       label: "User ID" },
  { key: "medicineName", label: "Medicine" },
  { key: "reminderTime", label: "Time" },
  { key: "dosage",       label: "Dosage" },
  { key: "frequency",    label: "Frequency" },
  { key: "active",       label: "Active",
    render: v => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
];

const FIELDS = [
  { key: "medicineName", label: "Medicine Name", required: true },
  { key: "reminderTime", label: "Reminder Time", required: true, placeholder: "e.g. 08:00 AM" },
  { key: "dosage",       label: "Dosage",        placeholder: "e.g. 1 tablet" },
  { key: "frequency",    label: "Frequency",     type: "select",
    options: ["DAILY","TWICE_DAILY","THREE_TIMES_DAILY","WEEKLY","AS_NEEDED"] },
  { key: "active",       label: "Active",        type: "checkbox", default: true },
];

export default function RemindersPage() {
  return (
    <CrudPage
      title="Medicine Reminders"
      endpoint="/reminders"
      columns={COLUMNS}
      formFields={FIELDS}
      canCreate={false}  // Created by users via app
      canEdit={false}    // User-owned data: admin panel uses read-only view
      canDelete={false}
    />
  );
}
