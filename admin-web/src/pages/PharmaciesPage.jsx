import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",       label: "ID" },
  { key: "name",     label: "Name" },
  { key: "district", label: "District" },
  { key: "upazila",  label: "Upazila" },
  { key: "phone",    label: "Phone" },
  { key: "open24h",  label: "24h Open",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "active",   label: "Active",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
];

const FIELDS = [
  { key: "name",     label: "Name",     required: true },
  { key: "district", label: "District", required: true },
  { key: "upazila",  label: "Upazila" },
  { key: "phone",    label: "Phone",    required: true },
  { key: "address",  label: "Address" },
  { key: "open24h",  label: "Open 24h", type: "checkbox" },
  { key: "active",   label: "Active",   type: "checkbox", default: true },
];

export default function PharmaciesPage() {
  return (
    <CrudPage title="Pharmacies" endpoint="/pharmacies" columns={COLUMNS} formFields={FIELDS} />
  );
}
