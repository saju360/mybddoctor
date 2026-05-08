package com.lifeplus.healthcare.network;

import com.lifeplus.healthcare.util.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * Attaches the JWT Bearer token to every outgoing request automatically.
 * If the server returns 401, attempts a token refresh once, then retries.
 */
public class AuthInterceptor implements Interceptor {

    private final SessionManager session;

    public AuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Skip auth header for auth endpoints
        String url = original.url().toString();
        if (url.contains("/auth/login") || url.contains("/auth/register")
                || url.contains("/auth/refresh")) {
            return chain.proceed(original);
        }

        String token = session.getAccessToken();
        Request request = token != null
                ? original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build()
                : original;

        Response response = chain.proceed(request);

        // 401 -> try refresh once
        if (response.code() == 401) {
            synchronized (this) {
                if (session.getRefreshToken() != null) {
                    response.close();
                    String newToken = TokenRefresher.refresh(session);
                    if (newToken != null) {
                        Request retried = original.newBuilder()
                                .header("Authorization", "Bearer " + newToken)
                                .build();
                        return chain.proceed(retried);
                    } else {
                        // Refresh failed, clear session and force logout logic could go here
                        session.clearSession();
                    }
                }
            }
        }
        return response;
    }
}

