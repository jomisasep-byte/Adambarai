package com.example.recognition

data class AudioSampleRequest(
  val scanId: String,
  val fileName: String,
  val segmentStartMs: Long,
  val segmentEndMs: Long,
  val mimeType: String?,
)

data class TrackCandidate(
  val title: String?,
  val artist: String?,
  val album: String?,
  val label: String?,
  val externalUrl: String?,
  val segmentStartMs: Long?,
  val segmentEndMs: Long?,
  val providerName: String,
)

sealed interface RecognitionResult {
  data class MatchFound(val candidates: List<TrackCandidate>) : RecognitionResult
  data class NoMatch(val coverageDescription: String) : RecognitionResult
  data class NotConfigured(val reason: String) : RecognitionResult
  data object InvalidToken : RecognitionResult
  data class RateLimited(val retryAfterSeconds: Int?) : RecognitionResult
  data object Timeout : RecognitionResult
  data class NetworkError(val message: String) : RecognitionResult
  data class UnsupportedMedia(val message: String) : RecognitionResult
  data class MalformedResponse(val message: String) : RecognitionResult
  data class ServerError(val code: Int, val message: String) : RecognitionResult
}

interface RecognitionProvider {
  val providerDisplayName: String
  suspend fun recognize(sample: AudioSampleRequest, backendProxyUrl: String): RecognitionResult
}

/**
 * Honest Tahap 1 adapter for AudD recognition.
 * Does NOT embed third-party secrets in the APK and does NOT fabricate API matches.
 * Real HTTPS proxy upload & JSON response parsing will be implemented in Tahap 4.
 */
class AuddRecognitionProvider : RecognitionProvider {
  override val providerDisplayName: String = "AudD (via Backend HTTPS Proxy)"

  override suspend fun recognize(
    sample: AudioSampleRequest,
    backendProxyUrl: String,
  ): RecognitionResult {
    if (backendProxyUrl.isBlank()) {
      return RecognitionResult.NotConfigured(
        "Backend HTTPS proxy dan token AudD belum dikonfigurasi. Token pihak ketiga tidak boleh ditanam di APK."
      )
    }
    return RecognitionResult.NotConfigured(
      "Integrasi jaringan pengenalan audio akan diaktifkan pada Tahap 4 setelah audit skema AudD & endpoint proxy HTTPS selesai."
    )
  }
}
