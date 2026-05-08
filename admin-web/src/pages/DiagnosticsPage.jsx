import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",           label: "ID" },
  { key: "name",         label: "Name" },
  { key: "district",     label: "District" },
  { key: "upazila",      label: "Upazila" },
  { key: "testsOffered", label: "Tests Offered" },
  { key: "phone",        label: "Phone" },
  { key: "address",      label: "Address" },
];

const FIELDS = [
  { key: "name",         label: "Name",          required: true },
  { key: "district",     label: "District",      required: true },
  { key: "upazila",      label: "Upazila" },
  { key: "testsOffered", label: "Tests Offered", required: true, type: "textarea",
    placeholder: "CBC, Blood Sugar, X-Ray, ECG…" },
  { key: "phone",        label: "Phone" },
  { key: "address",      label: "Address" },
];

export default function DiagnosticsPage() {
  return (
    <CrudPage title="Diagnostic Centers" endpoint="/diagnostics" columns={COLUMNS} formFields={FIELDS} />
  );
}
