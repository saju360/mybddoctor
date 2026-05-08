import React, { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const NAV_GROUPS = [
  {
    label: "Overview",
    items: [
      { to: "/", icon: "D", label: "Dashboard" },
      { to: "/approvals", icon: "A", label: "Approvals" },
      { to: "/users", icon: "U", label: "Users" },
    ],
  },
  {
    label: "Blood Services",
    items: [
      { to: "/donors", icon: "DN", label: "Donors" },
      { to: "/blood-requests", icon: "BR", label: "Blood Requests" },
      { to: "/blood-banks", icon: "BB", label: "Blood Banks" },
      { to: "/blood-orgs", icon: "BO", label: "Blood Orgs" },
    ],
  },
  {
    label: "Healthcare",
    items: [
      { to: "/hospitals", icon: "H", label: "Hospitals" },
      { to: "/doctors", icon: "DR", label: "Doctors" },
      { to: "/clinics", icon: "CL", label: "Clinics" },
      { to: "/ambulances", icon: "AM", label: "Ambulances" },
      { to: "/pharmacies", icon: "PH", label: "Pharmacies" },
      { to: "/diagnostics", icon: "DG", label: "Diagnostics" },
    ],
  },
  {
    label: "Patient Services",
    items: [
      { to: "/emergency", icon: "E", label: "Emergency" },
      { to: "/appointments", icon: "AP", label: "Appointments" },
      { to: "/telemedicine", icon: "TM", label: "Telemedicine" },
      { to: "/reminders", icon: "RM", label: "Reminders" },
      { to: "/health-records", icon: "HR", label: "Health Records" },
    ],
  },
  {
    label: "App Management",
    items: [
      { to: "/slides", icon: "SL", label: "Dashboard Slides" },
      { to: "/walkthrough", icon: "WT", label: "Walkthrough" },
      { to: "/notifications", icon: "NT", label: "Notifications" },
      { to: "/reviews", icon: "RV", label: "Reviews" },
      { to: "/chat-monitor", icon: "CH", label: "Chat Monitor" },
      { to: "/ads-settings", icon: "AD", label: "Ads Settings" },
      { to: "/settings", icon: "ST", label: "Settings" },
    ],
  },
];

export default function Sidebar() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const initials = user?.fullName
    ? user.fullName.split(" ").map((w) => w[0]).join("").slice(0, 2).toUpperCase()
    : user?.userId
      ? `A${user.userId}`.slice(0, 2).toUpperCase()
      : "AD";

  return (
    <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
      <div className="sidebar-header">
        <span className="sidebar-logo">LP</span>
        {!collapsed && <span className="sidebar-title">LifePlus Admin</span>}
        <button className="sidebar-toggle" onClick={() => setCollapsed(!collapsed)} title={collapsed ? "Expand" : "Collapse"}>
          {collapsed ? ">" : "<"}
        </button>
      </div>

      <nav className="sidebar-nav">
        {NAV_GROUPS.map((group) => (
          <div key={group.label} className="nav-group">
            {!collapsed && <div className="nav-group-label">{group.label}</div>}
            {group.items.map(({ to, icon, label }) => (
              <NavLink
                key={to}
                to={to}
                end={to === "/"}
                className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
                title={collapsed ? label : undefined}
              >
                <span className="nav-icon">{icon}</span>
                {!collapsed && <span className="nav-label">{label}</span>}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        {!collapsed && (
          <div className="sidebar-user">
            <div className="user-avatar">{initials}</div>
            <div className="user-info">
              <div className="user-name">{user?.fullName || "Administrator"}</div>
              <div className="user-id" style={{ fontSize: 11, color: "var(--text3)" }}>ID: {user?.userId}</div>
            </div>
            <span className="user-badge">ADMIN</span>
          </div>
        )}
        <button className="logout-btn" onClick={handleLogout} title="Logout">
          <span>OUT</span>
          {!collapsed && <span>Logout</span>}
        </button>
      </div>
    </aside>
  );
}
