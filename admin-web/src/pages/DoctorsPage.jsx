import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",                       label: "ID" },
  { key: "fullName",                 label: "Name" },
  { key: "specialty",                label: "Specialty" },
  { key: "hospitalId",               label: "Hospital ID" },
  { key: "qualifications",           label: "Qualifications" },
  { key: "phone",                    label: "Phone" },
  { key: "availableForTelemedicine", label: "Telemedicine",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
];

const FIELDS = [
  { key: "fullName",                 label: "Full Name",    required: true },
  { key: "specialty",                label: "Specialty",    required: true },
  { key: "hospitalId",               label: "Hospital ID",  required: true, type: "number" },
  { key: "qualifications",           label: "Qualifications" },
  { key: "chamberSchedule",          label: "Chamber Schedule" },
  { key: "phone",                    label: "Phone" },
  { key: "availableForTelemedicine", label: "Available for Telemedicine", type: "checkbox" },
];

export default function DoctorsPage() {
  return (
    <CrudPage title="Doctors" endpoint="/doctors" columns={COLUMNS} formFields={FIELDS} />
  );
}
