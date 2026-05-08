package com.lifeplus.healthcare.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.BuildConfig
import com.lifeplus.healthcare.presentation.viewmodel.LanguageViewModel
import com.lifeplus.healthcare.presentation.viewmodel.SettingsViewModel
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.components.SettingsClickItem
import com.lifeplus.healthcare.ui.components.SettingsSectionHeader
import com.lifeplus.healthcare.ui.components.SettingsToggleItem

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    languageViewModel: LanguageViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()

    fun safeStartActivity(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Settings",
                subtitle = "Control app preferences",
                onBackClick = onNavigateBack
            )

            LazyColumn(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                item { SettingsSectionHeader(title = "Account") }
                item {
                    SettingsToggleItem(
                        label = "Language",
                        subtitle = if (currentLanguage == "bn") "Bangla" else "English",
                        checked = currentLanguage != "bn",
                        onCheckedChange = { isEnglish -> languageViewModel.setLanguage(if (isEnglish) "en" else "bn") }
                    )
                }
                item {
                    SettingsToggleItem(
                        label = "Push Notifications",
                        subtitle = "Receive updates and reminder alerts",
                        checked = notificationsEnabled,
                        onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) }
                    )
                }

                item { HorizontalDivider() }
                item { SettingsSectionHeader(title = "Appearance") }
                item {
                    SettingsClickItem(
                        label = "Dark Mode",
                        subtitle = "Theme customization is coming soon",
                        leadingIcon = Icons.Default.ColorLens,
                        badge = "Coming Soon",
                        onClick = {}
                    )
                }

                item { HorizontalDivider() }
                item { SettingsSectionHeader(title = "About") }

                val aboutItems = listOf(
                    Triple("App Version", "v${BuildConfig.VERSION_NAME}", Icons.Default.Info),
                    Triple("Privacy Policy", "Read how your data is handled", Icons.Default.Description),
                    Triple("Terms of Service", "Review user terms", Icons.Default.Gavel),
                    Triple("Rate the App", "Share your feedback on Play Store", Icons.Default.StarRate),
                    Triple("Contact Support", "support@lifeplus.health", Icons.Default.MailOutline)
                )

                items(aboutItems) { item ->
                    SettingsClickItem(
                        label = item.first,
                        subtitle = item.second,
                        leadingIcon = item.third,
                        onClick = {
                            when (item.first) {
                                "Privacy Policy" -> safeStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lifeplus.health/privacy")))
                                "Terms of Service" -> safeStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lifeplus.health/terms")))
                                "Rate the App" -> {
                                    val pkg = context.packageName
                                    safeStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                                }
                                "Contact Support" -> safeStartActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@lifeplus.health")))
                                else -> Unit
                            }
                        }
                    )
                }
            }
        }
    }
}


