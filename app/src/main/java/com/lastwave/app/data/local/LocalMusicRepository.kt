package com.lastwave.app.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import androidx.annotation.WorkerThread
import com.lastwave.app.data.local.db.LocalAudioTrackDao
import com.lastwave.app.data.local.db.LocalAudioTrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import javax.inject.Inject
import javax.inject.Singleton

data class LocalMusicScanResult(
    val scanned: Int,
    val imported: Int,
)

@Singleton
class LocalMusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: LocalAudioTrackDao,
    private val settingsPreferences: SettingsPreferences,
) {
    val tracks: Flow<List<LocalAudioTrackEntity>> = dao.observeAll()

    suspend fun setMusicFolder(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        settingsPreferences.setOfflineMusicTreeUri(uri.toString())
    }

    suspend fun clearMusicFolder() {
        settingsPreferences.setOfflineMusicTreeUri(null)
        dao.clear()
    }

    suspend fun scanSelectedFolder(): LocalMusicScanResult = withContext(Dispatchers.IO) {
        val treeUri = settingsPreferences.settings.first().offlineMusicTreeUri
            ?: return@withContext LocalMusicScanResult(0, 0)
        val root = Uri.parse(treeUri)
        val rootDocumentId = DocumentsContract.getTreeDocumentId(root)
        val discovered = ArrayList<LocalAudioTrackEntity>()
        val pending = ArrayDeque<String>()
        pending.add(rootDocumentId)
        var scanned = 0

        while (pending.isNotEmpty()) {
            val parentDocumentId = pending.removeFirst()
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                root,
                parentDocumentId,
            )
            runCatching {
                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_SIZE,
                    ),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(idIndex)
                        val mime = cursor.getString(mimeIndex)
                        val name = cursor.getString(nameIndex).orEmpty()
                        val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else 0L
                        val child = DocumentsContract.buildDocumentUriUsingTree(root, id)
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            pending.add(id)
                            continue
                        }
                        if (!isAudio(name, mime)) continue
                        scanned++
                        readMetadata(child, name, mime, size)?.let(discovered::add)
                    }
                }
            }
        }

        dao.clear()
        if (discovered.isNotEmpty()) dao.insertAll(discovered)
        LocalMusicScanResult(scanned, discovered.size)
    }

    @WorkerThread
    private fun readMetadata(
        uri: Uri,
        displayName: String,
        mimeType: String?,
        size: Long,
    ): LocalAudioTrackEntity? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.trim().orEmpty().ifBlank { displayName.substringBeforeLast('.') }
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.trim().orEmpty().ifBlank { "Unknown artist" }
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.trim().orEmpty()
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            LocalAudioTrackEntity(
                uri = uri.toString(),
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                fileSizeBytes = size,
                mimeType = mimeType?.takeIf { it.isNotBlank() },
            )
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun isAudio(name: String, mimeType: String?): Boolean {
        if (mimeType?.startsWith("audio/") == true) return true
        return AUDIO_EXTENSIONS.any { name.endsWith(it, ignoreCase = true) }
    }

    private companion object {
        val AUDIO_EXTENSIONS = setOf(
            ".mp3", ".m4a", ".aac", ".flac", ".wav", ".ogg", ".opus",
            ".oga", ".amr", ".3gp", ".mka", ".webm", ".aiff", ".aif",
        )
    }
}
