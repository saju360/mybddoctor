import React, { useEffect, useState, useCallback } from "react";
import api from "../api/axios";
import { useToast } from "../components/Toast";
import PageHeader from "../components/PageHeader";
import Modal from "../components/Modal";

// ── All settings the Android app reads from the backend ──────────────────────
// Grouped by category with label, description, type, and default value.
const SETTING_DEFINITIONS = [
  // ── App / System ────────────────────────────────────────────────────────────
  { key: "app_maintenance_mode",      label: "Maintenance Mode",          category: "SYSTEM",   type: "bool",   default: "false", description: "Show maintenance screen to all users. App becomes read-only." },
  { key: "force_update_android",      label: "Force Update (Android)",    category: "SYSTEM",   type: "bool",   default: "false", description: "Force users to update the Android app before using it." },
  { key: "app_version_android",       label: "Min Android Version",       category: "SYSTEM",   type: "text",   default: "1.0",   description: "Minimum required Android app version (e.g. 1.2). Users below this are prompted to update." },
  { key: "announcement_text",         label: "Announcement Banner Text",  category: "SYSTEM",   type: "text",   default: "",      description: "Text shown in a banner on the dashboard. Leave empty to hide." },
  { key: "announcement_color",        label: "Announcement Banner Color", category: "SYSTEM",   type: "text",   default: "#F59E0B", description: "Hex color for the announcement banner (e.g. #F59E0B for amber)." },

  // ── Feature Flags ───────────────────────────────────────────────────────────
  { key: "feature_hospitals",         label: "Show Hospitals",            category: "FEATURES", type: "bool",   default: "true",  description: "Show Hospitals tile on dashboard and explore screen." },
  { key: "feature_doctors",           label: "Show Doctors",              category: "FEATURES", type: "bool",   default: "true",  description: "Show Doctors tile on dashboard and explore screen." },
  { key: "feature_ambulance",         label: "Show Ambulances",           category: "FEATURES", type: "bool",   default: "true",  description: "Show Ambulance tile on dashboard." },
  { key: "feature_pharmacies",        label: "Show Pharmacies",           category: "FEATURES", type: "bool",   default: "true",  description: "Show Pharmacies tile on dashboard." },
  { key: "feature_clinics",           label: "Show Clinics",              category: "FEATURES", type: "bool",   default: "true",  description: "Show Clinics tile on dashboard." },
  { key: "feature_diagnostics",       label: "Show Diagnostics",          category: "FEATURES", type: "bool",   default: "true",  description: "Show Diagnostics tile on dashboard." },
  { key: "feature_blood_banks",       label: "Show Blood Banks",          category: "FEATURES", type: "bool",   default: "true",  description: "Show Blood Banks tile on dashboard." },
  { key: "feature_telemedicine",      label: "Show Telemedicine",         category: "FEATURES", type: "bool",   default: "true",  description: "Show Telemedicine tile on dashboard." },
  { key: "feature_request_blood",     label: "Show Request Blood",        category: "FEATURES", type: "bool",   default: "true",  description: "Show Request Blood tile on dashboard." },
  { key: "feature_blood_requests",    label: "Show Blood Requests",       category: "FEATURES", type: "bool",   default: "true",  description: "Show Blood Requests tile on dashboard." },
  { key: "feature_donor_list",        label: "Show Donor List",           category: "FEATURES", type: "bool",   default: "true",  description: "Show Donor List tile on dashboard." },
  { key: "telemedicine_enabled",      label: "Telemedicine Feature",      category: "FEATURES", type: "bool",   default: "true",  description: "Enable/disable the telemedicine consultation feature globally." },
  { key: "donor_search_enabled",      label: "Donor Search",              category: "FEATURES", type: "bool",   default: "true",  description: "Allow users to search for blood donors." },
  { key: "blood_request_guest",       label: "Guest Blood Requests",      category: "FEATURES", type: "bool",   default: "true",  description: "Allow non-logged-in users to submit blood requests." },

  // ── Emergency ───────────────────────────────────────────────────────────────
  { key: "emergency_sla_minutes",     label: "Emergency SLA (minutes)",   category: "EMERGENCY", type: "number", default: "5",    description: "Target dispatch time in minutes shown on emergency screen." },

  // ── Reminders ───────────────────────────────────────────────────────────────
  { key: "max_reminders_per_user",    label: "Max Reminders Per User",    category: "REMINDERS", type: "number", default: "20",   description: "Maximum number of medicine reminders a user can create." },

  // ── Ads — General ───────────────────────────────────────────────────────────
  { key: "ads_enabled",               label: "Ads Enabled",               category: "ADS",      type: "bool",   default: "true",  description: "Master switch — disabling this hides all ads in the Android app." },
  { key: "ads_provider_priority",     label: "Ad Provider Priority",      category: "ADS",      type: "select", default: "admob", options: ["admob","facebook","both"], description: "Which ad network to use. 'both' shows AdMob first, falls back to Facebook." },
  { key: "ads_banner_enabled",        label: "Banner Ads",                category: "ADS",      type: "bool",   default: "true",  description: "Show banner ads on dashboard and browse screens." },
  { key: "ads_interstitial_enabled",  label: "Interstitial Ads",          category: "ADS",      type: "bool",   default: "true",  description: "Show full-screen interstitial ads between screens." },
  { key: "ads_rewarded_enabled",      label: "Rewarded Ads",              category: "ADS",      type: "bool",   default: "true",  description: "Show rewarded video ads (user earns points)." },
  { key: "ads_interstitial_every_n_clicks", label: "Interstitial Every N Clicks", category: "ADS", type: "number", default: "4", description: "Show interstitial ad every N navigation clicks." },
  { key: "ads_interstitial_cooldown_seconds", label: "Interstitial Cooldown (s)", category: "ADS", type: "number", default: "90", description: "Minimum seconds between two interstitial ads." },
  { key: "ads_rewarded_points",       label: "Rewarded Ad Points",        category: "ADS",      type: "number", default: "5",    description: "Points awarded to user for watching a rewarded ad." },
  { key: "ads_rewarded_unit",         label: "Rewarded Unit Label",       category: "ADS",      type: "text",   default: "Support Points", description: "Label shown next to rewarded ad points (e.g. 'Support Points')." },
  { key: "ads_tag_for_child_directed_treatment", label: "Child-Directed Treatment", category: "ADS", type: "bool", default: "false", description: "Tag ads for child-directed treatment (COPPA compliance)." },
  { key: "ads_tag_for_under_age_of_consent",     label: "Under Age of Consent",     category: "ADS", type: "bool", default: "false", description: "Tag ads for users under age of consent (GDPR)." },
  { key: "ads_max_ad_content_rating", label: "Max Ad Content Rating",     category: "ADS",      type: "select", default: "T",   options: ["G","PG","T","MA"], description: "Maximum content rating for ads. G=General, PG=Parental Guidance, T=Teen, MA=Mature." },

  // ── Ads — AdMob Unit IDs ────────────────────────────────────────────────────
  { key: "admob_banner_unit_id",       label: "AdMob Banner Unit ID",       category: "ADS_IDS", type: "text", default: "ca-app-pub-3940256099942544/6300978111",  description: "AdMob banner ad unit ID. Use test ID in development." },
  { key: "admob_interstitial_unit_id", label: "AdMob Interstitial Unit ID", category: "ADS_IDS", type: "text", default: "ca-app-pub-3940256099942544/1033173712",  description: "AdMob interstitial ad unit ID." },
  { key: "admob_rewarded_unit_id",     label: "AdMob Rewarded Unit ID",     category: "ADS_IDS", type: "text", default: "ca-app-pub-3940256099942544/5224354917",  description: "AdMob rewarded video ad unit ID." },
  { key: "admob_app_open_unit_id",     label: "AdMob App Open Unit ID",     category: "ADS_IDS", type: "text", default: "ca-app-pub-3940256099942544/9257391923",  description: "AdMob app open ad unit ID (shown on app launch)." },

  // ── Ads — Facebook Audience Network ────────────────────────────────────────
  { key: "fb_banner_placement_id",       label: "Facebook Banner Placement ID",       category: "ADS_IDS", type: "text", default: "", description: "Facebook Audience Network banner placement ID." },
  { key: "fb_interstitial_placement_id", label: "Facebook Interstitial Placement ID", category: "ADS_IDS", type: "text", default: "", description: "Facebook Audience Network interstitial placement ID." },
];

