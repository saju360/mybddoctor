import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",           label: "ID" },
  { key: "providerName", label: "Provider" },
  { key: "district",     label: "District" },
  { key: "phone",        label: "Phone" },
  { key: "vehicleNumber",label: "Vehicle No." },
  { key: "available",    label: "Available",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
];

const FIELDS = [
  { key: "providerName",  label: "Provider Name",  required: true },
  { key: "district",      label: "District",        required: true },
  { key: "phone",         label: "Phone",           required: true },
  { key: "vehicleNumber", label: "Vehicle Number" },
  { key: "available",     label: "Available",       type: "checkbox", default: true },
];

export default function AmbulancesPage() {
  return (
    <CrudPage title="Ambulances" endpoint="/ambulances" columns={COLUMNS} formFields={FIELDS} />
  );
}
