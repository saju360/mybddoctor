import React from "react";

const COLOR_MAP = {
  OPEN:       "badge-blue",
  PENDING:    "badge-amber",
  APPROVED:   "badge-green",
  REJECTED:   "badge-red",
  MATCHED:    "badge-purple",
  FULFILLED:  "badge-green",
  CANCELLED:  "badge-red",
  DISPATCHED: "badge-blue",
  RESOLVED:   "badge-green",
  SCHEDULED:  "badge-blue",
  IN_PROGRESS:"badge-amber",
  COMPLETED:  "badge-green",
  CONFIRMED:  "badge-green",
  ACTIVE:     "badge-green",
  INACTIVE:   "badge-red",
  true:       "badge-green",
  false:      "badge-red",
};

export function StatusBadge({ value }) {
  const cls = COLOR_MAP[String(value)] || "badge-gray";
  return <span className={`status-badge ${cls}`}>{String(value)}</span>;
}
