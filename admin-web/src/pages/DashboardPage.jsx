import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useToast } from "../components/Toast";

const STATS = [
  { key: "users",        label: "Total Users",       icon: "👤", color: "#4f8ef7", path: "/users" },
  { key: "approvals",    label: "Pending Approvals",  icon: "⏳", color: "#f5a623", path: "/approvals" },
  { key: "donors",       label: "Donors",             icon: "🩸", color: "#f05252", path: "/donors" },
  { key: "bloodRequests",label: "Blood Requests",     icon: "💉", color: "#f97316", path: "/blood-requests" },
  { key: "hospitals",    label: "Hospitals",          icon: "🏥", color: "#4f8ef7", path: "/hospitals" },
  { key: "doctors",      label: "Doctors",            icon: "👨‍⚕️", color: "#a78bfa", path: "/doctors" },
  { key: "ambulances",   label: "Ambulances",         icon: "🚑", color: "#f05252", path: "/ambulances" },
  { key: "pharmacies",   label: "Pharmacies",         icon: "💊", color: "#10d98a", path: "/pharmacies" },
  { key: "diagnostics",  label: "Diagnostics",        icon: "🔬", color: "#22d3ee", path: "/diagnostics" },
  { key: "bloodBanks",   label: "Blood Banks",        icon: "🏦", color: "#f05252", path: "/blood-banks" },
  { key: "emergency",    label: "Open Emergencies",   icon: "🆘", color: "#f05252", path: "/emergency" },
  { key: "appointments", label: "Appointments",       icon: "📅", color: "#4f8ef7", path: "/appointments" },
];

