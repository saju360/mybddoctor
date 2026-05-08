import React from "react";
import Modal from "./Modal";

/**
 * Reusable confirmation dialog.
 * Props: open, title, message, onConfirm, onCancel, danger, confirmLabel
 */
export default function ConfirmDialog({
  open,
  title       = "Confirm Action",
  message,
  onConfirm,
  onCancel,
  danger      = false,
  confirmLabel = "Confirm",
}) {
  return (
    <Modal open={open} title={title} onClose={onCancel} size="sm"
      footer={
        <>
          <button className="btn-secondary" onClick={onCancel}>Cancel</button>
          <button className={danger ? "btn-danger" : "btn-primary"} onClick={onConfirm}>
            {confirmLabel}
          </button>
        </>
      }
    >
      <p style={{ color: "var(--text2)", fontSize: 14, lineHeight: 1.6 }}>{message}</p>
    </Modal>
  );
}
