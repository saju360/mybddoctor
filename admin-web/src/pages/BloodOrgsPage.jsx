import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",         label: "ID" },
  { key: "name",       label: "Name" },
  { key: "district",   label: "District" },
  { key: "upazila",    label: "Upazila" },
  { key: "phone",      label: "Phone" },
  { key: "address",    label: "Address" },
  { key: "donorCount", label: "Donors" },
];

const FIELDS = [
  { key: "name",       label: "Name",     required: true },
  { key: "district",   label: "District", required: true },
  { key: "upazila",    label: "Upazila" },
  { key: "phone",      label: "Phone" },
  { key: "address",    label: "Address" },
  { key: "donorCount", label: "Donor Count", type: "number", default: 0 },
];

export default function BloodOrgsPage() {
  return (
    <CrudPage title="Blood Organizations" endpoint="/blood-organizations" columns={COLUMNS} formFields={FIELDS} />
  );
}
