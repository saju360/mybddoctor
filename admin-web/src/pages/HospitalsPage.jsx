import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",       label: "ID" },
  { key: "name",     label: "Name" },
  { key: "district", label: "District" },
  { key: "upazila",  label: "Upazila" },
  { key: "type",     label: "Type" },
  { key: "phone",    label: "Phone" },
  { key: "address",  label: "Address" },
];

const FIELDS = [
  { key: "name",     label: "Name",     required: true },
  { key: "district", label: "District", required: true },
  { key: "upazila",  label: "Upazila",  required: true },
  { key: "type",     label: "Type",     type: "select",
    options: ["Government","Private","NGO","Specialized"] },
  { key: "phone",    label: "Phone" },
  { key: "address",  label: "Address" },
];

export default function HospitalsPage() {
  return (
    <CrudPage title="Hospitals" endpoint="/hospitals" columns={COLUMNS} formFields={FIELDS} />
  );
}
