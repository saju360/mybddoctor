import React, { useEffect, useState } from "react";
import api from "../api/axios";
import { useToast } from "../components/Toast";
import PageHeader from "../components/PageHeader";
import Modal from "../components/Modal";

const CATEGORY_COLORS = {
  SYSTEM: "#8b5cf6", EMERGENCY: "#ef4444", BLOOD: "#ef4444",
  REMINDERS: "#f59e0b", FEATURES: "#22c55e", UI: "#3b82f6", GENERAL: "#94a3b8",
};

export default function SettingsPage() {
  const toast = useToast();
  const [settings, setSettings] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [saving, setSaving]     = useState(null);
  const [addModal, setAddModal] = useState(false);
  const [newSetting, setNewSetting] = useState({ settingKey: "", settingValue: "", description: "", category: "GENERAL" });

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      const res = await api.get("/settings");
      setSettings(Array.isArray(res.data) ? res.data : []);
    } catch { toast.error("Failed to load settings."); }
    finally { setLoading(false); }
  }

  async function handleSave(key, value, description, category) {
    setSaving(key);
    try {
      await api.post("/settings", { settingKey: key, settingValue: value, description, category });
      toast.success(`Setting '${key}' updated.`);
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Failed to update."); }
    finally { setSaving(null); }
  }

  async function handleDelete(key) {
    if (!window.confirm(`Delete setting '${key}'?`)) return;
    try {
      await api.delete(`/settings/${key}`);
      toast.success("Setting deleted.");
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Delete failed."); }
  }

  async function handleAdd() {
    if (!newSetting.settingKey.trim()) { toast.error("Key is required."); return; }
    try {
      await api.post("/settings", newSetting);
      toast.success("Setting added.");
      setAddModal(false);
      setNewSetting({ settingKey: "", settingValue: "", description: "", category: "GENERAL" });
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Failed to add."); }
  }

  const handleChange = (key, value) =>
    setSettings(prev => prev.map(s => s.settingKey === key ? { ...s, settingValue: value } : s));

  // Group by category
  const grouped = settings.reduce((acc, s) => {
    const cat = s.category || "GENERAL";
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(s);
    return acc;
  }, {});

  return (
    <div className="page-content">
      <PageHeader title="Global Settings" subtitle="Control cross-platform features — changes apply to Android app in real-time" />

      <div className="page-header" style={{ marginBottom: 24 }}>
        <div />
        <div className="header-actions">
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
          <button className="btn-primary" onClick={() => setAddModal(true)}>+ Add Setting</button>
        </div>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : (
        Object.entries(grouped).map(([cat, items]) => (
          <div key={cat} className="section-card" style={{ marginBottom: 20 }}>
            <div className="section-header">
              <h2 style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ width: 10, height: 10, borderRadius: "50%", background: CATEGORY_COLORS[cat] || "#94a3b8", display: "inline-block" }} />
                {cat}
              </h2>
            </div>
            {items.map(s => (
              <div key={s.settingKey} className="settings-row">
                <div className="settings-info">
                  <code style={{ color: "var(--primary)", fontSize: 13 }}>{s.settingKey}</code>
                  {s.description && <p style={{ margin: "2px 0 0", fontSize: 12, color: "var(--text2)" }}>{s.description}</p>}
                </div>
                <div className="settings-control">
                  {s.settingValue === "true" || s.settingValue === "false" ? (
                    <label className="toggle-switch">
                      <input type="checkbox" checked={s.settingValue === "true"}
                        onChange={e => handleSave(s.settingKey, e.target.checked ? "true" : "false", s.description, s.category)}
                        disabled={saving === s.settingKey} />
                      <span className="toggle-slider" />
                    </label>
                  ) : (
                    <div style={{ display: "flex", gap: 8 }}>
                      <input className="settings-input" value={s.settingValue}
                        onChange={e => handleChange(s.settingKey, e.target.value)} />
                      <button className="btn-primary" style={{ padding: "6px 12px", fontSize: 12 }}
                        onClick={() => handleSave(s.settingKey, s.settingValue, s.description, s.category)}
                        disabled={saving === s.settingKey}>
                        {saving === s.settingKey ? "…" : "Save"}
                      </button>
                    </div>
                  )}
                  <button className="btn-action btn-danger" style={{ marginLeft: 8 }}
                    onClick={() => handleDelete(s.settingKey)}>✕</button>
                </div>
              </div>
            ))}
          </div>
        ))
      )}

      {/* Add new setting modal */}
      <Modal open={addModal} title="Add New Setting" onClose={() => setAddModal(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setAddModal(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleAdd}>Add</button>
          </>
        }
      >
        <div className="form-grid">
          {[
            { key: "settingKey",   label: "Key *",         placeholder: "e.g. feature_enabled" },
            { key: "settingValue", label: "Value *",        placeholder: "e.g. true" },
            { key: "description",  label: "Description",    placeholder: "What does this control?" },
          ].map(f => (
            <div key={f.key} className="form-field">
              <label>{f.label}</label>
              <input value={newSetting[f.key]} placeholder={f.placeholder}
                onChange={e => setNewSetting({ ...newSetting, [f.key]: e.target.value })} />
            </div>
          ))}
          <div className="form-field">
            <label>Category</label>
            <select value={newSetting.category}
              onChange={e => setNewSetting({ ...newSetting, category: e.target.value })}>
              {["GENERAL","SYSTEM","FEATURES","EMERGENCY","BLOOD","REMINDERS","UI"].map(c =>
                <option key={c} value={c}>{c}</option>
              )}
            </select>
          </div>
        </div>
      </Modal>

      <style>{`
        .settings-row {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 12px 0;
          border-bottom: 1px solid var(--border);
          gap: 16px;
          flex-wrap: wrap;
        }
        .settings-row:last-child { border-bottom: none; }
        .settings-info { flex: 1; min-width: 200px; }
        .settings-control { display: flex; align-items: center; gap: 8px; }
        .settings-input {
          background: var(--bg3);
          border: 1px solid var(--border);
          border-radius: 6px;
          padding: 6px 10px;
          color: var(--text);
          font-size: 13px;
          min-width: 180px;
        }
        .toggle-switch { position: relative; display: inline-block; width: 44px; height: 22px; }
        .toggle-switch input { opacity: 0; width: 0; height: 0; }
        .toggle-slider {
          position: absolute; cursor: pointer; inset: 0;
          background: #374151; border-radius: 22px; transition: .3s;
        }
        .toggle-slider:before {
          content: ""; position: absolute;
          height: 16px; width: 16px; left: 3px; bottom: 3px;
          background: white; border-radius: 50%; transition: .3s;
        }
        .toggle-switch input:checked + .toggle-slider { background: var(--primary); }
        .toggle-switch input:checked + .toggle-slider:before { transform: translateX(22px); }
      `}</style>
    </div>
  );
}
