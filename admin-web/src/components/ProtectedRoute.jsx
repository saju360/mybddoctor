import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Wraps a route — redirects to /login if not authenticated.
 * If adminOnly=true, redirects to / if not ADMIN role.
 */
export function ProtectedRoute({ children, adminOnly = false }) {
  const { loggedIn, isAdmin } = useAuth();
  const location = useLocation();

  if (!loggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && !isAdmin()) {
    return <Navigate to="/" replace />;
  }

  return children;
}
