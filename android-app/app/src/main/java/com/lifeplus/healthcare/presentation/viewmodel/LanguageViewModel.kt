package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.local.SessionDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val session: SessionDataStore
) : ViewModel() {

    val currentLanguage: StateFlow<String> = session.language.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "en"
    )

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            session.saveLanguage(lang)
        }
    }
}
