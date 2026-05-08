package com.lifeplus.healthcare.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.lifeplus.healthcare.network.ApiClient;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reads global settings from the backend and caches them locally.
 * Android reads these on startup to apply admin-controlled configuration.
 *
 * Usage: AppSettings.get(context).isFeatureEnabled("telemedicine_enabled")
 */
public final class AppSettings {

    private static final String PREFS = "app_settings_cache";
    private static volatile AppSettings instance;

    private final SharedPreferences prefs;

    private AppSettings(Context ctx) {
        this.prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static AppSettings get(Context ctx) {
        if (instance == null) {
            synchronized (AppSettings.class) {
                if (instance == null) instance = new AppSettings(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    /** Fetch settings from backend and cache locally. Call on app startup. */
    public void syncFromBackend(Context ctx) {
        ApiClient.get(ctx).getSettings().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    for (Map<String, Object> s : response.body()) {
                        String key   = String.valueOf(s.get("settingKey"));
                        String value = String.valueOf(s.get("settingValue"));
                        editor.putString(key, value);
                    }
                    editor.apply();
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // Use cached values — no crash
            }
        });
    }

    // ── Typed getters ─────────────────────────────────────────────────────────

    public String get(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public boolean isEnabled(String key, boolean defaultValue) {
        String val = prefs.getString(key, null);
        if (val == null) return defaultValue;
        return "true".equalsIgnoreCase(val);
    }

    public int getInt(String key, int defaultValue) {
        try { return Integer.parseInt(prefs.getString(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    // ── Convenience methods ───────────────────────────────────────────────────

    public boolean isMaintenanceMode()      { return isEnabled("app_maintenance_mode",   false); }
    public boolean isTelemedicineEnabled()  { return isEnabled("telemedicine_enabled",   true); }
    public boolean isDonorSearchEnabled()   { return isEnabled("donor_search_enabled",   true); }
    public boolean isBloodRequestGuest()    { return isEnabled("blood_request_guest",    true); }
    public boolean isForceUpdate()          { return isEnabled("force_update_android",   false); }
    public int     getEmergencySlaMinutes() { return getInt("emergency_sla_minutes",     5); }
    public int     getMaxReminders()        { return getInt("max_reminders_per_user",    20); }
    public String  getAnnouncementText()    { return get("announcement_text",            ""); }
    public String  getAnnouncementColor()   { return get("announcement_color",           "#F59E0B"); }
    public String  getMinAndroidVersion()   { return get("app_version_android",          "1.0"); }

    // Ads settings (managed from admin panel)
    public boolean isAdsEnabled()                  { return isEnabled("ads_enabled", true); }
    public String  getAdProviderPriority()         { return get("ads_provider_priority", "admob").toLowerCase(Locale.ROOT); }
    public boolean isBannerEnabled()               { return isEnabled("ads_banner_enabled", true); }
    public boolean isInterstitialEnabled()         { return isEnabled("ads_interstitial_enabled", true); }
    public boolean isRewardedEnabled()             { return isEnabled("ads_rewarded_enabled", true); }
    public String  getAdmobBannerUnitId()          { return get("admob_banner_unit_id", "ca-app-pub-3940256099942544/6300978111"); }
    public String  getAdmobInterstitialUnitId()    { return get("admob_interstitial_unit_id", "ca-app-pub-3940256099942544/1033173712"); }
    public String  getAdmobRewardedUnitId()        { return get("admob_rewarded_unit_id", "ca-app-pub-3940256099942544/5224354917"); }
    public String  getAdmobAppOpenUnitId()        { return get("admob_app_open_unit_id", "ca-app-pub-3940256099942544/9257391923"); }
    public String  getFacebookBannerPlacementId()  { return get("fb_banner_placement_id", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"); }
    public String  getFacebookInterstitialPlacementId() { return get("fb_interstitial_placement_id", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"); }
    public int     getRewardedPoints()             { return getInt("ads_rewarded_points", 5); }
    public String  getRewardedUnit()               { return get("ads_rewarded_unit", "Support Points"); }
    public int     getInterstitialEveryNClicks()   { return getInt("ads_interstitial_every_n_clicks", 4); }
    public int     getInterstitialCooldownSeconds(){ return getInt("ads_interstitial_cooldown_seconds", 90); }
    public boolean isTagForChildDirectedTreatment() { return isEnabled("ads_tag_for_child_directed_treatment", false); }
    public boolean isTagForUnderAgeOfConsent()      { return isEnabled("ads_tag_for_under_age_of_consent", false); }
    public String  getMaxAdContentRating()          { return get("ads_max_ad_content_rating", "T"); }
}
