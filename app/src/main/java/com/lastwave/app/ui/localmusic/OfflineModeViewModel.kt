// Copyright (C) 2026 Anshuman X (maxxcodebug)

package com.lastwave.app.ui.localmusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lastwave.app.data.local.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineModeViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = settingsPreferences.settings
        .map { it.offlineServiceEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setOfflineServiceEnabled(enabled)
        }
    }
}
