package com.lifeplus.healthcare.ui.screens.auth

data class PasswordMatchResult(
    val error: String? = null,
    val shouldSubmit: Boolean = error == null
)

fun validatePasswordMatch(newPassword: String, confirmPassword: String): PasswordMatchResult {
    if (newPassword != confirmPassword) {
        return PasswordMatchResult(error = "Passwords do not match", shouldSubmit = false)
    }
    return PasswordMatchResult(error = null, shouldSubmit = true)
}

fun isResendEnabled(cooldown: Int): Boolean = cooldown == 0
