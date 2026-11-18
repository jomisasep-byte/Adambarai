package com.example.media

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.MediaInspectionInfo
import com.example.data.model.MediaKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaInspector(private val context: Context) {

  suspend fun inspectUri(uri: Uri): MediaInspectionInfo =
    withContext(Dispatchers.IO) {
      val resolver = context.contentResolver
      var displayName = "media_terpilih"
      var fileSize: Long? = null

      val mimeType = runCatching { resolver.getType(uri) }.getOrNull()

      try {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIdx != -1) {
              displayName = cursor.getString(nameIdx) ?: displayName
            }
            if (sizeIdx != -1 && !cursor.isNull(sizeIdx)) {
              fileSize = cursor.getLong(sizeIdx)
            }
          }
        }
      } catch (e: SecurityException) {
        return@withContext MediaInspectionInfo(
          uriString = uri.toString(),
          fileName = displayName,
          mimeType = mimeType,
          fileSizeBytes = null,
          durationMs = null,
          mediaKind = inferMediaKind(mimeType, displayName),
          hasAudioTrack = null,
          audioCodecSummary = null,
          isReadable = false,
          validationError = "Akses ke file ditolak oleh sistem (SecurityException).",
        )
      } catch (e: Exception) {
        return@withContext MediaInspectionInfo(
          uriString = uri.toString(),
          fileName = displayName,
          mimeType = mimeType,
          fileSizeBytes = null,
          durationMs = null,
          mediaKind = inferMediaKind(mimeType, displayName),
          hasAudioTrack = null,
          audioCodecSummary = null,
          isReadable = false,
          validationError = "Gagal membaca metadata URI: ${e.localizedMessage ?: "error tidak diketahui"}.",
        )
      }

      val resolvedSize = fileSize
      if (resolvedSize != null && resolvedSize <= 0L) {
        return@withContext MediaInspectionInfo(
          uriString = uri.toString(),
          fileName = displayName,
          mimeType = mimeType,
          fileSizeBytes = resolvedSize,
          durationMs = null,
          mediaKind = inferMediaKind(mimeType, displayName),
          hasAudioTrack = false,
          audioCodecSummary = null,
          isReadable = false,
          validationError = "File kosong (0 byte).",
        )
      }

      // Verify stream is actually openable
      val canOpen =
        try {
          resolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        } catch (_: Exception) {
          false
        }
      if (!canOpen) {
        return@withContext MediaInspectionInfo(
          uriString = uri.toString(),
          fileName = displayName,
          mimeType = mimeType,
          fileSizeBytes = fileSize,
          durationMs = null,
          mediaKind = inferMediaKind(mimeType, displayName),
          hasAudioTrack = null,
          audioCodecSummary = null,
          isReadable = false,
          validationError = "URI tidak dapat dibuka untuk dibaca.",
        )
      }

      val mediaKind = inferMediaKind(mimeType, displayName)
      var durationMs: Long? = null
      var hasAudioFromRetriever: Boolean? = null

      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(context, uri)
        durationMs =
          retriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
        val hasAudioMeta =
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        if (hasAudioMeta != null) {
          hasAudioFromRetriever = hasAudioMeta.equals("yes", ignoreCase = true)
        }
      } catch (_: Exception) {
        // Fallback to MediaExtractor below
      } finally {
        runCatching { retriever.release() }
      }

      var hasAudioTrack = hasAudioFromRetriever
      var audioCodecSummary: String? = null
      var extractorError: String? = null

      val extractor = MediaExtractor()
      try {
        extractor.setDataSource(context, uri, null)
        val audioFormats = mutableListOf<String>()
        for (i in 0 until extractor.trackCount) {
          val format = extractor.getTrackFormat(i)
          val trackMime = format.getString(MediaFormat.KEY_MIME) ?: continue
          if (trackMime.startsWith("audio/")) {
            val sampleRate =
              if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                "${format.getInteger(MediaFormat.KEY_SAMPLE_RATE)} Hz"
              } else null
            val channels =
              if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                "${format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)} ch"
              } else null
            val details = listOfNotNull(trackMime, sampleRate, channels).joinToString(" • ")
            audioFormats.add(details)
            if (durationMs == null && format.containsKey(MediaFormat.KEY_DURATION)) {
              durationMs = format.getLong(MediaFormat.KEY_DURATION) / 1000L
            }
          }
        }
        hasAudioTrack = audioFormats.isNotEmpty()
        audioCodecSummary = audioFormats.firstOrNull()
      } catch (e: Exception) {
        extractorError = "Format media tidak didukung atau file rusak (${e.localizedMessage ?: "MediaExtractor"})."
      } finally {
        runCatching { extractor.release() }
      }

      val validationError =
        when {
          extractorError != null && hasAudioTrack != true -> extractorError
          hasAudioTrack == false -> "File media ini tidak memiliki track audio untuk dikenali."
          else -> null
        }

      MediaInspectionInfo(
        uriString = uri.toString(),
        fileName = displayName,
        mimeType = mimeType,
        fileSizeBytes = fileSize,
        durationMs = durationMs,
        mediaKind = mediaKind,
        hasAudioTrack = hasAudioTrack,
        audioCodecSummary = audioCodecSummary,
        isReadable = validationError == null,
        validationError = validationError,
      )
    }

  private fun inferMediaKind(mimeType: String?, fileName: String): MediaKind {
    if (mimeType != null) {
      if (mimeType.startsWith("video/")) return MediaKind.VIDEO
      if (mimeType.startsWith("audio/")) return MediaKind.AUDIO
    }
    val lower = fileName.lowercase()
    return when {
      lower.endsWith(".mp4") ||
        lower.endsWith(".mkv") ||
        lower.endsWith(".mov") ||
        lower.endsWith(".webm") -> MediaKind.VIDEO
      lower.endsWith(".mp3") ||
        lower.endsWith(".wav") ||
        lower.endsWith(".m4a") ||
        lower.endsWith(".aac") ||
        lower.endsWith(".ogg") ||
        lower.endsWith(".flac") -> MediaKind.AUDIO
      else -> MediaKind.UNKNOWN
    }
  }
}
