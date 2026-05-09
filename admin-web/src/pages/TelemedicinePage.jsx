import React from "react";
import CrudPage from "./CrudPage";

const STATUS_OPTIONS = ["SCHEDULED","IN_PROGRESS","COMPLETED","CANCELLED"];
const STATUS_COLORS  = { SCHEDULED:"badge-amber", IN_PROGRESS:"badge-blue", COMPLETED:"badge-green", CANCELLED:"badge-red" };

const COLUMNS = [
  { key: "id",            label: "ID" },
  { key: "doctorId",      label: "Doctor ID" },
  { key: "doctorName",    label: "Doctor" },
  { key: "patientUserId", label: "Patient ID" },
  { key: "date",          label: "Date" },
  { key: "time",          label: "Time" },
  { key: "platform",      label: "Platform" },
  { key: "status",        label: "Status",
    render: (v) => <span className={`badge ${STATUS_COLORS[v]||""}`}>{v}</span> },
  { key: "meetingLink",   label: "Meeting Link",
    render: (v) => v ? <a href={v} target="_blank" rel="noreferrer" className="link">Open</a> : "—" },
];

const FIELDS = [
  { key: "date",        label: "Date",         required: true, placeholder: "YYYY-MM-DD" },
  { key: "time",        label: "Time",         placeholder: "10:00 AM" },
  { key: "platform",    label: "Platform",     placeholder: "Google Meet, Zoom…" },
  { key: "meetingLink", label: "Meeting Link", placeholder: "https://meet.google.com/…" },
  { key: "status",      label: "Status",       type: "select", options: STATUS_OPTIONS },
];

export default function TelemedicinePage() {
  return (
    <CrudPage
      title="Telemedicine Sessions"
      endpoint="/telemedicine"
      columns={COLUMNS}
      formFields={FIELDS}
      canCreate={false}  // Booked by users
    />
  );
}
