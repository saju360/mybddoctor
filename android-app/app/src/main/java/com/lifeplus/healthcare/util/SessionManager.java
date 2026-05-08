package com.lifeplus.healthcare.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Secure token storage using EncryptedSharedPreferences (AES-256).
 * Singleton — call SessionManager.get(context) anywhere.
 */
public final class SessionManager {

    private static final String PREFS_FILE    = "lifeplus_secure_prefs";
    private static final String KEY_ACCESS    = "access_token";
    private static final String KEY_REFRESH   = "refresh_token";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_ROLE      = "user_role";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE     = "phone";

    private static volatile SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context ctx) {
        SharedPreferences p;
        try {
            MasterKey masterKey = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            p = EncryptedSharedPreferences.create(
                    ctx, PREFS_FILE, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to plain prefs if encryption unavailable (very old devices)
            p = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        }
        this.prefs = p;
    }

    public static SessionManager get(Context ctx) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) instance = new SessionManager(ctx.getApplicationContext());
            }
        }
        return instance;
    }

    public void saveSession(String accessToken, String refreshToken,
                            long userId, String role) {
        prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public void saveProfile(String fullName, String phone) {
        prefs.edit()
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_PHONE, phone)
                .apply();
    }

    public void updateAccessToken(String accessToken) {
        prefs.edit().putString(KEY_ACCESS, accessToken).apply();
    }

    public String getAccessToken()  { return prefs.getString(KEY_ACCESS, null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH, null); }
    public long   getUserId()       { return prefs.getLong(KEY_USER_ID, -1); }
    public String getRole()         { return prefs.getString(KEY_ROLE, "USER"); }
    public String getFullName()     { return prefs.getString(KEY_FULL_NAME, ""); }
    public String getPhone()        { return prefs.getString(KEY_PHONE, ""); }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    /** Returns "Bearer <token>" or null if not logged in. */
    public String bearerToken() {
        String t = getAccessToken();
        return t != null ? "Bearer " + t : null;
    }
}
