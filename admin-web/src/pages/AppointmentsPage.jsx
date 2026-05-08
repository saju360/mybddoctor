import React from "react";
import CrudPage from "./CrudPage";

const STATUS_OPTIONS = ["PENDING","CONFIRMED","CANCELLED","COMPLETED"];
const STATUS_COLORS  = { PENDING:"badge-amber", CONFIRMED:"badge-green", CANCELLED:"badge-red", COMPLETED:"badge-blue" };

const COLUMNS = [
  { key: "id",              label: "ID" },
  { key: "userId",          label: "User ID" },
  { key: "doctorId",        label: "Doctor ID" },
  { key: "appointmentDate", label: "Date" },
  { key: "timeSlot",        label: "Time" },
  { key: "status",          label: "Status",
    render: (v) => <span className={`badge ${STATUS_COLORS[v]||""}`}>{v}</span> },
  { key: "notes",           label: "Notes" },
];

const FIELDS = [
  { key: "appointmentDate", label: "Date",      required: true, placeholder: "YYYY-MM-DD" },
  { key: "timeSlot",        label: "Time Slot", placeholder: "10:00 AM" },
  { key: "status",          label: "Status",    type: "select", options: STATUS_OPTIONS },
  { key: "notes",           label: "Notes",     type: "textarea" },
];

export default function AppointmentsPage() {
  return (
    <CrudPage
      title="Appointments"
      endpoint="/appointments"
      columns={COLUMNS}
      formFields={FIELDS}
      canCreate={false}  // Booked by users
    />
  );
}
