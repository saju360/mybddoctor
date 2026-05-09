import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",             label: "ID" },
  { key: "name",           label: "Name" },
  { key: "district",       label: "District" },
  { key: "upazila",        label: "Upazila" },
  { key: "phone",          label: "Phone" },
  { key: "address",        label: "Address" },
  { key: "donorCount",     label: "Donors" },
  { key: "availableGroups",label: "Available Groups",
    render: (v) => Array.isArray(v) ? v.map(g => g.replace("_POS","+").replace("_NEG","-")).join(", ") : "—" },
];

const FIELDS = [
  { key: "name",           label: "Name",     required: true },
  { key: "district",       label: "District", required: true },
  { key: "upazila",        label: "Upazila" },
  { key: "phone",          label: "Phone",    required: true },
  { key: "address",        label: "Address" },
  { key: "donorCount",     label: "Donor Count", type: "number", default: 0 },
];

export default function BloodBanksPage() {
  return (
    <CrudPage title="Blood Banks" endpoint="/blood-banks" columns={COLUMNS} formFields={FIELDS} />
  );
}
