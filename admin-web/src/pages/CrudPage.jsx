import React, { useEffect, useState, useCallback } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import { useToast } from "../components/Toast";

/**
 * Generic CRUD page with bulk operations, CSV export, search.
 */
export default function CrudPage({
  title, endpoint, columns, formFields = [],
  canCreate = true, canEdit = true, canDelete = true,
  extraActions = [], transformCreate, transformEdit,
  bulkEndpoint,   // optional: e.g. "/bulk/hospitals"
}) {
  const toast = useToast();
  const [data,     setData]     = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [modal,    setModal]    = useState(null);
  const [form,     setForm]     = useState({});
  const [saving,   setSaving]   = useState(false);
  const [selected, setSelected] = useState(new Set());
  const [bulkModal, setBulkModal] = useState(false);
  const [bulkJson,  setBulkJson]  = useState("");
  const [bulkSaving, setBulkSaving] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(null); // { row } or { bulk: true }

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(endpoint);
      setData(Array.isArray(res.data) ? res.data : []);
      setSelected(new Set());
    } catch { toast.error(`Failed to load ${title}.`); }
    finally { setLoading(false); }
  }, [endpoint, title]);

  useEffect(() => { load(); }, [load]);

  function openCreate() {
    const blank = {};
    formFields.forEach(f => { blank[f.key] = f.default ?? ""; });
    setForm(blank);
    setModal({ mode: "create" });
  }

  function openEdit(row) {
    const pre = {};
    formFields.forEach(f => { pre[f.key] = row[f.key] ?? ""; });
    setForm(pre);
    setModal({ mode: "edit", row });
  }

  async function handleSave() {
    const errors = [];
    for (const f of formFields) {
      if (f.required && !form[f.key]?.toString().trim()) {
        errors.push(`${f.label} is required.`);
      }
      if (f.type === "tel" && form[f.key] && form[f.key].length < 10) {
        errors.push(`${f.label} must be a valid phone number.`);
      }
      if (f.type === "email" && form[f.key] && !form[f.key].includes("@")) {
        errors.push(`${f.label} must be a valid email address.`);
      }
    }
    if (errors.length > 0) { toast.error(errors[0]); return; }
    setSaving(true);
    try {
      if (modal.mode === "create") {
        const body = transformCreate ? transformCreate(form) : form;
        await api.post(endpoint, body);
        toast.success(`${title} created successfully.`);
      } else {
        // Preserve untouched fields so PUT payload keeps backend-required values.
        const merged = { ...modal.row, ...form };
        const body = transformEdit ? transformEdit(merged, modal.row) : merged;
        await api.put(`${endpoint}/${modal.row.id}`, body);
        toast.success(`${title} updated successfully.`);
      }
      setModal(null); load();
    } catch (err) { toast.error(err.response?.data?.message || "Save failed. Please try again."); }
    finally { setSaving(false); }
  }

  async function handleDelete(row) {
    setConfirmDelete({ row });
  }

  async function doDelete(row) {
    setConfirmDelete(null);
    try {
      await api.delete(`${endpoint}/${row.id}`);
      toast.success("Deleted successfully."); load();
    } catch (err) { toast.error(err.response?.data?.message || "Delete failed."); }
  }

  async function handleBulkDelete() {
    if (selected.size === 0) { toast.error("No rows selected."); return; }
    setConfirmDelete({ bulk: true });
  }

  async function doBulkDelete() {
    setConfirmDelete(null);
    try {
      await Promise.all([...selected].map(id => api.delete(`${endpoint}/${id}`)));
      toast.success(`${selected.size} records deleted.`);
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Bulk delete failed."); }
  }

  async function handleBulkInsert() {
    let items;
    try { items = JSON.parse(bulkJson); }
    catch { toast.error("Invalid JSON."); return; }
    if (!Array.isArray(items)) { toast.error("JSON must be an array."); return; }
    setBulkSaving(true);
    try {
      const ep = bulkEndpoint || endpoint;
      if (bulkEndpoint) {
        await api.post(bulkEndpoint, items);
      } else {
        await Promise.all(items.map(item => api.post(endpoint, item)));
      }
      toast.success(`${items.length} records inserted.`);
      setBulkModal(false); setBulkJson(""); load();
    } catch (err) { toast.error(err.response?.data?.message || "Bulk insert failed."); }
    finally { setBulkSaving(false); }
  }

  function exportCSV() {
    if (data.length === 0) { toast.error("No data to export."); return; }
    const keys = columns.map(c => c.key);
    const header = columns.map(c => c.label).join(",");
    const rows = data.map(row =>
      keys.map(k => {
        const v = row[k] ?? "";
        return typeof v === "string" && v.includes(",") ? `"${v}"` : v;
      }).join(",")
    );
    const csv = [header, ...rows].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement("a");
    a.href = url; a.download = `${title.toLowerCase().replace(/\s+/g, "_")}.csv`;
    a.click(); URL.revokeObjectURL(url);
    toast.success("CSV exported.");
  }

  const tableActions = [
    ...(canEdit   ? [{ label: "Edit",   className: "btn-primary", onClick: openEdit }] : []),
    ...(canDelete ? [{ label: "Delete", className: "btn-danger",  onClick: handleDelete }] : []),
    ...extraActions.map(a => ({ ...a, onClick: (row) => a.onClick(row, load) })),
  ];

  return (
    <div className="page-content">
      <div className="page-header">
        <h1>{title}
          {selected.size > 0 && (
            <span className="badge badge-amber" style={{ marginLeft: 10, fontSize: 13 }}>
              {selected.size} selected
            </span>
          )}
        </h1>
        <div className="header-actions">
          {selected.size > 0 && canDelete && (
            <button className="btn-action btn-danger" onClick={handleBulkDelete}>
              🗑 Delete {selected.size}
            </button>
          )}
          <button className="btn-refresh" onClick={exportCSV}>⬇ CSV</button>
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
          {canCreate && (
            <>
              <button className="btn-secondary" onClick={() => setBulkModal(true)}>⬆ Bulk Insert</button>
              <button className="btn-primary" onClick={openCreate}>+ Add {title}</button>
            </>
          )}
        </div>
      </div>

      <DataTable
        columns={columns}
        data={data}
        actions={tableActions}
        loading={loading}
        selectable={canDelete}
        selected={selected}
        onSelectChange={setSelected}
      />

      {/* Create / Edit Modal */}
      <Modal
        open={!!modal}
        title={modal?.mode === "create" ? `Add ${title}` : `Edit ${title} #${modal?.row?.id}`}
        onClose={() => setModal(null)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button className="btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? "Saving…" : "Save"}
            </button>
          </>
        }
      >
        <div className="form-grid">
          {formFields.map(f => (
            <div key={f.key} className={`form-field ${f.fullWidth ? "full-width" : ""}`}>
              <label>{f.label}{f.required && <span className="required">*</span>}</label>
              {f.type === "select" ? (
                <select value={form[f.key] ?? ""}
                  onChange={e => setForm({ ...form, [f.key]: e.target.value })}>
                  <option value="">— Select —</option>
                  {f.options.map(o => (
                    <option key={o.value ?? o} value={o.value ?? o}>{o.label ?? o}</option>
                  ))}
                </select>
              ) : f.type === "textarea" ? (
                <textarea rows={3} value={form[f.key] ?? ""}
                  onChange={e => setForm({ ...form, [f.key]: e.target.value })}
                  placeholder={f.placeholder} />
              ) : f.type === "checkbox" ? (
                <input type="checkbox" checked={!!form[f.key]}
                  onChange={e => setForm({ ...form, [f.key]: e.target.checked })} />
              ) : (
                <input type={f.type || "text"} value={form[f.key] ?? ""}
                  onChange={e => setForm({ ...form, [f.key]: e.target.value })}
                  placeholder={f.placeholder} />
              )}
            </div>
          ))}
        </div>
      </Modal>

      {/* Bulk Insert Modal */}
      <Modal open={bulkModal} title={`Bulk Insert ${title}`} onClose={() => setBulkModal(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setBulkModal(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleBulkInsert} disabled={bulkSaving}>
              {bulkSaving ? "Inserting…" : "Insert All"}
            </button>
          </>
        }
      >
        <p style={{ color: "var(--text2)", fontSize: 13, marginBottom: 12 }}>
          Paste a JSON array of objects. Each object should match the {title} fields.
        </p>
        <textarea
          rows={12}
          value={bulkJson}
          onChange={e => setBulkJson(e.target.value)}
          placeholder={`[\n  { "name": "...", "district": "..." },\n  ...\n]`}
          style={{
            width: "100%", background: "var(--bg3)", border: "1px solid var(--border)",
            borderRadius: 8, padding: 12, color: "var(--text)", fontFamily: "monospace",
            fontSize: 12, resize: "vertical",
          }}
        />
      </Modal>

      <style>{`
        .full-width { grid-column: 1 / -1; }
      `}</style>

      {/* Delete confirmation */}
      <ConfirmDialog
        open={!!confirmDelete}
        title={confirmDelete?.bulk ? `Delete ${selected.size} Records` : `Delete Record #${confirmDelete?.row?.id}`}
        message={confirmDelete?.bulk
          ? `This will permanently delete ${selected.size} records. This action cannot be undone.`
          : `This will permanently delete record #${confirmDelete?.row?.id}. This action cannot be undone.`}
        danger
        confirmLabel="Delete"
        onConfirm={() => confirmDelete?.bulk ? doBulkDelete() : doDelete(confirmDelete.row)}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  );
}
