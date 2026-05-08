import React, { useEffect, useState } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";
import { useToast } from "../components/Toast";
import PageHeader from "../components/PageHeader";

export default function SlidesPage() {
  const toast = useToast();
  const [data, setData]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal]   = useState(null);
  const [form, setForm]     = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const res = await api.get("/slides/all");
      setData(res.data);
    } catch { toast.error("Failed to load slides."); }
    finally { setLoading(false); }
  }

  function openCreate() {
    setForm({ title: "", subtitle: "", imageUrl: "", actionUrl: "", displayOrder: data.length, active: true });
    setModal({ mode: "create" });
  }

  function openEdit(row) {
    setForm({ ...row });
    setModal({ mode: "edit", row });
  }

  async function handleSave() {
    if (!form.title?.trim() || !form.imageUrl?.trim()) {
      toast.error("Title and Image URL are required."); return;
    }
    setSaving(true);
    try {
      if (modal.mode === "create") await api.post("/slides", form);
      else await api.put(`/slides/${modal.row.id}`, form);
      toast.success(modal.mode === "create" ? "Slide created." : "Slide updated.");
      setModal(null); load();
    } catch (err) { toast.error(err.response?.data?.message || "Save failed."); }
    finally { setSaving(false); }
  }

  async function handleDelete(row) {
    if (!window.confirm(`Delete slide "${row.title}"?`)) return;
    try { await api.delete(`/slides/${row.id}`); toast.success("Deleted."); load(); }
    catch (err) { toast.error(err.response?.data?.message || "Delete failed."); }
  }

  const columns = [
    { key: "id", label: "ID" },
    { key: "displayOrder", label: "Order" },
    { key: "title", label: "Title" },
    { key: "subtitle", label: "Subtitle", render: (v) => v ? v.substring(0, 50) + "…" : "—" },
    { key: "imageUrl", label: "Image URL", render: (v) => (
      <a href={v} target="_blank" rel="noreferrer" className="link">Preview ↗</a>
    )},
    { key: "active", label: "Active", render: (v) => (
      <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Active" : "Hidden"}</span>
    )},
  ];

  return (
    <div className="page-content">
      <PageHeader title="Dashboard Slides" subtitle="Image slideshow shown on Android home screen" />
      <div className="page-header">
        <div />
        <div className="header-actions">
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
          <button className="btn-primary" onClick={openCreate}>+ Add Slide</button>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={data}
        loading={loading}
        actions={[
          { label: "Edit",   className: "btn-primary", onClick: openEdit },
          { label: "Delete", className: "btn-danger",  onClick: handleDelete },
        ]}
      />

      <Modal
        open={!!modal}
        title={modal?.mode === "create" ? "Add Slide" : `Edit Slide #${modal?.row?.id}`}
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
          {[
            { key: "title",        label: "Title *",        required: true },
            { key: "subtitle",     label: "Subtitle" },
            { key: "imageUrl",     label: "Image URL *",    required: true, placeholder: "https://..." },
            { key: "actionUrl",    label: "Action URL",     placeholder: "Optional deep link" },
            { key: "displayOrder", label: "Display Order",  type: "number" },
          ].map(f => (
            <div key={f.key} className="form-field">
              <label>{f.label}</label>
              <input
                type={f.type || "text"}
                value={form[f.key] ?? ""}
                onChange={e => setForm({ ...form, [f.key]: e.target.value })}
                placeholder={f.placeholder}
              />
            </div>
          ))}
          <div className="form-field">
            <label>Active</label>
            <input type="checkbox" checked={!!form.active}
              onChange={e => setForm({ ...form, active: e.target.checked })} />
          </div>
        </div>
        {form.imageUrl && (
          <div style={{ marginTop: 12 }}>
            <img src={form.imageUrl} alt="preview"
              style={{ width: "100%", maxHeight: 160, objectFit: "cover", borderRadius: 8 }}
              onError={e => e.target.style.display = "none"} />
          </div>
        )}
      </Modal>
    </div>
  );
}
