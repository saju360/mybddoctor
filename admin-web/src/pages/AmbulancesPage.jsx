import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",          label: "ID" },
  { key: "name",        label: "Provider" },
  { key: "district",    label: "District" },
  { key: "upazila",     label: "Upazila" },
  { key: "phone",       label: "Phone" },
  { key: "icuEquipped", label: "ICU",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "available",   label: "Available",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "status",      label: "Status",
    render: (v) => <span className={`badge ${v === "APPROVED" ? "badge-green" : v === "REJECTED" ? "badge-red" : "badge-amber"}`}>{v || "PENDING"}</span> },
];

const FIELDS = [
  { key: "name",        label: "Provider Name",  required: true },
  { key: "district",    label: "District",        required: true },
  { key: "upazila",     label: "Upazila" },
  { key: "phone",       label: "Phone",           required: true },
  { key: "icuEquipped", label: "ICU Equipped",    type: "checkbox" },
  { key: "available",   label: "Available",       type: "checkbox", default: true },
  { key: "status",      label: "Status", type: "select",
    options: ["PENDING", "APPROVED", "REJECTED"] },
  { key: "adminNotes",  label: "Admin Notes",     type: "textarea" },
];

export default function AmbulancesPage() {
  return (
    <CrudPage title="Ambulances" endpoint="/ambulances" columns={COLUMNS} formFields={FIELDS} />
  );
}
