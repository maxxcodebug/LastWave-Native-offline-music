// Copyright (C) 2026 Anshuman X (maxxcodebug)

package com.lastwave.app.data.network

import com.lastwave.app.data.local.SettingsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineModeGuard @Inject constructor(
    settingsPreferences: SettingsPreferences,
    applicationScope: CoroutineScope,
) {
    @Volatile
    var isOffline: Boolean = false
        private set

    init {
        applicationScope.launch {
            settingsPreferences.settings
                .map { it.offlineServiceEnabled }
                .distinctUntilChanged()
                .collect { isOffline = it }
        }
    }

    fun checkNetworkAllowed() {
        if (isOffline) throw IOException("LastWave Offline Mode is active")
    }
}
