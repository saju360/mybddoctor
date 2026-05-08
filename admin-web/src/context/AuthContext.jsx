import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import api from "../api/axios";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(null);   // { userId, role, token, fullName }
  const [loading, setLoading] = useState(true);   // checking stored session

  // Restore session from localStorage on mount
  useEffect(() => {
    const token  = localStorage.getItem("admin_token");
    const role   = localStorage.getItem("admin_role");
    const userId = localStorage.getItem("admin_userId");
    const name   = localStorage.getItem("admin_name");
    if (token && role === "ADMIN") {
      setUser({ token, role, userId, fullName: name });
    }
    setLoading(false);
  }, []);

  /** Login — calls /auth/login, enforces ADMIN role */
  const login = useCallback(async (phone, password) => {
    const res = await api.post("/auth/login", { phone, password });
    const { accessToken, role, userId, fullName } = res.data;

    if (role !== "ADMIN") {
      throw new Error("Access denied. Admin role required.");
    }

    localStorage.setItem("admin_token",  accessToken);
    localStorage.setItem("admin_role",   role);
    localStorage.setItem("admin_userId", userId);
    localStorage.setItem("admin_name",   fullName || "");
    setUser({ token: accessToken, role, userId, fullName });
    return res.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("admin_token");
    localStorage.removeItem("admin_role");
    localStorage.removeItem("admin_userId");
    localStorage.removeItem("admin_name");
    setUser(null);
  }, []);

  /** Re-fetch the current user profile (e.g. after profile update) */
  const refreshUser = useCallback(async () => {
    try {
      const res = await api.get("/users/me");
      const { fullName } = res.data;
      localStorage.setItem("admin_name", fullName || "");
      setUser(prev => prev ? { ...prev, fullName } : prev);
    } catch {
      // silently fail — not critical
    }
  }, []);

  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAdmin, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
