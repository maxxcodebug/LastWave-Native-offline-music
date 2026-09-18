package com.lastwave.app.ui.localmusic

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lastwave.app.data.local.LocalMusicRepository
import com.lastwave.app.data.local.SettingsPreferences
import com.lastwave.app.data.local.db.LocalAudioTrackEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OfflineMusicUiState(
    val tracks: List<LocalAudioTrackEntity> = emptyList(),
    val folderUri: String? = null,
    val offlineEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class OfflineMusicViewModel @Inject constructor(
    private val repository: LocalMusicRepository,
    settingsPreferences: SettingsPreferences,
) : ViewModel() {
    private val scanning = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            settingsPreferences.settings.first().offlineMusicTreeUri?.let {
                scan()
            }
        }
    }

    val uiState: StateFlow<OfflineMusicUiState> = combine(
        repository.tracks,
        settingsPreferences.settings,
        scanning,
        message,
    ) { tracks, settings, isScanning, message ->
        OfflineMusicUiState(
            tracks = tracks,
            folderUri = settings.offlineMusicTreeUri,
            offlineEnabled = settings.offlineServiceEnabled,
            isScanning = isScanning,
            message = message,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        OfflineMusicUiState(),
    )

    fun setFolder(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                repository.setMusicFolder(uri)
                scan()
            }.onFailure {
                message.value = "Couldn't access that folder"
            }
        }
    }

    fun scan() {
        if (scanning.value) return
        viewModelScope.launch {
            scanning.value = true
            message.value = null
            runCatching { repository.scanSelectedFolder() }
                .onSuccess {
                    message.value = "${it.imported} local song(s) found"
                }
                .onFailure {
                    message.value = "Couldn't scan the selected folder"
                }
            scanning.value = false
        }
    }

    fun clearFolder() {
        viewModelScope.launch {
            runCatching { repository.clearMusicFolder() }
                .onFailure { message.value = "Couldn't clear the music folder" }
        }
    }

    fun clearMessage() {
        message.value = null
    }
}
