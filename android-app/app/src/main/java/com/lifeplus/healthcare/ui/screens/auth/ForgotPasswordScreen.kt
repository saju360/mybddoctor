package com.lifeplus.healthcare.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.theme.Primary
import com.lifeplus.healthcare.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpRequested by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableIntStateOf(0) }
    var passwordMatchError by remember { mutableStateOf<String?>(null) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.passwordResetSuccess) {
        if (state.passwordResetSuccess) onNavigateBackToLogin()
    }

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            otpRequested = true
            resendCooldown = 60
        }
    }

    LaunchedEffect(otpRequested, resendCooldown) {
        if (otpRequested && resendCooldown > 0) {
            delay(1000)
            resendCooldown -= 1
        }
    }

    val allDisabled = state.isLoading

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBackToLogin) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                }
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = if (otpRequested) "Step 2 of 2" else "Step 1 of 2",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(start = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                placeholder = "01XXXXXXXXX",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !allDisabled
            )

            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = if (otpRequested) {
                    if (isResendEnabled(resendCooldown)) "Resend OTP" else "Resend in ${resendCooldown}s"
                } else {
                    "Send OTP"
                },
                onClick = { viewModel.forgotPasswordRequest(phone) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = Primary,
                isLoading = state.isLoading,
                enabled = !otpRequested || isResendEnabled(resendCooldown)
            )

            if (state.isDemoOtpMode && !state.demoOtpCode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Demo OTP: ${state.demoOtpCode}",
                    color = Primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (otpRequested) {
                Spacer(modifier = Modifier.height(20.dp))

                InputField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = "OTP Code",
                    placeholder = "6 digit OTP",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !allDisabled
                )

                Spacer(modifier = Modifier.height(12.dp))
                InputField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordMatchError = null
                    },
                    label = "New Password",
                    placeholder = "At least 8 characters",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    enabled = !allDisabled
                )

                Spacer(modifier = Modifier.height(12.dp))
                InputField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordMatchError = null
                    },
                    label = "Confirm Password",
                    placeholder = "Re-enter new password",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle confirm password visibility"
                            )
                        }
                    },
                    isError = passwordMatchError != null,
                    errorMessage = passwordMatchError,
                    enabled = !allDisabled
                )

                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Reset Password",
                    onClick = {
                        val result = validatePasswordMatch(newPassword, confirmPassword)
                        if (!result.shouldSubmit) {
                            passwordMatchError = result.error
                            return@PrimaryButton
                        }
                        viewModel.forgotPasswordReset(phone, otp, newPassword, confirmPassword)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = state.isLoading,
                    enabled = !allDisabled
                )
            }

            if (passwordMatchError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(passwordMatchError.orEmpty(), color = Color(0xFFB91C1C), style = MaterialTheme.typography.bodySmall)
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(state.error.orEmpty(), color = Color(0xFFB91C1C), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
