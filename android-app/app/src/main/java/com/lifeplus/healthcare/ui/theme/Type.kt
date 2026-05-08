package com.lifeplus.healthcare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.R

@OptIn(ExperimentalTextApi::class)
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@OptIn(ExperimentalTextApi::class)
val HindSiliguriFont = GoogleFont("Hind Siliguri")

@OptIn(ExperimentalTextApi::class)
val NunitoSansFont = GoogleFont("Nunito Sans")

@OptIn(ExperimentalTextApi::class)
val HindSiliguri = FontFamily(
    Font(googleFont = HindSiliguriFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = HindSiliguriFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = HindSiliguriFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = HindSiliguriFont, fontProvider = provider, weight = FontWeight.Normal)
)

@OptIn(ExperimentalTextApi::class)
val NunitoSans = FontFamily(
    Font(googleFont = NunitoSansFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = NunitoSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = NunitoSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NunitoSansFont, fontProvider = provider, weight = FontWeight.Normal)
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HindSiliguri,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = HindSiliguri,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    )
)