const CATEGORY_META = {
  SYSTEM:    { label: "⚙️ App & System",        color: "#8b5cf6", desc: "Core app behavior, maintenance, and version control" },
  FEATURES:  { label: "🔧 Feature Flags",        color: "#22c55e", desc: "Show/hide features on the Android dashboard" },
  EMERGENCY: { label: "🆘 Emergency",            color: "#ef4444", desc: "Emergency response settings" },
  REMINDERS: { label: "💊 Medicine Reminders",   color: "#f59e0b", desc: "Reminder limits and behavior" },
  ADS:       { label: "📢 Ads Configuration",    color: "#3b82f6", desc: "Ad network settings and behavior" },
  ADS_IDS:   { label: "🔑 Ad Unit IDs",          color: "#6366f1", desc: "AdMob and Facebook ad unit IDs" },
  GENERAL:   { label: "📋 General",              color: "#94a3b8", desc: "Other settings" },
};

export default function SettingsPage() {
  const toast = useToast();
  const [liveSettings, setLiveSettings] = useState({});   // key → { value, description, category }
  const [localValues,  setLocalValues]  = useState({});   // key → edited value (unsaved)
  const [loading,  setLoading]  = useState(true);
  const [saving,   setSaving]   = useState(null);
  const [seedModal, setSeedModal] = useState(false);
  const [seeding,   setSeeding]  = useState(false);
  const [customModal, setCustomModal] = useState(false);
  const [customForm, setCustomForm] = useState({ settingKey: "", settingValue: "", description: "", category: "GENERAL" });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get("/settings");
      const map = {};
      (Array.isArray(res.data) ? res.data : []).forEach(s => {
        map[s.settingKey] = { value: s.settingValue, description: s.description, category: s.category };
      });

      // Auto-seed any missing settings with their defaults (silent, no toast)
      const missing = SETTING_DEFINITIONS.filter(d => !map[d.key]);
      if (missing.length > 0) {
        await Promise.allSettled(
          missing.map(def =>
            api.post("/settings", {
              settingKey:   def.key,
              settingValue: def.default,
              description:  def.description,
              category:     def.category,
            }).then(() => {
              map[def.key] = { value: def.default, description: def.description, category: def.category };
            })
          )
        );
      }

      setLiveSettings(map);
      setLocalValues({});
    } catch { toast.error("Failed to load settings."); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  // Get effective value: local edit > live > definition default
  const getValue = (key) => {
    if (localValues[key] !== undefined) return localValues[key];
    if (liveSettings[key] !== undefined) return liveSettings[key].value;
    return SETTING_DEFINITIONS.find(d => d.key === key)?.default ?? "";
  };

  const isDirty = (key) => localValues[key] !== undefined && localValues[key] !== (liveSettings[key]?.value ?? "");

  async function saveSetting(key, value, description, category) {
    setSaving(key);
    try {
      await api.post("/settings", { settingKey: key, settingValue: String(value), description, category });
      toast.success(`✓ '${key}' saved.`);
      setLocalValues(prev => { const n = { ...prev }; delete n[key]; return n; });
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Save failed."); }
    finally { setSaving(null); }
  }

  async function handleSeedDefaults() {
    setSeeding(true);
    let ok = 0, fail = 0;
    for (const def of SETTING_DEFINITIONS) {
      try {
        await api.post("/settings", {
          settingKey: def.key,
          settingValue: def.default,
          description: def.description,
          category: def.category,
        });
        ok++;
      } catch { fail++; }
    }
    toast.success(`Re-seeded ${ok} settings.${fail > 0 ? ` ${fail} failed.` : ""}`);
    setSeedModal(false);
    setSeeding(false);
    load();
  }

  async function handleAddCustom() {
    if (!customForm.settingKey.trim()) { toast.error("Key is required."); return; }
    try {
      await api.post("/settings", customForm);
      toast.success("Custom setting added.");
      setCustomModal(false);
      setCustomForm({ settingKey: "", settingValue: "", description: "", category: "GENERAL" });
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Failed."); }
  }

  async function handleDelete(key) {
    if (!window.confirm(`Delete setting '${key}'?`)) return;
    try {
      await api.delete(`/settings/${key}`);
      toast.success("Deleted.");
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Delete failed."); }
  }

  // Group defined settings by category
  const definedByCategory = SETTING_DEFINITIONS.reduce((acc, d) => {
    if (!acc[d.category]) acc[d.category] = [];
    acc[d.category].push(d);
    return acc;
  }, {});

  // Custom settings (in DB but not in SETTING_DEFINITIONS)
  const definedKeys = new Set(SETTING_DEFINITIONS.map(d => d.key));
  const customSettings = Object.entries(liveSettings).filter(([k]) => !definedKeys.has(k));

  const missingCount = SETTING_DEFINITIONS.filter(d => !liveSettings[d.key]).length;
  const dirtyCount   = Object.keys(localValues).filter(k => isDirty(k)).length;

  return (
    <div className="page-content">
      <PageHeader
        title="Android App Settings"
        subtitle="All settings the Android app reads from the backend — changes apply in real-time on next app launch"
      />

      {/* Action bar */}
      <div className="page-header" style={{ marginBottom: 24 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
          {dirtyCount > 0 && (
            <div style={{
              background: "rgba(59,130,246,0.1)", border: "1px solid rgba(59,130,246,0.3)",
              borderRadius: 8, padding: "8px 14px", fontSize: 13, color: "#3b82f6"
            }}>
              {dirtyCount} unsaved change{dirtyCount > 1 ? "s" : ""}
            </div>
          )}
        </div>
        <div className="header-actions">
          <button className="btn-refresh" onClick={load}>↻ Refresh</button>
          <button className="btn-secondary" onClick={() => setSeedModal(true)} title="Reset all settings to their default values">↺ Reset Defaults</button>
          <button className="btn-secondary" onClick={() => setCustomModal(true)}>+ Custom Setting</button>
        </div>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : (
        <>
          {/* Defined settings by category */}
          {Object.entries(definedByCategory).map(([cat, defs]) => {
            const meta = CATEGORY_META[cat] || { label: cat, color: "#94a3b8", desc: "" };
            return (
              <div key={cat} className="section-card" style={{ marginBottom: 20, borderTop: `3px solid ${meta.color}` }}>
                <div className="section-header" style={{ marginBottom: 4 }}>
                  <div>
                    <h2 style={{ display: "flex", alignItems: "center", gap: 8, margin: 0 }}>
                      {meta.label}
                    </h2>
                    {meta.desc && <p style={{ margin: "4px 0 0", fontSize: 12, color: "var(--text2)" }}>{meta.desc}</p>}
                  </div>
                  <span style={{ fontSize: 12, color: "var(--text3)" }}>
                    {defs.filter(d => liveSettings[d.key]).length}/{defs.length} in DB
                  </span>
                </div>

                <div style={{ marginTop: 16 }}>
                  {defs.map(def => {
                    const val     = getValue(def.key);
                    const inDb    = !!liveSettings[def.key];
                    const dirty   = isDirty(def.key);
                    const isSaving = saving === def.key;

                    return (
                      <div key={def.key} style={{
                        display: "flex", alignItems: "flex-start", justifyContent: "space-between",
                        padding: "14px 0", borderBottom: "1px solid var(--border)",
                        gap: 16, flexWrap: "wrap",
                        background: dirty ? "rgba(59,130,246,0.03)" : undefined,
                      }}>
                        {/* Info */}
                        <div style={{ flex: 1, minWidth: 220 }}>
                          <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
                            <code style={{ color: "var(--primary)", fontSize: 12, fontWeight: 600 }}>{def.key}</code>
                            {!inDb && <span style={{ fontSize: 10, background: "rgba(245,158,11,0.15)", color: "#f59e0b", borderRadius: 4, padding: "1px 6px" }}>NOT IN DB</span>}
                            {dirty && <span style={{ fontSize: 10, background: "rgba(59,130,246,0.15)", color: "#3b82f6", borderRadius: 4, padding: "1px 6px" }}>UNSAVED</span>}
                          </div>
                          <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text)", marginTop: 2 }}>{def.label}</div>
                          <div style={{ fontSize: 12, color: "var(--text2)", marginTop: 2, lineHeight: 1.4 }}>{def.description}</div>
                          <div style={{ fontSize: 11, color: "var(--text3)", marginTop: 2 }}>Default: <code>{def.default || "(empty)"}</code></div>
                        </div>

                        {/* Control */}
                        <div style={{ display: "flex", alignItems: "center", gap: 8, flexShrink: 0 }}>
                          {def.type === "bool" ? (
                            <label className="toggle-switch" title={val === "true" ? "Enabled" : "Disabled"}>
                              <input type="checkbox"
                                checked={val === "true"}
                                onChange={e => {
                                  const newVal = e.target.checked ? "true" : "false";
                                  saveSetting(def.key, newVal, def.description, def.category);
                                }}
                                disabled={isSaving}
                              />
                              <span className="toggle-slider" />
                            </label>
                          ) : def.type === "select" ? (
                            <select
                              value={val}
                              onChange={e => setLocalValues(prev => ({ ...prev, [def.key]: e.target.value }))}
                              style={{ background: "var(--bg3)", border: "1px solid var(--border)", borderRadius: 6, padding: "6px 10px", color: "var(--text)", fontSize: 13 }}
                            >
                              {def.options.map(o => <option key={o} value={o}>{o}</option>)}
                            </select>
                          ) : (
                            <input
                              type={def.type === "number" ? "number" : "text"}
                              value={val}
                              onChange={e => setLocalValues(prev => ({ ...prev, [def.key]: e.target.value }))}
                              style={{
                                background: "var(--bg3)", border: `1px solid ${dirty ? "#3b82f6" : "var(--border)"}`,
                                borderRadius: 6, padding: "6px 10px", color: "var(--text)", fontSize: 13,
                                minWidth: def.type === "number" ? 80 : 200,
                              }}
                            />
                          )}

                          {/* Save button for non-bool types */}
                          {def.type !== "bool" && (
                            <button
                              className={dirty ? "btn-primary" : "btn-secondary"}
                              style={{ padding: "6px 12px", fontSize: 12, opacity: isSaving ? 0.6 : 1 }}
                              onClick={() => saveSetting(def.key, val, def.description, def.category)}
                              disabled={isSaving}
                            >
                              {isSaving ? "…" : dirty ? "Save" : inDb ? "Re-save" : "Create"}
                            </button>
                          )}

                          {inDb && (
                            <button className="btn-action btn-danger" style={{ padding: "6px 8px", fontSize: 12 }}
                              onClick={() => handleDelete(def.key)} title="Delete from DB">✕</button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })}

          {/* Custom / unknown settings */}
          {customSettings.length > 0 && (
            <div className="section-card" style={{ marginBottom: 20, borderTop: "3px solid #94a3b8" }}>
              <div className="section-header">
                <h2>📋 Custom Settings ({customSettings.length})</h2>
                <span style={{ fontSize: 12, color: "var(--text3)" }}>Not part of standard Android settings</span>
              </div>
              {customSettings.map(([key, s]) => (
                <div key={key} style={{
                  display: "flex", alignItems: "center", justifyContent: "space-between",
                  padding: "12px 0", borderBottom: "1px solid var(--border)", gap: 16, flexWrap: "wrap"
                }}>
                  <div style={{ flex: 1 }}>
                    <code style={{ color: "var(--primary)", fontSize: 12 }}>{key}</code>
                    {s.description && <p style={{ margin: "2px 0 0", fontSize: 12, color: "var(--text2)" }}>{s.description}</p>}
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    {s.value === "true" || s.value === "false" ? (
                      <label className="toggle-switch">
                        <input type="checkbox" checked={s.value === "true"}
                          onChange={e => saveSetting(key, e.target.checked ? "true" : "false", s.description, s.category)}
                          disabled={saving === key} />
                        <span className="toggle-slider" />
                      </label>
                    ) : (
                      <>
                        <input
                          value={localValues[key] ?? s.value}
                          onChange={e => setLocalValues(prev => ({ ...prev, [key]: e.target.value }))}
                          style={{ background: "var(--bg3)", border: "1px solid var(--border)", borderRadius: 6, padding: "6px 10px", color: "var(--text)", fontSize: 13, minWidth: 180 }}
                        />
                        <button className="btn-primary" style={{ padding: "6px 12px", fontSize: 12 }}
                          onClick={() => saveSetting(key, localValues[key] ?? s.value, s.description, s.category)}
                          disabled={saving === key}>
                          {saving === key ? "…" : "Save"}
                        </button>
                      </>
                    )}
                    <button className="btn-action btn-danger" style={{ padding: "6px 8px", fontSize: 12 }}
                      onClick={() => handleDelete(key)}>✕</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* Seed defaults modal */}
      <Modal open={seedModal} title="Reset All Settings to Defaults"
        onClose={() => setSeedModal(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setSeedModal(false)}>Cancel</button>
            <button className="btn-danger" onClick={handleSeedDefaults} disabled={seeding}>
              {seeding ? "Resetting…" : `Reset All ${SETTING_DEFINITIONS.length} Settings`}
            </button>
          </>
        }
      >
        <p style={{ color: "var(--text2)", fontSize: 14, lineHeight: 1.6 }}>
          This will <strong>overwrite all {SETTING_DEFINITIONS.length} settings</strong> with their default values.
          Any custom values you have set will be lost.
        </p>
        <p style={{ color: "var(--danger)", fontSize: 12, marginTop: 8 }}>
          ⚠ This action cannot be undone. Use only to reset to factory defaults.
        </p>
      </Modal>

      {/* Add custom setting modal */}
      <Modal open={customModal} title="Add Custom Setting"
        onClose={() => setCustomModal(false)}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setCustomModal(false)}>Cancel</button>
            <button className="btn-primary" onClick={handleAddCustom}>Add</button>
          </>
        }
      >
        <div className="form-grid">
          {[
            { key: "settingKey",   label: "Key *",      placeholder: "e.g. my_custom_flag" },
            { key: "settingValue", label: "Value *",    placeholder: "e.g. true" },
            { key: "description",  label: "Description", placeholder: "What does this control?" },
          ].map(f => (
            <div key={f.key} className="form-field">
              <label>{f.label}</label>
              <input value={customForm[f.key]} placeholder={f.placeholder}
                onChange={e => setCustomForm({ ...customForm, [f.key]: e.target.value })} />
            </div>
          ))}
          <div className="form-field">
            <label>Category</label>
            <select value={customForm.category}
              onChange={e => setCustomForm({ ...customForm, category: e.target.value })}>
              {["GENERAL","SYSTEM","FEATURES","EMERGENCY","BLOOD","REMINDERS","ADS","ADS_IDS","UI"].map(c =>
                <option key={c} value={c}>{c}</option>
              )}
            </select>
          </div>
        </div>
      </Modal>

      <style>{`
        .toggle-switch { position: relative; display: inline-block; width: 44px; height: 22px; cursor: pointer; }
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
        .toggle-switch input:disabled + .toggle-slider { opacity: 0.5; cursor: not-allowed; }
      `}</style>
    </div>
  );
}
