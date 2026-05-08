import React, { useEffect, useState, useCallback } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";
import { useToast } from "../components/Toast";

const STATUS_COLORS = {
  PENDING:  "badge-amber",
  APPROVED: "badge-green",
  REJECTED: "badge-red",
};

const ENTITY_ICONS = {
  hospital: "🏥", clinic: "🏨", doctor: "👨‍⚕️", ambulance: "🚑",
  pharmacy: "💊", diagnostic: "🔬", blood_bank: "🏦", blood_org: "🤝",
  donor: "🩸", user: "👤", blood_request: "💧",
};

export default function ApprovalsPage() {
  const toast = useToast();
  const [data,     setData]     = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [filter,   setFilter]   = useState("PENDING");
  const [selected, setSelected] = useState(new Set());
  const [rejectModal, setRejectModal] = useState(null); // { id }
  const [rejectNotes, setRejectNotes] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get("/approvals");
      setData(Array.isArray(res.data) ? res.data : []);
      setSelected(new Set());
    } catch {
      toast.error("Failed to load approvals.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  // ── Single actions ──────────────────────────────────────────────────────────

  async function handleApprove(id) {
    if (!window.confirm("Verify this donation and award 50 points to the donor?")) return;
    try {
      await api.put(`/approvals/${id}/approve`);
      toast.success(`Request #${id} approved and points awarded.`);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || "Approve failed.");
    }
  }

  async function handleReject(id, notes) {
    try {
      await api.put(`/approvals/${id}/reject`, notes ? { notes } : {});
      toast.success(`Request #${id} rejected.`);
      setRejectModal(null);
      setRejectNotes("");
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || "Reject failed.");
    }
  }

  // ── Bulk actions ────────────────────────────────────────────────────────────

  async function handleBulkApprove() {
    if (selected.size === 0) { toast.error("No items selected."); return; }
    if (!window.confirm(`Approve ${selected.size} requests?`)) return;
    try {
      await Promise.all([...selected].map(id => api.put(`/approvals/${id}/approve`)));
      toast.success(`${selected.size} requests approved.`);
      load();
    } catch (err) {
      toast.error("Bulk approve failed.");
    }
  }

  async function handleBulkReject() {
    if (selected.size === 0) { toast.error("No items selected."); return; }
    const notes = window.prompt(`Rejection reason for ${selected.size} requests (optional):`);
    if (notes === null) return; // cancelled
    try {
      await Promise.all([...selected].map(id =>
        api.put(`/approvals/${id}/reject`, notes ? { notes } : {})
      ));
      toast.success(`${selected.size} requests rejected.`);
      load();
    } catch (err) {
      toast.error("Bulk reject failed.");
    }
  }

  // ── Table config ────────────────────────────────────────────────────────────

  const filtered = filter === "ALL" ? data : data.filter(d => d.status === filter);

  const columns = [
    { key: "id",              label: "ID" },
    { key: "entityType",      label: "Entity",
      render: (v) => (
        <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <span>{ENTITY_ICONS[v?.toLowerCase()] || "📋"}</span>
          <span className="badge badge-blue">{v}</span>
        </span>
      )
    },
    { key: "entityId",        label: "Entity ID" },
    { key: "requestedRole",   label: "Role Requested",
      render: (v) => <span className="badge badge-purple">{v}</span> },
    { key: "requesterUserId", label: "Requester ID" },
    { key: "status",          label: "Status",
      render: (v) => <span className={`badge ${STATUS_COLORS[v] || ""}`}>{v}</span> },
    { key: "notes",           label: "Notes",
      render: (v) => v ? <span style={{ color: "var(--text2)", fontSize: 12 }}>{v}</span> : "—" },
    { key: "reviewedAt",      label: "Reviewed",
      render: (v) => v ? new Date(v).toLocaleDateString() : "—" },
    { key: "createdAt",       label: "Submitted",
      render: (v) => v ? new Date(v).toLocaleDateString() : "—" },
  ];

  const actions = [
    {
      label: "✓ Approve",
      className: "btn-success",
      onClick: (row) => {
        if (row.status !== "PENDING") { toast.error("Only PENDING requests can be approved."); return; }
        handleApprove(row.id);
      },
    },
    {
      label: "✕ Reject",
      className: "btn-danger",
      onClick: (row) => {
        if (row.status !== "PENDING") { toast.error("Only PENDING requests can be rejected."); return; }
        setRejectModal({ id: row.id });
        setRejectNotes("");
      },
    },
  ];

  const pendingCount   = data.filter(d => d.status === "PENDING").length;
  const approvedCount  = data.filter(d => d.status === "APPROVED").length;
  const rejectedCount  = data.filter(d => d.status === "REJECTED").length;

  return (
    <div className="page-content">
      <div className="page-header">
        <div>
          <h1>Approvals</h1>
          <p style={{ color: "var(--text2)", fontSize: 13, marginTop: 4 }}>
            Review and manage user join requests and entity approval requests
          </p>
        </div>
        <div className="header-actions">
          {selected.size > 0 && (
            <>
              <button className="btn-action btn-success" onClick={handleBulkApprove}>
                ✓ Approve {selected.size}
              </button>
              <button className="btn-action btn-danger" onClick={handleBulkReject}>
                ✕ Reject {selected.size}
              </button>
            </>
          )}
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
        </div>
      </div>

      {/* Summary cards */}
      <div style={{ display: "flex", gap: 12, marginBottom: 20, flexWrap: "wrap" }}>
        {[
          { label: "Pending",  count: pendingCount,  color: "#f59e0b", filter: "PENDING" },
          { label: "Approved", count: approvedCount, color: "#22c55e", filter: "APPROVED" },
          { label: "Rejected", count: rejectedCount, color: "#ef4444", filter: "REJECTED" },
          { label: "Total",    count: data.length,   color: "#3b82f6", filter: "ALL" },
        ].map(({ label, count, color, filter: f }) => (
          <div key={f}
            onClick={() => setFilter(f)}
            style={{
              background: "var(--bg2)", border: `1px solid ${filter === f ? color : "var(--border)"}`,
              borderRadius: 12, padding: "14px 20px", cursor: "pointer",
              borderTop: `3px solid ${color}`, minWidth: 120, transition: "all 0.15s",
            }}>
            <div style={{ fontSize: 24, fontWeight: 800, color }}>{count}</div>
            <div style={{ fontSize: 12, color: "var(--text2)", marginTop: 2 }}>{label}</div>
          </div>
        ))}
      </div>

      {/* Filter tabs */}
      <div className="filter-tabs">
        {["PENDING", "APPROVED", "REJECTED", "ALL"].map((s) => (
          <button key={s}
            className={`filter-tab ${filter === s ? "active" : ""}`}
            onClick={() => setFilter(s)}>
            {s}
            {s !== "ALL" && (
              <span className="tab-count">
                {s === "PENDING" ? pendingCount : s === "APPROVED" ? approvedCount : rejectedCount}
              </span>
            )}
          </button>
        ))}
      </div>

      <DataTable
        columns={columns}
        data={filtered}
        actions={actions}
        loading={loading}
        selectable={true}
        selected={selected}
        onSelectChange={setSelected}
      />

      {/* Reject with notes modal */}
      <Modal
        open={!!rejectModal}
        title={`Reject Request #${rejectModal?.id}`}
        onClose={() => setRejectModal(null)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setRejectModal(null)}>Cancel</button>
            <button className="btn-danger" onClick={() => handleReject(rejectModal.id, rejectNotes)}>
              Reject
            </button>
          </>
        }
      >
        <div className="form-field">
          <label>Rejection Reason (optional)</label>
          <textarea
            rows={3}
            value={rejectNotes}
            onChange={e => setRejectNotes(e.target.value)}
            placeholder="Explain why this request is being rejected…"
            style={{
              width: "100%", background: "var(--bg3)", border: "1px solid var(--border)",
              borderRadius: 8, padding: 10, color: "var(--text)", fontFamily: "inherit",
              fontSize: 13, resize: "vertical",
            }}
          />
        </div>
      </Modal>
    </div>
  );
}
