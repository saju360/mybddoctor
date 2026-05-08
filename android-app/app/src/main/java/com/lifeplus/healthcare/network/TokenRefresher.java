package com.lifeplus.healthcare.network;

import com.google.gson.Gson;
import com.lifeplus.healthcare.model.AuthModels;
import com.lifeplus.healthcare.util.SessionManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

/**
 * Synchronous token refresh — called from AuthInterceptor on 401.
 * Uses a plain OkHttpClient (no interceptors) to avoid infinite loops.
 */
public final class TokenRefresher {

    private static final OkHttpClient plain = new OkHttpClient();
    private static final Gson gson = new Gson();

    private TokenRefresher() {}

    /** Returns new access token or null on failure. */
    public static String refresh(SessionManager session) {
        try {
            String refreshToken = session.getRefreshToken();
            if (refreshToken == null) return null;

            String body = gson.toJson(new AuthModels.RefreshRequest(refreshToken));
            Request req = new Request.Builder()
                    .url(ApiConfig.BASE_URL + "auth/refresh")
                    .post(RequestBody.create(body,
                            MediaType.parse("application/json")))
                    .build();

            try (Response resp = plain.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONObject json = new JSONObject(resp.body().string());
                    String newToken = json.optString("accessToken", null);
                    if (newToken != null) {
                        session.updateAccessToken(newToken);
                        return newToken;
                    }
                }
            }
        } catch (Exception ignored) {}
        // Refresh failed — clear session so user is sent to login
        session.clearSession();
        return null;
    }
}
