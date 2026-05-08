import React from "react";
import CrudPage from "./CrudPage";

const COLUMNS = [
  { key: "id", label: "ID" },
  { key: "entityType", label: "Entity Type" },
  { key: "entityId", label: "Entity ID" },
  { key: "userId", label: "User ID" },
  { key: "userName", label: "User Name" },
  {
    key: "rating",
    label: "Rating",
    render: (v) => <span className="badge badge-blue">{v}/5</span>,
  },
  {
    key: "comment",
    label: "Comment",
    render: (v) => <span title={v || ""}>{v || "—"}</span>,
  },
  { key: "createdAt", label: "Created" },
];

export default function ReviewsPage() {
  return (
    <CrudPage
      title="Reviews"
      endpoint="/reviews"
      columns={COLUMNS}
      formFields={[]}
      canCreate={false}
      canEdit={false}
      canDelete
    />
  );
}

