package com.lastwave.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalAudioTrackDao {
    @Query("SELECT * FROM local_audio_tracks ORDER BY artist COLLATE NOCASE, album COLLATE NOCASE, title COLLATE NOCASE")
    fun observeAll(): Flow<List<LocalAudioTrackEntity>>

    @Query("SELECT * FROM local_audio_tracks ORDER BY artist COLLATE NOCASE, album COLLATE NOCASE, title COLLATE NOCASE")
    suspend fun getAll(): List<LocalAudioTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<LocalAudioTrackEntity>)

    @Query("DELETE FROM local_audio_tracks")
    suspend fun clear()
}
