import React from "react";

export default function StatCard({ title, value, icon, color = "#3b82f6", sub }) {
  return (
    <div className="stat-card" style={{ borderTop: `3px solid ${color}` }}>
      <div className="stat-icon" style={{ color }}>{icon}</div>
      <div className="stat-body">
        <div className="stat-value">{value ?? "—"}</div>
        <div className="stat-title">{title}</div>
        {sub && <div className="stat-sub">{sub}</div>}
      </div>
    </div>
  );
}
