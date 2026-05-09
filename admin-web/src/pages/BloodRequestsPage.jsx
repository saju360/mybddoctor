import React from "react";
import CrudPage from "./CrudPage";

const BG_OPTIONS = ["A_POS","A_NEG","B_POS","B_NEG","AB_POS","AB_NEG","O_POS","O_NEG"];
const STATUS_OPTIONS = ["OPEN","PENDING","ACCEPTED","REJECTED","FULFILLED","CANCELLED"];
const fmtBg = (v) => v ? v.replace("_POS","+").replace("_NEG","-") : "—";
const STATUS_COLORS = { 
  OPEN: "badge-amber", 
  PENDING: "badge-blue", 
  ACCEPTED: "badge-indigo", 
  REJECTED: "badge-red", 
  FULFILLED: "badge-green", 
  CANCELLED: "badge-gray" 
};

const COLUMNS = [
  { key: "id",                  label: "ID" },
  { key: "bloodGroup",          label: "Blood Group", render: fmtBg },
  { key: "patientName",         label: "Patient" },
  { key: "hospitalName",        label: "Hospital" },
  { key: "district",            label: "District" },
  { key: "urgency",             label: "Urgency",
    render: (v) => <span className={`badge ${v === "URGENT" ? "badge-red" : "badge-amber"}`}>{v || "Normal"}</span> },
  { key: "status",              label: "Status",
    render: (v) => <span className={`badge ${STATUS_COLORS[v]||""}`}>{v}</span> },
  { key: "donorId",             label: "Donor" },
  { key: "contactPhone",        label: "Phone" },
  { key: "createdAt",           label: "Date",
    render: (v) => v ? new Date(v).toLocaleDateString() : "—" },
];

const FIELDS = [
  { key: "bloodGroup",   label: "Blood Group", required: true, type: "select", options: BG_OPTIONS },
  { key: "district",     label: "District",    required: true },
  { key: "upazila",      label: "Upazila" },
  { key: "patientName",  label: "Patient Name" },
  { key: "hospitalName", label: "Hospital Name" },
  { key: "status",       label: "Status",      type: "select", options: STATUS_OPTIONS },
  { key: "donorId",      label: "Donor ID (Direct Request)", type: "number" },
  { key: "requestedByUserId", label: "Requester User ID", type: "number" },
  { key: "contactPhone", label: "Contact Phone" },
  { key: "notes",        label: "Notes",       type: "textarea" },
];

export default function BloodRequestsPage() {
  return (
    <CrudPage
      title="Blood Requests"
      endpoint="/blood-requests"
      columns={COLUMNS}
      formFields={FIELDS}
    />
  );
}
