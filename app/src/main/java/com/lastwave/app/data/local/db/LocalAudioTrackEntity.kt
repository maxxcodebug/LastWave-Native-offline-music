package com.lastwave.app.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_audio_tracks",
    indices = [Index(value = ["uri"], unique = true)],
)
data class LocalAudioTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val mimeType: String? = null,
    val addedAtMillis: Long = System.currentTimeMillis(),
)
