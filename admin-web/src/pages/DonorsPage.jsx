import React from "react";
import CrudPage from "./CrudPage";

const BG_OPTIONS = ["A_POS","A_NEG","B_POS","B_NEG","AB_POS","AB_NEG","O_POS","O_NEG"];
const fmtBg = (v) => v ? v.replace("_POS","+").replace("_NEG","-") : "—";

const COLUMNS = [
  { key: "id",              label: "ID" },
  { key: "userId",          label: "User ID" },
  { key: "bloodGroup",      label: "Blood Group", render: fmtBg },
  { key: "district",        label: "District" },
  { key: "upazila",         label: "Upazila",       required: true },
  { key: "availableNow",    label: "Available",
    render: (v) => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Yes" : "No"}</span> },
  { key: "rewardPoints",    label: "Rewards", render: (v) => <span className="badge badge-blue">{v || 0} pts</span> },
  { key: "contactPhone",    label: "Phone" },
  { key: "lastDonationDate",label: "Last Donation" },
];

const FIELDS = [
  { key: "userId",          label: "User ID",       required: true, type: "number" },
  { key: "bloodGroup",      label: "Blood Group",   required: true, type: "select", options: BG_OPTIONS },
  { key: "district",        label: "District",      required: true },
  { key: "upazila",         label: "Upazila" },
  { key: "contactPhone",    label: "Contact Phone" },
  { key: "lastDonationDate",label: "Last Donation Date", placeholder: "YYYY-MM-DD" },
  { key: "availableNow",    label: "Available Now", type: "checkbox", default: true },
  { key: "rewardPoints",    label: "Reward Points", type: "number", default: 0 },
  { key: "physicalHistory", label: "Physical History", type: "textarea" },
];

export default function DonorsPage() {
  return (
    <CrudPage
      title="Donors"
      endpoint="/donors"
      columns={COLUMNS}
      formFields={FIELDS}
    />
  );
}
