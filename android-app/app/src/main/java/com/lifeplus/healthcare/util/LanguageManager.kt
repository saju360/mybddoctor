package com.lifeplus.healthcare.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {

    /**
     * Apply the given language code app-wide.
     *
     * On API 33+ (Android 13) the system handles locale persistence if
     * `android:localeConfig` is defined in the manifest.
     *
     * For older versions, AppCompat handles persistence via the
     * `AppLocalesMetadataHolderService` with `autoStoreLocales="true"`.
     *
     * On API 24–32 `AppCompatDelegate.setApplicationLocales()` triggers an
     * Activity recreation so the new locale takes effect immediately.
     *
     * On API < 24 we fall back to updating the Configuration directly.
     */
    fun applyLanguage(languageCode: String) {
        val tag = if (languageCode.lowercase() == "bn") "bn" else "en"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    /**
     * Wrap a Context so that resources resolve in the saved language.
     * Call this from Activity.attachBaseContext() to ensure the locale is
     * applied before any view inflation.
     */
    fun wrapContext(context: Context, languageCode: String): Context {
        val tag = if (languageCode.lowercase() == "bn") "bn" else "en"
        val locale = Locale(tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
