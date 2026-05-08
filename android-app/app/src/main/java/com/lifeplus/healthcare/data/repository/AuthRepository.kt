package com.lifeplus.healthcare.data.repository

import com.lifeplus.healthcare.data.api.ApiService
import com.lifeplus.healthcare.data.local.SessionDataStore
import com.lifeplus.healthcare.data.model.LoginRequest
import com.lifeplus.healthcare.data.model.RegisterRequest
import com.lifeplus.healthcare.data.util.Resource
import com.lifeplus.healthcare.data.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

data class OtpResponse(
    val message: String,
    val demoOtpCode: String? = null,
    val demoMode: Boolean = false
)

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionDataStore
) {
    suspend fun login(phone: String, password: String): Resource<Unit> {
        val result = safeApiCall { api.login(LoginRequest(phone, password)) }
        return when (result) {
            is Resource.Success -> {
                val r = result.data
                if (!r.isVerified) {
                    return Resource.Error("Please verify your phone number.")
                }
                session.saveSession(
                    r.accessToken ?: "",
                    r.refreshToken ?: "",
                    r.userId,
                    r.role ?: "USER",
                    r.fullName ?: "",
                    r.phone ?: phone,
                    "" // email can be fetched if needed or left empty
                )
                Resource.Success(Unit)
            }
            is Resource.Error   -> result
            Resource.Loading    -> Resource.Loading
        }
    }

    suspend fun register(
        fullName: String, phone: String, email: String,
        password: String, bloodGroup: String, district: String,
        otpCode: String? = null
    ): Resource<Unit> {
        val result = safeApiCall {
            api.register(RegisterRequest(fullName, phone, email, password, bloodGroup, district, otpCode))
        }
        return when (result) {
            is Resource.Success -> {
                val r = result.data
                // Tokens may be absent depending on backend auth policy or OTP flow
                if (!r.accessToken.isNullOrBlank()) {
                    session.saveSession(
                        r.accessToken,
                        r.refreshToken ?: "",
                        r.userId,
                        r.role ?: "USER",
                        r.fullName ?: fullName,
                        r.phone ?: phone
                    )
                }
                session.updateUserInfo(
                    fullName = r.fullName ?: fullName,
                    email = email,
                    bloodGroup = bloodGroup,
                    district = district
                )
                Resource.Success(Unit)
            }
            is Resource.Error   -> result
            Resource.Loading    -> Resource.Loading
        }
    }

    suspend fun logout() = session.clearSession()

    suspend fun requestOtp(phone: String): Resource<OtpResponse> {
        val result = safeApiCall { api.requestOtp(mapOf("phone" to phone)) }
        return when (result) {
            is Resource.Success -> Resource.Success(
                OtpResponse(
                    message = result.data["message"] ?: "OTP sent",
                    demoOtpCode = result.data["demoOtpCode"]?.takeIf { it.isNotBlank() },
                    demoMode = result.data["demoMode"]?.toBooleanStrictOrNull() ?: false
                )
            )
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    suspend fun verifyOtp(phone: String, otpCode: String): Resource<String> {
        val result = safeApiCall { api.verifyOtp(mapOf("phone" to phone, "otpCode" to otpCode)) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data["message"] ?: "Verified")
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    suspend fun forgotPasswordRequest(phone: String): Resource<OtpResponse> {
        val result = safeApiCall { api.forgotPasswordRequest(mapOf("phone" to phone)) }
        return when (result) {
            is Resource.Success -> Resource.Success(
                OtpResponse(
                    message = result.data["message"] ?: "OTP sent",
                    demoOtpCode = result.data["demoOtpCode"]?.takeIf { it.isNotBlank() },
                    demoMode = result.data["demoMode"]?.toBooleanStrictOrNull() ?: false
                )
            )
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    suspend fun forgotPasswordReset(phone: String, otpCode: String, newPassword: String, confirmPassword: String): Resource<String> {
        val result = safeApiCall {
            api.forgotPasswordReset(
                mapOf(
                    "phone" to phone,
                    "otpCode" to otpCode,
                    "newPassword" to newPassword,
                    "confirmPassword" to confirmPassword
                )
            )
        }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data["message"] ?: "Password reset successful")
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }

    suspend fun updateUser(id: Long, body: Map<String, String>): Resource<Unit> {
        val result = safeApiCall { api.updateProfile(id, body) }
        return when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }
}
