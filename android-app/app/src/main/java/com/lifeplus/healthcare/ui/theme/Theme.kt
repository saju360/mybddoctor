package com.lifeplus.healthcare.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.lightColorScheme

private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    primaryContainer   = PrimaryLight,
    onPrimaryContainer = Color(0xFF001D35),
    secondary          = Secondary,
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E8D1),
    onSecondaryContainer = Color(0xFF002104),
    tertiary           = Accent,
    onTertiary         = Color(0xFFFFFFFF),
    tertiaryContainer  = AccentLight,
    background         = BackgroundLight,
    surface            = SurfaceLight,
    surfaceVariant     = Surface2Light,
    surfaceTint        = Primary.copy(alpha = 0.05f),
    error              = ErrorColor,
    onError            = Color(0xFFFFFFFF),
    onBackground       = TextPrimary,
    onSurface          = TextPrimary,
    onSurfaceVariant   = TextSecondary,
    outline            = Color(0xFF74777F),
    outlineVariant     = Color(0xFFC4C6D0),
    scrim              = Color(0x99000000),
    inverseSurface     = Color(0xFF2F3033),
    inverseOnSurface   = Color(0xFFF1F0F4),
    inversePrimary     = PrimaryLight,
)

@Composable
fun MediCareTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
