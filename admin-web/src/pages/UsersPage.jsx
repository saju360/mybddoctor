import React, { useEffect, useState, useCallback } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";
import { useToast } from "../components/Toast";

export default function UsersPage() {
  const toast = useToast();
  const [data, setData]       = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(new Set());
  const [createModal, setCreateModal] = useState(false);
  const [pwdModal, setPwdModal]       = useState(null); // { id, name }
  const [roleModal, setRoleModal]     = useState(null); // { id, name, currentRole }
  const [form, setForm]   = useState({ fullName: "", phone: "", email: "", password: "", preferredLanguage: "en", role: "USER", bloodGroup: "", district: "" });
  const [pwdForm, setPwdForm] = useState({ password: "" });
  const [roleForm, setRoleForm] = useState({ role: "USER" });
  const [saving, setSaving]   = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get("/users");
      setData(Array.isArray(res.data) ? res.data : []);
    } catch { toast.error("Failed to load users."); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  async function handleCreate() {
    if (!form.fullName.trim() || !form.phone.trim() || !form.email.trim() || form.password.length < 8) {
      toast.error("All fields required. Password min 8 chars."); return;
    }
    setSaving(true);
    try {
      const res = await api.post("/admin/users", form);
      // Assign role if not USER
      if (form.role && form.role !== "USER" && res.data?.id) {
        await api.post("/admin/roles/assign", { userId: String(res.data.id), role: form.role });
      }
      toast.success("User created."); setCreateModal(false);
      setForm({ fullName: "", phone: "", email: "", password: "", preferredLanguage: "en", role: "USER", bloodGroup: "", district: "" });
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Create failed."); }
    finally { setSaving(false); }
  }

  async function handleAssignRole() {
    setSaving(true);
    try {
      await api.post("/admin/roles/assign", { userId: String(roleModal.id), role: roleForm.role });
      toast.success(`Role updated to ${roleForm.role}.`);
      setRoleModal(null);
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Role update failed."); }
    finally { setSaving(false); }
  }

  async function handleToggleActive(row) {
    try {
      await api.put(`/admin/users/${row.id}/toggle-active`);
      toast.success(`User ${row.active ? "deactivated" : "activated"}.`); load();
    } catch (err) { toast.error(err.response?.data?.message || "Failed."); }
  }

  async function handleResetPassword() {
    if (pwdForm.password.length < 8) { toast.error("Password min 8 chars."); return; }
    setSaving(true);
    try {
      await api.put(`/admin/users/${pwdModal.id}/reset-password`, pwdForm);
      toast.success("Password reset."); setPwdModal(null); setPwdForm({ password: "" });
    } catch (err) { toast.error(err.response?.data?.message || "Failed."); }
    finally { setSaving(false); }
  }

  async function handleBulkDelete() {
    if (selected.size === 0) { toast.error("No users selected."); return; }
    if (!window.confirm(`Delete ${selected.size} users?`)) return;
    try {
      await api.delete("/admin/users/bulk", { data: { ids: [...selected] } });
      toast.success(`${selected.size} users deleted.`); load();
    } catch (err) { toast.error(err.response?.data?.message || "Bulk delete failed."); }
  }

  function exportCSV() {
    const header = "ID,Full Name,Phone,Email,Blood Group,District,Language,Active,Joined";
    const rows = data.map(u =>
      `${u.id},"${u.fullName}",${u.phone},${u.email},${u.bloodGroup},${u.district},${u.preferredLanguage},${u.active},${u.createdAt ? new Date(u.createdAt).toLocaleDateString() : ""}`
    );
    const csv = [header, ...rows].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a"); a.href = url; a.download = "users.csv"; a.click();
    URL.revokeObjectURL(url); toast.success("CSV exported.");
  }

  const columns = [
    { key: "id",                label: "ID" },
    { key: "fullName",          label: "Full Name" },
    { key: "phone",             label: "Phone" },
    { key: "email",             label: "Email" },
    { key: "bloodGroup",        label: "Blood" },
    { key: "district",          label: "District" },
    { key: "preferredLanguage", label: "Lang" },
    { key: "active",            label: "Active",
      render: v => <span className={`badge ${v ? "badge-green" : "badge-red"}`}>{v ? "Active" : "Inactive"}</span> },
    { key: "createdAt",         label: "Joined",
      render: v => v ? new Date(v).toLocaleDateString() : "—" },
  ];

  const actions = [
    { label: "Toggle Active", className: "btn-warning", onClick: handleToggleActive },
    { label: "Assign Role",   className: "btn-primary",
      onClick: row => { setRoleModal({ id: row.id, name: row.fullName }); setRoleForm({ role: "USER" }); } },
    { label: "Reset Pwd",     className: "btn-secondary",
      onClick: row => { setPwdModal({ id: row.id, name: row.fullName }); setPwdForm({ password: "" }); } },
  ];

  return (
    <div className="page-content">
      <div className="page-header">
        <h1>Users {selected.size > 0 && <span className="badge badge-amber" style={{ marginLeft: 10 }}>{selected.size} selected</span>}</h1>
        <div className="header-actions">
          {selected.size > 0 && <button className="btn-action btn-danger" onClick={handleBulkDelete}>🗑 Delete {selected.size}</button>}
          <button className="btn-refresh" onClick={exportCSV}>⬇ CSV</button>
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
          <button className="btn-primary" onClick={() => setCreateModal(true)}>+ Create User</button>
        </div>
      </div>

      <DataTable columns={columns} data={data} actions={actions} loading={loading}
        selectable selected={selected} onSelectChange={setSelected} />

      {/* Create User Modal */}
      <Modal open={createModal} title="Create New User" onClose={() => setCreateModal(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setCreateModal(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleCreate} disabled={saving}>
              {saving ? "Creating…" : "Create"}
            </button>
          </>
        }
      >
        <div className="form-grid">
          {[
            { key: "fullName", label: "Full Name *" },
            { key: "phone",    label: "Phone *",    type: "tel" },
            { key: "email",    label: "Email *",    type: "email" },
            { key: "password", label: "Password *", type: "password" },
            { key: "district", label: "District" },
          ].map(f => (
            <div key={f.key} className="form-field">
              <label>{f.label}</label>
              <input type={f.type || "text"} value={form[f.key]}
                onChange={e => setForm({ ...form, [f.key]: e.target.value })} />
            </div>
          ))}
          <div className="form-field">
            <label>Blood Group</label>
            <select value={form.bloodGroup} onChange={e => setForm({ ...form, bloodGroup: e.target.value })}>
              <option value="">Unknown</option>
              <option value="A_POS">A+</option>
              <option value="A_NEG">A-</option>
              <option value="B_POS">B+</option>
              <option value="B_NEG">B-</option>
              <option value="O_POS">O+</option>
              <option value="O_NEG">O-</option>
              <option value="AB_POS">AB+</option>
              <option value="AB_NEG">AB-</option>
            </select>
          </div>
          <div className="form-field">
            <label>Language</label>
            <select value={form.preferredLanguage}
              onChange={e => setForm({ ...form, preferredLanguage: e.target.value })}>
              <option value="en">English</option>
              <option value="bn">বাংলা</option>
            </select>
          </div>
          <div className="form-field">
            <label>Role</label>
            <select value={form.role}
              onChange={e => setForm({ ...form, role: e.target.value })}>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
              <option value="HOSPITAL_ADMIN">HOSPITAL_ADMIN</option>
              <option value="BLOOD_ORG_ADMIN">BLOOD_ORG_ADMIN</option>
              <option value="AMBULANCE_PROVIDER">AMBULANCE_PROVIDER</option>
            </select>
          </div>
        </div>
      </Modal>

      {/* Reset Password Modal */}
      <Modal open={!!pwdModal} title={`Reset Password — ${pwdModal?.name}`}
        onClose={() => setPwdModal(null)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setPwdModal(null)}>Cancel</button>
            <button className="btn-primary" onClick={handleResetPassword} disabled={saving}>
              {saving ? "Resetting…" : "Reset"}
            </button>
          </>
        }
      >
        <div className="form-field">
          <label>New Password (min 8 chars)</label>
          <input type="password" value={pwdForm.password}
            onChange={e => setPwdForm({ password: e.target.value })} />
        </div>
      </Modal>

      {/* Assign Role Modal */}
      <Modal open={!!roleModal} title={`Assign Role — ${roleModal?.name}`}
        onClose={() => setRoleModal(null)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setRoleModal(null)}>Cancel</button>
            <button className="btn-primary" onClick={handleAssignRole} disabled={saving}>
              {saving ? "Saving…" : "Assign Role"}
            </button>
          </>
        }
      >
        <div className="form-field">
          <label>Role</label>
          <select value={roleForm.role} onChange={e => setRoleForm({ role: e.target.value })}>
            <option value="USER">USER — Regular user</option>
            <option value="ADMIN">ADMIN — Full admin access</option>
            <option value="HOSPITAL_ADMIN">HOSPITAL_ADMIN — Manage hospitals</option>
            <option value="BLOOD_ORG_ADMIN">BLOOD_ORG_ADMIN — Manage blood orgs</option>
            <option value="AMBULANCE_PROVIDER">AMBULANCE_PROVIDER — Manage ambulances</option>
          </select>
          <p style={{ color: "var(--text3)", fontSize: 12, marginTop: 6 }}>
            ⚠ ADMIN role gives full access to the admin panel. Assign carefully.
          </p>
        </div>
      </Modal>
    </div>
  );
}
