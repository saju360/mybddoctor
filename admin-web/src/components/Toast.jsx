import React, { createContext, useContext, useState, useCallback } from "react";

const ToastContext = createContext(null);

let _id = 0;

const ICONS = {
  success: "✓",
  error:   "✕",
  info:    "ℹ",
  warning: "⚠",
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const show = useCallback((message, type = "info") => {
    const id = ++_id;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  const success = useCallback((msg) => show(msg, "success"), [show]);
  const error   = useCallback((msg) => show(msg, "error"),   [show]);
  const info    = useCallback((msg) => show(msg, "info"),    [show]);
  const warning = useCallback((msg) => show(msg, "warning"), [show]);

  function dismiss(id) {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }

  return (
    <ToastContext.Provider value={{ success, error, info, warning }}>
      {children}
      <div className="toast-container">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type}`} onClick={() => dismiss(t.id)} style={{ cursor: "pointer" }}>
            <span className="toast-icon" style={{ fontSize: 15, flexShrink: 0 }}>{ICONS[t.type]}</span>
            <span style={{ flex: 1 }}>{t.message}</span>
            <span style={{ color: "var(--text3)", fontSize: 16, flexShrink: 0 }}>×</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