export default function DashboardPage() {
  const toast    = useToast();
  const navigate = useNavigate();
  const [counts,          setCounts]          = useState({});
  const [loading,         setLoading]         = useState(true);
  const [recentApprovals, setRecentApprovals] = useState([]);
  const [recentUsers,     setRecentUsers]     = useState([]);
  const [recentEmergency, setRecentEmergency] = useState([]);
  const [recentBloodReqs, setRecentBloodReqs] = useState([]);
  const [recentDonors,    setRecentDonors]    = useState([]);
  const [lastRefresh,     setLastRefresh]     = useState(null);

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const sources = [
        { key: "users", url: "/users" },
        { key: "donors", url: "/donors" },
        { key: "bloodRequests", url: "/blood-requests" },
        { key: "hospitals", url: "/hospitals" },
        { key: "doctors", url: "/doctors" },
        { key: "ambulances", url: "/ambulances" },
        { key: "pharmacies", url: "/pharmacies" },
        { key: "diagnostics", url: "/diagnostics" },
        { key: "bloodBanks", url: "/blood-banks" },
        { key: "approvals", url: "/approvals" },
        { key: "emergency", url: "/emergency-requests" },
        { key: "appointments", url: "/appointments" },
      ];

      const results = await Promise.allSettled(
        sources.map((s) => api.get(s.url))
      );

      const dataMap = {};
      const failed = [];

      sources.forEach((s, idx) => {
        const result = results[idx];
        if (result.status === "fulfilled") {
          dataMap[s.key] = Array.isArray(result.value?.data) ? result.value.data : [];
        } else {
          dataMap[s.key] = [];
          failed.push(s.url);
        }
      });

      const pendingApprovals = dataMap.approvals.filter(a => a.status === "PENDING");
      const openEmergencies  = dataMap.emergency.filter(e => e.status === "OPEN" || e.status === "PENDING");
      const openBloodReqs    = dataMap.bloodRequests.filter(r => r.status === "OPEN" || r.status === "PENDING");

      setCounts({
        users:         dataMap.users.length,
        donors:        dataMap.donors.length,
        bloodRequests: dataMap.bloodRequests.length,
        hospitals:     dataMap.hospitals.length,
        doctors:       dataMap.doctors.length,
        ambulances:    dataMap.ambulances.length,
        pharmacies:    dataMap.pharmacies.length,
        diagnostics:   dataMap.diagnostics.length,
        bloodBanks:    dataMap.bloodBanks.length,
        approvals:     pendingApprovals.length,
        emergency:     openEmergencies.length,
        appointments:  dataMap.appointments.length,
      });

      setRecentApprovals(pendingApprovals.slice(0, 5));
      setRecentUsers([...dataMap.users].reverse().slice(0, 5));
      setRecentEmergency(openEmergencies.slice(0, 4));
      setRecentBloodReqs(openBloodReqs.slice(0, 5));
      setRecentDonors(dataMap.donors.filter(d => d.availableNow).slice(0, 5));
      setLastRefresh(new Date().toLocaleTimeString());

      if (failed.length > 0) {
        toast.error(`Some dashboard sources failed: ${failed.slice(0, 3).join(", ")}${failed.length > 3 ? "..." : ""}`);
      }
    } catch {
      toast.error("Failed to load dashboard data.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadAll(); }, [loadAll]);

  // Auto-refresh every 60 seconds
  useEffect(() => {
    const interval = setInterval(loadAll, 60_000);
    return () => clearInterval(interval);
  }, [loadAll]);

  async function handleApproval(id, action) {
    try {
      await api.put(`/approvals/${id}/${action}`);
      toast.success(`Request #${id} ${action}d.`);
      loadAll();
    } catch (err) {
      toast.error(err.response?.data?.message || "Action failed.");
    }
  }

  return (
    <div className="page-content">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          {lastRefresh && (
            <p className="page-subtitle">
              <span className="status-dot online" />
              Last updated: {lastRefresh} · Auto-refreshes every 60s
            </p>
          )}
        </div>
        <button className="btn-refresh" onClick={loadAll} disabled={loading}>
          {loading ? "Loading…" : "↻ Refresh"}
        </button>
      </div>

      {/* Stat Cards */}
      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : (
        <div className="stats-grid">
          {STATS.map(({ key, label, icon, color, path }) => (
            <div
              key={key}
              className="stat-card"
              style={{
                borderTop: `3px solid ${color}`,
                cursor: "pointer",
                boxShadow: counts[key] > 0 && (key === "approvals" || key === "emergency")
                  ? `0 0 16px ${color}22` : undefined,
              }}
              onClick={() => navigate(path)}
              title={`Go to ${label}`}
            >
              <div className="stat-icon" style={{ color }}>{icon}</div>
              <div className="stat-body">
                <div className="stat-value" style={{ color }}>{counts[key] ?? "—"}</div>
                <div className="stat-title">{label}</div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Alert banner for open emergencies */}
      {!loading && counts.emergency > 0 && (
        <div style={{
          background: "rgba(240,82,82,0.08)",
          border: "1px solid rgba(240,82,82,0.3)",
          borderRadius: 12,
          padding: "14px 20px",
          marginBottom: 24,
          display: "flex",
          alignItems: "center",
          gap: 12,
          cursor: "pointer",
        }} onClick={() => navigate("/emergency")}>
          <span style={{ fontSize: 20 }}>🆘</span>
          <div style={{ flex: 1 }}>
            <strong style={{ color: "var(--danger)" }}>{counts.emergency} open emergency request{counts.emergency > 1 ? "s" : ""}</strong>
            <span style={{ color: "var(--text2)", fontSize: 13, marginLeft: 8 }}>require immediate attention</span>
          </div>
          <span style={{ color: "var(--danger)", fontSize: 13, fontWeight: 600 }}>View all →</span>
        </div>
      )}

      {/* Two-column widgets */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20, marginBottom: 20 }}>

        {/* Pending Approvals */}
        <div className="section-card">
          <div className="section-header">
            <h2>
              ⏳ Pending Approvals
              {counts.approvals > 0 && (
                <span className="badge badge-amber" style={{ marginLeft: 8 }}>{counts.approvals}</span>
              )}
            </h2>
            <button className="btn-refresh" style={{ fontSize: 12 }} onClick={() => navigate("/approvals")}>
              View all →
            </button>
          </div>
          {recentApprovals.length === 0 ? (
            <p className="empty-msg">✓ No pending approvals</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr><th>#</th><th>Entity</th><th>Role</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {recentApprovals.map((a) => (
                  <tr key={a.id}>
                    <td style={{ color: "var(--text3)", fontSize: 12 }}>{a.id}</td>
                    <td><span className="badge badge-blue">{a.entityType}</span></td>
                    <td><span className="badge badge-purple">{a.requestedRole}</span></td>
                    <td className="action-cell">
                      <button className="btn-action btn-success" onClick={() => handleApproval(a.id, "approve")}>✓</button>
                      <button className="btn-action btn-danger"  onClick={() => handleApproval(a.id, "reject")}>✕</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Recent Users */}
        <div className="section-card">
          <div className="section-header">
            <h2>👤 Recent Users</h2>
            <button className="btn-refresh" style={{ fontSize: 12 }} onClick={() => navigate("/users")}>
              Manage →
            </button>
          </div>
          {recentUsers.length === 0 ? (
            <p className="empty-msg">No users yet</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr><th>Name</th><th>Phone</th><th>Status</th><th>Joined</th></tr>
              </thead>
              <tbody>
                {recentUsers.map((u) => (
                  <tr key={u.id}>
                    <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                    <td style={{ color: "var(--text2)", fontSize: 12 }}>{u.phone}</td>
                    <td>
                      <span className={`badge ${u.active ? "badge-green" : "badge-red"}`}>
                        {u.active ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td style={{ color: "var(--text3)", fontSize: 12 }}>
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "—"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Open Emergencies widget */}
      {recentEmergency.length > 0 && (
        <div className="section-card" style={{ borderTop: "3px solid var(--danger)", marginBottom: 20 }}>
          <div className="section-header">
            <h2 style={{ color: "var(--danger)" }}>
              🆘 Open Emergencies
              <span className="badge badge-red" style={{ marginLeft: 8 }}>{counts.emergency}</span>
            </h2>
            <button className="btn-refresh" style={{ fontSize: 12 }} onClick={() => navigate("/emergency")}>
              View all →
            </button>
          </div>
          <table className="data-table">
            <thead>
              <tr><th>#</th><th>Caller</th><th>Type</th><th>District</th><th>Phone</th></tr>
            </thead>
            <tbody>
              {recentEmergency.map((e) => (
                <tr key={e.id}>
                  <td style={{ color: "var(--text3)", fontSize: 12 }}>{e.id}</td>
                  <td style={{ fontWeight: 600 }}>{e.callerName}</td>
                  <td><span className="badge badge-red">{e.emergencyType}</span></td>
                  <td style={{ color: "var(--text2)" }}>{e.district}</td>
                  <td style={{ color: "var(--text2)", fontSize: 12 }}>{e.phone}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Blood Donation Section */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20, marginBottom: 20 }}>
        
        {/* Recent Blood Requests */}
        <div className="section-card" style={{ borderTop: "3px solid #f97316" }}>
          <div className="section-header">
            <h2 style={{ color: "#f97316" }}>💉 Recent Blood Requests</h2>
            <button className="btn-refresh" style={{ fontSize: 12 }} onClick={() => navigate("/blood-requests")}>
              View all →
            </button>
          </div>
          {recentBloodReqs.length === 0 ? (
            <p className="empty-msg">No active requests</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr><th>BG</th><th>District</th><th>Status</th><th>Date</th></tr>
              </thead>
              <tbody>
                {recentBloodReqs.map((r) => (
                  <tr key={r.id}>
                    <td><span className="badge badge-red">{r.bloodGroup?.replace("_POS","+").replace("_NEG","-")}</span></td>
                    <td>{r.district}</td>
                    <td><span className={`badge ${r.status === "OPEN" ? "badge-amber" : "badge-blue"}`}>{r.status}</span></td>
                    <td style={{ color: "var(--text3)", fontSize: 12 }}>{r.createdAt ? new Date(r.createdAt).toLocaleDateString() : "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Active Donors */}
        <div className="section-card" style={{ borderTop: "3px solid #f05252" }}>
          <div className="section-header">
            <h2 style={{ color: "#f05252" }}>🩸 Active Donors</h2>
            <button className="btn-refresh" style={{ fontSize: 12 }} onClick={() => navigate("/donors")}>
              View all →
            </button>
          </div>
          {recentDonors.length === 0 ? (
            <p className="empty-msg">No available donors</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr><th>BG</th><th>District</th><th>Rewards</th><th>Phone</th></tr>
              </thead>
              <tbody>
                {recentDonors.map((d) => (
                  <tr key={d.id}>
                    <td><span className="badge badge-red">{d.bloodGroup?.replace("_POS","+").replace("_NEG","-")}</span></td>
                    <td>{d.district}</td>
                    <td><span className="badge badge-blue">{d.rewardPoints || 0} pts</span></td>
                    <td style={{ color: "var(--text2)", fontSize: 12 }}>{d.contactPhone}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      </div>

      <style>{`
        @media (max-width: 900px) {
          div[style*="grid-template-columns: 1fr 1fr"] {
            grid-template-columns: 1fr !important;
          }
        }
      `}</style>
    </div>
  );
}
