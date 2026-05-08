import React, { useEffect, useState } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";
import { useToast } from "../components/Toast";
import PageHeader from "../components/PageHeader";

const ICON_OPTIONS = [
  "ic_app_logo","ic_emergency","ic_blooddrop","ic_health_record",
  "ic_hospital","ic_doctor","ic_ambulance","ic_pharmacy","ic_reminder",
  "ic_vaccine","ic_bmi","ic_telemedicine","ic_appointment",
];

export default function WalkthroughPage() {
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
      const res = await api.get("/walkthrough/all");
      setData(res.data);
    } catch { toast.error("Failed to load walkthrough slides."); }
    finally { setLoading(false); }
  }

  function openCreate() {
    setForm({ title: "", subtitle: "", iconName: "ic_app_logo", accentColor: "#EF4444", displayOrder: data.length + 1, active: true });
    setModal({ mode: "create" });
  }

  function openEdit(row) { setForm({ ...row }); setModal({ mode: "edit", row }); }

  async function handleSave() {
    if (!form.title?.trim() || !form.subtitle?.trim()) {
      toast.error("Title and subtitle are required."); return;
    }
    setSaving(true);
    try {
      if (modal.mode === "create") await api.post("/walkthrough", form);
      else await api.put(`/walkthrough/${modal.row.id}`, form);
      toast.success("Saved."); setModal(null); load();
    } catch (err) { toast.error(err.response?.data?.message || "Save failed."); }
    finally { setSaving(false); }
  }

  async function handleDelete(row) {
    if (!window.confirm(`Delete slide "${row.title}"?`)) return;
    try { await api.delete(`/walkthrough/${row.id}`); toast.success("Deleted."); load(); }
    catch (err) { toast.error(err.response?.data?.message || "Delete failed."); }
  }

  const columns = [
    { key: "id", label: "ID" },
    { key: "displayOrder", label: "Order" },
    { key: "title", label: "Title" },
    { key: "subtitle", label: "Subtitle", render: v => v?.substring(0, 60) + "…" },
    { key: "iconName", label: "Icon" },
    { key: "accentColor", label: "Color", render: v => (
      <span style={{ display: "inline-flex", alignItems: "center", gap: 6 }}>
        <span style={{ width: 16, height: 16, borderRadius: "50%", background: v, display: "inline-block" }} />
        {v}
      </span>
    )},
    { key: "active", label: "Active", render: v => (
      <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Active" : "Hidden"}</span>
    )},
  ];

  return (
    <div className="page-content">
      <PageHeader title="Walkthrough Slides" subtitle="Onboarding slides shown on first app launch" />
      <div className="page-header">
        <div />
        <div className="header-actions">
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
          <button className="btn-primary" onClick={openCreate}>+ Add Slide</button>
        </div>
      </div>

      <DataTable columns={columns} data={data} loading={loading}
        actions={[
          { label: "Edit",   className: "btn-primary", onClick: openEdit },
          { label: "Delete", className: "btn-danger",  onClick: handleDelete },
        ]}
      />

      <Modal open={!!modal}
        title={modal?.mode === "create" ? "Add Walkthrough Slide" : `Edit Slide #${modal?.row?.id}`}
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
          <div className="form-field">
            <label>Title *</label>
            <input value={form.title ?? ""} onChange={e => setForm({ ...form, title: e.target.value })} />
          </div>
          <div className="form-field" style={{ gridColumn: "1 / -1" }}>
            <label>Subtitle *</label>
            <textarea rows={3} value={form.subtitle ?? ""}
              onChange={e => setForm({ ...form, subtitle: e.target.value })} />
          </div>
          <div className="form-field">
            <label>Icon Name</label>
            <select value={form.iconName ?? ""} onChange={e => setForm({ ...form, iconName: e.target.value })}>
              {ICON_OPTIONS.map(o => <option key={o} value={o}>{o}</option>)}
            </select>
          </div>
          <div className="form-field">
            <label>Accent Color</label>
            <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <input type="color" value={form.accentColor ?? "#EF4444"}
                onChange={e => setForm({ ...form, accentColor: e.target.value })}
                style={{ width: 48, height: 36, padding: 2, cursor: "pointer" }} />
              <input value={form.accentColor ?? ""} onChange={e => setForm({ ...form, accentColor: e.target.value })}
                placeholder="#EF4444" style={{ flex: 1 }} />
            </div>
          </div>
          <div className="form-field">
            <label>Display Order</label>
            <input type="number" value={form.displayOrder ?? 1}
              onChange={e => setForm({ ...form, displayOrder: parseInt(e.target.value) })} />
          </div>
          <div className="form-field">
            <label>Active</label>
            <input type="checkbox" checked={!!form.active}
              onChange={e => setForm({ ...form, active: e.target.checked })} />
          </div>
        </div>
      </Modal>
    </div>
  );
}
