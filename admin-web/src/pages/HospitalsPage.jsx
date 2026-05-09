import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",              label: "ID" },
  { key: "name",            label: "Name" },
  { key: "district",        label: "District" },
  { key: "upazila",         label: "Upazila" },
  { key: "address",         label: "Address" },
  { key: "phone",           label: "Phone" },
  { key: "icuAvailable",    label: "ICU",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "open24h",         label: "24h",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "status",          label: "Status",
    render: (v) => <span className={`badge ${v === "APPROVED" ? "badge-green" : v === "REJECTED" ? "badge-red" : "badge-amber"}`}>{v || "PENDING"}</span> },
];

const FIELDS = [
  { key: "name",         label: "Name",     required: true },
  { key: "district",     label: "District", required: true },
  { key: "upazila",      label: "Upazila" },
  { key: "address",      label: "Address" },
  { key: "phone",        label: "Phone" },
  { key: "icuAvailable", label: "ICU Available",  type: "checkbox" },
  { key: "open24h",      label: "Open 24 Hours",  type: "checkbox" },
  { key: "status",       label: "Status", type: "select",
    options: ["PENDING", "APPROVED", "REJECTED"] },
  { key: "adminNotes",   label: "Admin Notes", type: "textarea" },
];

export default function HospitalsPage() {
  return (
    <CrudPage title="Hospitals" endpoint="/hospitals" columns={COLUMNS} formFields={FIELDS} />
  );
}
