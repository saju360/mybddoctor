import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",       label: "ID" },
  { key: "name",     label: "Name" },
  { key: "district", label: "District" },
  { key: "phone",    label: "Phone" },
  { key: "address",  label: "Address" },
];

const FIELDS = [
  { key: "name",     label: "Name",     required: true },
  { key: "district", label: "District", required: true },
  { key: "phone",    label: "Phone" },
  { key: "address",  label: "Address" },
];

export default function BloodOrgsPage() {
  return (
    <CrudPage title="Blood Organizations" endpoint="/blood-organizations" columns={COLUMNS} formFields={FIELDS} />
  );
}
