package com.lifeplus.healthcare.network;

import android.content.Context;
import com.lifeplus.healthcare.BuildConfig;
import com.lifeplus.healthcare.util.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public final class ApiClient {

    private static volatile ApiService instance;

    private ApiClient() {}

    public static ApiService get(Context ctx) {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = build(ctx).create(ApiService.class);
                }
            }
        }
        return instance;
    }

    private static Retrofit build(Context ctx) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // Only log bodies in debug builds — never in release
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(SessionManager.get(ctx)))
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /** Call after logout to force re-creation with cleared session. */
    public static void reset() {
        instance = null;
    }
}
