import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",       label: "ID" },
  { key: "name",     label: "Name" },
  { key: "district", label: "District" },
  { key: "upazila",  label: "Upazila" },
  { key: "phone",    label: "Phone" },
  { key: "address",  label: "Address" },
  { key: "specialties", label: "Specialties" },
  { key: "status",   label: "Status",
    render: (v) => <span className={`badge ${v === "APPROVED" ? "badge-green" : v === "REJECTED" ? "badge-red" : "badge-amber"}`}>{v || "PENDING"}</span> },
];

const FIELDS = [
  { key: "name",       label: "Name",       required: true },
  { key: "district",   label: "District",   required: true },
  { key: "upazila",    label: "Upazila" },
  { key: "phone",      label: "Phone" },
  { key: "address",    label: "Address" },
  { key: "specialties",label: "Specialties", placeholder: "Cardiology, Dental, General…" },
  { key: "status",     label: "Status", type: "select",
    options: ["PENDING", "APPROVED", "REJECTED"] },
  { key: "adminNotes", label: "Admin Notes", type: "textarea" },
];

export default function ClinicsPage() {
  return (
    <CrudPage title="Clinics" endpoint="/clinics" columns={COLUMNS} formFields={FIELDS} />
  );
}
