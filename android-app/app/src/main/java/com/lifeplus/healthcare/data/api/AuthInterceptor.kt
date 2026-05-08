package com.lifeplus.healthcare.data.api

import com.lifeplus.healthcare.data.local.SessionDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injects the JWT Bearer token into every outgoing request.
 *
 * NOTE: runBlocking is intentional here — OkHttp interceptors run on a
 * background I/O thread (never the main thread), so blocking that thread
 * briefly to read DataStore is safe and avoids the complexity of a
 * suspending interceptor.  The DataStore read is typically < 1 ms once
 * the preferences file is cached in memory.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val session: SessionDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { session.accessToken.firstOrNull() }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
            // Always send JSON content type for non-GET requests
            if (chain.request().method != "GET") {
                addHeader("Accept", "application/json")
            }
        }.build()

        val response = chain.proceed(request)

        // If 401 and we had a token, the token is expired — clear it
        if (response.code == 401 && !token.isNullOrBlank()) {
            runBlocking { session.clearSession() }
        }

        return response
    }
}
