package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.api.ApiService
import com.lifeplus.healthcare.data.model.AppSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _settings = MutableStateFlow<List<AppSetting>>(emptyList())
    val settings: StateFlow<List<AppSetting>> = _settings.asStateFlow()

    init {
        loadSettings()
        startPeriodicRefresh()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val response = api.getSettings()
                if (response.isSuccessful) {
                    _settings.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently handle error — use cached values
            }
        }
    }

    /** Refresh settings every 5 minutes so admin changes apply without app restart. */
    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L) // 5 minutes
                loadSettings()
            }
        }
    }

    // Helper to get a boolean feature flag (non-reactive — use settings StateFlow for reactive UI)
    fun isFeatureEnabled(key: String, default: Boolean = true): Boolean {
        val setting = _settings.value.find { it.settingKey == key }
        return setting?.settingValue?.toBooleanStrictOrNull() ?: default
    }

    // Helper to get string setting
    fun getStringSetting(key: String, default: String = ""): String {
        return _settings.value.find { it.settingKey == key }?.settingValue ?: default
    }
}
