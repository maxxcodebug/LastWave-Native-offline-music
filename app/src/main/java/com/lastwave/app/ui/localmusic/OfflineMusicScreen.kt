package com.lastwave.app.ui.localmusic

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lastwave.app.data.local.db.LocalAudioTrackEntity
import com.lastwave.app.playback.PlayableTrack
import com.lastwave.app.ui.player.LocalMusicPlayer
import com.lastwave.app.ui.theme.ExpressivePillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMusicScreen(
    onBack: () -> Unit = {},
    viewModel: OfflineMusicViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val musicPlayer = LocalMusicPlayer.current

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.setFolder(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Music") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { folderPicker.launch(null) },
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "Choose music folder")
                    }
                    IconButton(
                        onClick = viewModel::scan,
                        enabled = state.folderUri != null && !state.isScanning,
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Scan")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    shape = ExpressivePillShape,
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Storage,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Local music service", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (state.offlineEnabled) "Offline service enabled" else "Offline service is disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            state.folderUri?.let { "Selected folder: ${Uri.parse(it).path ?: it}" }
                                ?: "Choose a folder from internal storage or an SD card.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (state.isScanning) "Scanning music…" else "Tap the folder button to select or change your music location.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        state.message?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (state.tracks.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Filled.AudioFile, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No local songs found", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Choose a music folder and scan it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "${state.tracks.size} songs",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    )
                }
                items(state.tracks, key = { it.uri }) { track ->
                    LocalTrackRow(track) {
                        musicPlayer.play(
                            PlayableTrack(
                                title = track.title,
                                artist = track.artist,
                                album = track.album.ifBlank { null },
                                playbackUrl = track.uri,
                                playbackMimeType = track.mimeType,
                            ),
                            sourceLabel = "Offline Music",
                            startRadio = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalTrackRow(track: LocalAudioTrackEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.AudioFile, contentDescription = null, modifier = Modifier.size(34.dp))
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (track.album.isBlank()) track.artist else "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (track.durationMs > 0) {
                Text(
                    formatDuration(track.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
