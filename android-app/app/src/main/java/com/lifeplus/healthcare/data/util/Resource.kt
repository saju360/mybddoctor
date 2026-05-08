package com.lifeplus.healthcare.data.util

import org.json.JSONObject

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int = 0) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError   get() = this is Error
}

/**
 * Wraps a Retrofit suspend call in a try/catch and maps the result to [Resource].
 * Parses backend JSON error bodies for user-friendly messages.
 */
suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                // 204 No Content — treat as success with Unit-like empty body
                @Suppress("UNCHECKED_CAST")
                Resource.Success(Unit as T)
            }
        } else {
            val errorMessage = parseErrorBody(response) ?: "Error ${response.code()}: ${response.message()}"
            Resource.Error(errorMessage, response.code())
        }
    } catch (e: java.net.UnknownHostException) {
        Resource.Error("Network Error: No internet connection. Please check your network.")
    } catch (e: java.net.SocketTimeoutException) {
        Resource.Error("Timeout Error: Request timed out. Please try again.")
    } catch (e: java.net.ConnectException) {
        Resource.Error("Connection Error: Cannot connect to server. Please try again later.")
    } catch (e: retrofit2.HttpException) {
        Resource.Error("Server Error: ${e.code()} ${e.message()}")
    } catch (e: com.google.gson.JsonSyntaxException) {
        Resource.Error("Parsing Error: Unexpected server response. Please try again.")
    } catch (e: Exception) {
        Resource.Error("Critical Error: ${e.localizedMessage ?: "An unexpected error occurred."}")
    }
}

/** Attempts to extract a human-readable message from the error response body. */
private fun parseErrorBody(response: retrofit2.Response<*>): String? {
    return try {
        val errorBody = response.errorBody()?.string() ?: return null
        val json = JSONObject(errorBody)
        json.optString("message").takeIf { it.isNotBlank() }
            ?: json.optString("error").takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}
