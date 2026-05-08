import React, { useEffect, useState, useCallback } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import { useToast } from "../components/Toast";

const STATUS_COLORS = {
  OPEN:       "badge-red",
  PENDING:    "badge-amber",
  DISPATCHED: "badge-blue",
  RESOLVED:   "badge-green",
  CANCELLED:  "badge-red",
};

const TYPE_ICONS = {
  "Heart Attack": "❤️",
  "Accident":     "🚗",
  "Breathing":    "🫁",
  "Fire":         "🔥",
  "Other":        "🆘",
};

const COLUMNS = [
  { key: "id",            label: "ID" },
  { key: "callerName",    label: "Caller" },
  { key: "phone",         label: "Phone" },
  { key: "district",      label: "District" },
  { key: "emergencyType", label: "Type",
    render: (v) => (
      <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <span>{TYPE_ICONS[v] || "🆘"}</span>
        <span className="badge badge-red">{v}</span>
      </span>
    )
  },
  { key: "description",   label: "Description",
    render: (v) => v
      ? <span style={{ color: "var(--text2)", fontSize: 12, maxWidth: 200, display: "block", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{v}</span>
      : "—"
  },
  { key: "status",        label: "Status",
    render: (v) => <span className={`badge ${STATUS_COLORS[v] || "badge-amber"}`}>{v}</span>
  },
  { key: "createdAt",     label: "Received",
    render: (v) => v ? new Date(v).toLocaleString() : "—"
  },
];

export default function EmergencyPage() {
  const toast = useToast();
  const [data,    setData]    = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter,  setFilter]  = useState("OPEN");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get("/emergency-requests");
      setData(Array.isArray(res.data) ? res.data : []);
    } catch {
      toast.error("Failed to load emergency requests.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  // Auto-refresh every 30 seconds for emergencies
  useEffect(() => {
    const interval = setInterval(load, 30_000);
    return () => clearInterval(interval);
  }, [load]);

  async function updateStatus(id, status) {
    try {
      await api.put(`/emergency-requests/${id}`, { status });
      toast.success(`Emergency #${id} marked as ${status}.`);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || "Update failed.");
    }
  }

  const filtered = filter === "ALL" ? data : data.filter(d => d.status === filter || d.status === "PENDING");

  const openCount       = data.filter(d => d.status === "OPEN" || d.status === "PENDING").length;
  const dispatchedCount = data.filter(d => d.status === "DISPATCHED").length;
  const resolvedCount   = data.filter(d => d.status === "RESOLVED").length;

  const actions = [
    {
      label: "Dispatch",
      className: "btn-primary",
      onClick: (row) => {
        if (row.status === "RESOLVED" || row.status === "CANCELLED") {
          toast.error("Cannot dispatch a resolved/cancelled request.");
          return;
        }
        updateStatus(row.id, "DISPATCHED");
      },
    },
    {
      label: "Resolve",
      className: "btn-success",
      onClick: (row) => updateStatus(row.id, "RESOLVED"),
    },
  ];

  return (
    <div className="page-content">
      <div className="page-header">
        <div>
          <h1>Emergency Requests</h1>
          <p className="page-subtitle">
            <span className="status-dot online" />
            Auto-refreshes every 30s
          </p>
        </div>
        <button className="btn-refresh" onClick={load}>↻ Refresh</button>
      </div>

      {/* Summary cards */}
      <div style={{ display: "flex", gap: 12, marginBottom: 20, flexWrap: "wrap" }}>
        {[
          { label: "Open",       count: openCount,       color: "var(--danger)",  filter: "OPEN" },
          { label: "Dispatched", count: dispatchedCount, color: "var(--primary)", filter: "DISPATCHED" },
          { label: "Resolved",   count: resolvedCount,   color: "var(--success)", filter: "RESOLVED" },
          { label: "All",        count: data.length,     color: "var(--text2)",   filter: "ALL" },
        ].map(({ label, count, color, filter: f }) => (
          <div key={f}
            onClick={() => setFilter(f)}
            style={{
              background: "var(--bg2)",
              border: `1px solid ${filter === f ? color : "var(--border)"}`,
              borderTop: `3px solid ${color}`,
              borderRadius: 12, padding: "14px 20px",
              cursor: "pointer", minWidth: 120,
              transition: "all 0.15s",
              boxShadow: filter === f ? `0 0 12px ${color}22` : "none",
            }}>
            <div style={{ fontSize: 24, fontWeight: 800, color }}>{count}</div>
            <div style={{ fontSize: 12, color: "var(--text2)", marginTop: 2 }}>{label}</div>
          </div>
        ))}
      </div>

      {/* Filter tabs */}
      <div className="filter-tabs">
        {["OPEN", "DISPATCHED", "RESOLVED", "ALL"].map((s) => (
          <button key={s}
            className={`filter-tab ${filter === s ? "active" : ""}`}
            onClick={() => setFilter(s)}>
            {s}
          </button>
        ))}
      </div>

      <DataTable
        columns={columns}
        data={filter === "ALL" ? data : data.filter(d =>
          filter === "OPEN" ? (d.status === "OPEN" || d.status === "PENDING") : d.status === filter
        )}
        actions={actions}
        loading={loading}
      />
    </div>
  );
}

// Fix: use the COLUMNS constant defined above
const columns = COLUMNS;
