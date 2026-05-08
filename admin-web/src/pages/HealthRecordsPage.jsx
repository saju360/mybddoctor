import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id",           label: "ID" },
  { key: "userId",       label: "User ID" },
  { key: "recordType",   label: "Type",
    render: (v) => <span className="badge badge-blue">{v}</span> },
  { key: "recordDate",   label: "Date" },
  { key: "doctorName",   label: "Doctor" },
  { key: "facilityName", label: "Facility" },
  { key: "recordData",   label: "Data",
    render: (v) => v ? (v.length > 60 ? v.slice(0, 60) + "…" : v) : "—" },
];

export default function HealthRecordsPage() {
  return (
    <CrudPage
      title="Health Records"
      endpoint="/health-records"
      columns={COLUMNS}
      formFields={[]}
      canCreate={false}  // Created by users
      canEdit={false}
    />
  );
}
