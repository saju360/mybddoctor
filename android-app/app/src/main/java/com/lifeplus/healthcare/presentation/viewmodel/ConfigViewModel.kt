package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.api.ApiService
import com.lifeplus.healthcare.data.model.AppSetting
import dagger.hilt.android.lifecycle.HiltViewModel
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
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val response = api.getSettings()
                if (response.isSuccessful) {
                    _settings.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently handle error or use default fallback settings
            }
        }
    }

    // Helper to get a boolean feature flag
    fun isFeatureEnabled(key: String, default: Boolean = true): Boolean {
        val setting = _settings.value.find { it.settingKey == key }
        return setting?.settingValue?.toBooleanStrictOrNull() ?: default
    }

    // Helper to get string setting
    fun getStringSetting(key: String, default: String = ""): String {
        return _settings.value.find { it.settingKey == key }?.settingValue ?: default
    }
}
