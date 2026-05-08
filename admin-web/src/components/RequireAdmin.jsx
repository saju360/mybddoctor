import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Route guard — redirects to /login if not authenticated as ADMIN */
export default function RequireAdmin({ children }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="splash-loading">
        <div className="spinner" />
        <p>Checking session…</p>
      </div>
    );
  }

  if (!user || user.role !== "ADMIN") {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}
