package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.local.SessionDataStore
import com.lifeplus.healthcare.data.repository.AuthRepository
import com.lifeplus.healthcare.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false,
    val otpVerified: Boolean = false,
    val passwordResetSuccess: Boolean = false,
    val demoOtpCode: String? = null,
    val isDemoOtpMode: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val session: SessionDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    val isLoggedIn: StateFlow<Boolean> = session.isLoggedIn.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val isOnboardingCompleted: StateFlow<Boolean> = session.isOnboardingCompleted.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val fullName: StateFlow<String?> = session.fullName.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val phone: StateFlow<String?> = session.phone.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val email: StateFlow<String?> = session.email.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val userId: StateFlow<Long?> = session.userId.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val bloodGroup: StateFlow<String?> = session.bloodGroup.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val district: StateFlow<String?> = session.district.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val role: StateFlow<String?> = session.role.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val profileImageUri: StateFlow<String?> = session.profileImageUri.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    fun saveProfileImage(uriString: String) {
        viewModelScope.launch {
            session.saveProfileImageUri(uriString)
        }
    }

    fun login(phone: String, password: String) {
        val phoneClean = phone.trim()
        val pwdClean   = password.trim()
        when {
            phoneClean.isBlank() -> { _state.value = AuthUiState(error = "Phone number is required."); return }
            pwdClean.isBlank()   -> { _state.value = AuthUiState(error = "Password is required."); return }
            phoneClean.length < 10 -> { _state.value = AuthUiState(error = "Enter a valid phone number."); return }
        }
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            when (val result = repository.login(phoneClean, pwdClean)) {
                is Resource.Success -> _state.value = AuthUiState(isSuccess = true)
                is Resource.Error   -> _state.value = AuthUiState(error = result.message)
                Resource.Loading    -> Unit
            }
        }
    }

    fun register(
        fullName: String, phone: String, email: String,
        password: String, confirmPassword: String,
        bloodGroup: String, district: String,
        otpCode: String? = null
    ) {
        val nameClean  = fullName.trim()
        val phoneClean = phone.trim()
        val emailClean = email.trim()
        when {
            nameClean.isBlank()  -> { _state.value = AuthUiState(error = "Full name is required."); return }
            phoneClean.isBlank() -> { _state.value = AuthUiState(error = "Phone number is required."); return }
            emailClean.isBlank() -> { _state.value = AuthUiState(error = "Email is required."); return }
            !emailClean.contains("@") -> { _state.value = AuthUiState(error = "Enter a valid email address."); return }
            password.length < 6  -> { _state.value = AuthUiState(error = "Password must be at least 6 characters."); return }
            password != confirmPassword -> { _state.value = AuthUiState(error = "Passwords do not match."); return }
            otpCode.isNullOrBlank() -> { _state.value = AuthUiState(error = "OTP verification is required."); return }
        }
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            when (val result = repository.register(nameClean, phoneClean, emailClean, password, bloodGroup, district, otpCode)) {
                is Resource.Success -> _state.value = AuthUiState(isSuccess = true)
                is Resource.Error   -> _state.value = AuthUiState(error = result.message)
                Resource.Loading    -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    fun updateProfile(fullName: String, email: String, district: String, bloodGroup: String) = viewModelScope.launch {
        _state.value = AuthUiState(isLoading = true)
        val body = mapOf("fullName" to fullName, "email" to email, "district" to district, "bloodGroup" to bloodGroup)
        userId.value?.let { id ->
            when (val result = repository.updateUser(id, body)) {
                is Resource.Success -> {
                    session.updateUserInfo(fullName, email, bloodGroup, district)
                    _state.value = AuthUiState(isSuccess = true)
                }
                is Resource.Error -> _state.value = AuthUiState(error = result.message)
                else -> {}
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { session.saveOnboardingCompleted(true) }
    }

    fun requestOtp(phone: String) {
        if (phone.isBlank()) {
            _state.value = AuthUiState(error = "Phone number is required")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            when (val result = repository.requestOtp(phone.trim())) {
                is Resource.Success -> _state.value = AuthUiState(
                    otpSent = true,
                    demoOtpCode = result.data.demoOtpCode,
                    isDemoOtpMode = result.data.demoMode
                )
                is Resource.Error -> _state.value = AuthUiState(error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun verifyOtp(phone: String, code: String) {
        if (code.isBlank()) {
            _state.value = AuthUiState(otpSent = true, error = "OTP code is required")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.verifyOtp(phone.trim(), code.trim())) {
                is Resource.Success -> _state.value = _state.value.copy(isLoading = false, otpVerified = true)
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, otpSent = true, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun forgotPasswordRequest(phone: String) {
        if (phone.isBlank()) {
            _state.value = AuthUiState(error = "Phone number is required")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            when (val result = repository.forgotPasswordRequest(phone.trim())) {
                is Resource.Success -> _state.value = AuthUiState(
                    otpSent = true,
                    demoOtpCode = result.data.demoOtpCode,
                    isDemoOtpMode = result.data.demoMode
                )
                is Resource.Error -> _state.value = AuthUiState(error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun forgotPasswordReset(phone: String, otpCode: String, newPassword: String, confirmPassword: String) {
        if (phone.isBlank() || otpCode.isBlank()) {
            _state.value = _state.value.copy(error = "Phone and OTP are required")
            return
        }
        if (newPassword.length < 8) {
            _state.value = _state.value.copy(error = "Password must be at least 8 characters")
            return
        }
        if (newPassword != confirmPassword) {
            _state.value = _state.value.copy(error = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.forgotPasswordReset(phone.trim(), otpCode.trim(), newPassword, confirmPassword)) {
                is Resource.Success -> _state.value = _state.value.copy(isLoading = false, passwordResetSuccess = true)
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }
}
