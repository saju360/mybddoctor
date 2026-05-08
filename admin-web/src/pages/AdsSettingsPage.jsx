import React, { useEffect, useState } from "react";
import api from "../api/axios";
import PageHeader from "../components/PageHeader";
import { useToast } from "../components/Toast";

const FIELDS = [
  { key: "ads_enabled", label: "Enable All Ads", type: "bool", value: "true" },
  { key: "ads_provider_priority", label: "Primary Provider", type: "text", value: "admob" },
  { key: "ads_banner_enabled", label: "Enable Banner", type: "bool", value: "true" },
  { key: "ads_interstitial_enabled", label: "Enable Interstitial", type: "bool", value: "true" },
  { key: "ads_rewarded_enabled", label: "Enable Rewarded", type: "bool", value: "true" },
  { key: "ads_rewarded_points", label: "Rewarded Ad Points", type: "text", value: "5" },
  { key: "ads_rewarded_unit", label: "Rewarded Unit Label", type: "text", value: "Support Points" },
  { key: "ads_interstitial_every_n_clicks", label: "Interstitial Every N Actions", type: "text", value: "4" },
  { key: "ads_interstitial_cooldown_seconds", label: "Interstitial Cooldown Seconds", type: "text", value: "90" },
  { key: "ads_tag_for_child_directed_treatment", label: "Tag For Child Directed Treatment (COPPA)", type: "bool", value: "false" },
  { key: "ads_tag_for_under_age_of_consent", label: "Tag For Under Age Of Consent", type: "bool", value: "false" },
  { key: "ads_max_ad_content_rating", label: "Max Ad Content Rating (G/PG/T/MA)", type: "text", value: "T" },
  { key: "admob_banner_unit_id", label: "AdMob Banner Unit ID", type: "text", value: "ca-app-pub-3940256099942544/6300978111" },
  { key: "admob_interstitial_unit_id", label: "AdMob Interstitial Unit ID", type: "text", value: "ca-app-pub-3940256099942544/1033173712" },
  { key: "admob_rewarded_unit_id", label: "AdMob Rewarded Unit ID", type: "text", value: "ca-app-pub-3940256099942544/5224354917" },
  { key: "admob_app_open_unit_id", label: "AdMob App Open Unit ID", type: "text", value: "ca-app-pub-3940256099942544/9257391923" },
  { key: "fb_banner_placement_id", label: "Facebook Banner Placement ID", type: "text", value: "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID" },
  { key: "fb_interstitial_placement_id", label: "Facebook Interstitial Placement ID", type: "text", value: "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID" },
];

export default function AdsSettingsPage() {
  const toast = useToast();
  const [values, setValues] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      const res = await api.get("/settings");
      const map = {};
      (res.data || []).forEach((row) => { map[row.settingKey] = row.settingValue; });
      setValues(map);
    } catch {
      toast.error("Failed to load ad settings.");
    }
  }

  async function ensureDefaults() {
    setSaving(true);
    try {
      for (const f of FIELDS) {
        if (values[f.key] !== undefined) continue;
        await api.post("/settings", {
          settingKey: f.key,
          settingValue: f.value,
          description: "Ads configuration",
          category: "ADS",
        });
      }
      toast.success("Ad settings defaults added.");
      await load();
    } catch {
      toast.error("Failed to seed defaults.");
    } finally {
      setSaving(false);
    }
  }

  async function saveAll() {
    setSaving(true);
    try {
      for (const f of FIELDS) {
        await api.post("/settings", {
          settingKey: f.key,
          settingValue: String(values[f.key] ?? f.value),
          description: "Ads configuration",
          category: "ADS",
        });
      }
      toast.success("Ad settings saved.");
    } catch {
      toast.error("Save failed.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="page-content">
      <PageHeader title="Ads Settings" subtitle="Configure AdMob + Facebook ads from one place" />
      <div className="section-card">
        <div className="section-header" style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
          <h2>Provider & Units</h2>
          <div style={{ display: "flex", gap: 8 }}>
            <button className="btn-refresh" onClick={load}>Refresh</button>
            <button className="btn-secondary" onClick={ensureDefaults} disabled={saving}>Seed Test Defaults</button>
            <button className="btn-primary" onClick={saveAll} disabled={saving}>{saving ? "Saving..." : "Save All"}</button>
          </div>
        </div>
        <div className="form-grid">
          {FIELDS.map((f) => (
            <div className="form-field" key={f.key}>
              <label>{f.label}</label>
              {f.type === "bool" ? (
                <select
                  value={String(values[f.key] ?? f.value)}
                  onChange={(e) => setValues((v) => ({ ...v, [f.key]: e.target.value }))}
                >
                  <option value="true">true</option>
                  <option value="false">false</option>
                </select>
              ) : (
                <input
                  value={values[f.key] ?? f.value}
                  onChange={(e) => setValues((v) => ({ ...v, [f.key]: e.target.value }))}
                />
              )}
              <small style={{ color: "var(--text3)" }}>{f.key}</small>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
