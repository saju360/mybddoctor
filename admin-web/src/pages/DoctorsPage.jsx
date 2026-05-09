import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",                       label: "ID" },
  { key: "fullName",                 label: "Name" },
  { key: "specialty",                label: "Specialty" },
  { key: "district",                 label: "District" },
  { key: "hospitalId",               label: "Hospital ID" },
  { key: "qualifications",           label: "Qualifications" },
  { key: "phone",                    label: "Phone" },
  { key: "consultationHours",        label: "Hours" },
  { key: "available",                label: "Available",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "availableForTelemedicine", label: "Telemedicine",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "status",                   label: "Status",
    render: (v) => <span className={`badge ${v === "APPROVED" ? "badge-green" : v === "REJECTED" ? "badge-red" : "badge-amber"}`}>{v || "PENDING"}</span> },
];

const FIELDS = [
  { key: "fullName",                 label: "Full Name",    required: true },
  { key: "specialty",                label: "Specialty",    required: true },
  { key: "hospitalId",               label: "Hospital ID",  required: true, type: "number" },
  { key: "district",                 label: "District" },
  { key: "qualifications",           label: "Qualifications" },
  { key: "consultationHours",        label: "Consultation Hours", placeholder: "10:00 AM - 05:00 PM" },
  { key: "phone",                    label: "Phone" },
  { key: "available",                label: "Currently Available",        type: "checkbox", default: true },
  { key: "availableForTelemedicine", label: "Available for Telemedicine", type: "checkbox" },
  { key: "status",                   label: "Status", type: "select",
    options: ["PENDING", "APPROVED", "REJECTED"] },
  { key: "adminNotes",               label: "Admin Notes", type: "textarea" },
];

export default function DoctorsPage() {
  return (
    <CrudPage title="Doctors" endpoint="/doctors" columns={COLUMNS} formFields={FIELDS} />
  );
}
