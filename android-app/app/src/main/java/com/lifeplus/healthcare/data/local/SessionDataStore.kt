package com.lifeplus.healthcare.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medicare_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Exposes the underlying [DataStore] so it can be provided via Hilt for [SettingsViewModel]. */
    val dataStore: DataStore<Preferences> get() = context.dataStore

    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID       = longPreferencesKey("user_id")
        private val KEY_ROLE          = stringPreferencesKey("user_role")
        private val KEY_FULL_NAME     = stringPreferencesKey("full_name")
        private val KEY_PHONE         = stringPreferencesKey("phone")
        private val KEY_EMAIL         = stringPreferencesKey("email")
        private val KEY_BLOOD_GROUP   = stringPreferencesKey("blood_group")
        private val KEY_DISTRICT      = stringPreferencesKey("district")
        private val KEY_LANGUAGE      = stringPreferencesKey("language")
        private val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image_uri")
        private val KEY_ONBOARDING_COMPLETED = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val role: Flow<String?> = context.dataStore.data.map { it[KEY_ROLE] }
    val fullName: Flow<String?> = context.dataStore.data.map { it[KEY_FULL_NAME] }
    val phone: Flow<String?> = context.dataStore.data.map { it[KEY_PHONE] }
    val email: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val bloodGroup: Flow<String?> = context.dataStore.data.map { it[KEY_BLOOD_GROUP] }
    val district: Flow<String?> = context.dataStore.data.map { it[KEY_DISTRICT] }
    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "en" }
    val profileImageUri: Flow<String?> = context.dataStore.data.map { it[KEY_PROFILE_IMAGE] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { !it[KEY_ACCESS_TOKEN].isNullOrEmpty() }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: Long,
        role: String,
        fullName: String,
        phone: String,
        email: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID]       = userId
            prefs[KEY_ROLE]          = role
            prefs[KEY_FULL_NAME]     = fullName
            prefs[KEY_PHONE]         = phone
            prefs[KEY_EMAIL]         = email
        }
    }

    suspend fun updateAccessToken(token: String) {
        context.dataStore.edit { it[KEY_ACCESS_TOKEN] = token }
    }

    suspend fun updateUserInfo(fullName: String, email: String, bloodGroup: String, district: String) {
        context.dataStore.edit {
            it[KEY_FULL_NAME]   = fullName
            it[KEY_EMAIL]       = email
            it[KEY_BLOOD_GROUP] = bloodGroup
            it[KEY_DISTRICT]    = district
        }
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun saveProfileImageUri(uri: String) {
        context.dataStore.edit { it[KEY_PROFILE_IMAGE] = uri }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
