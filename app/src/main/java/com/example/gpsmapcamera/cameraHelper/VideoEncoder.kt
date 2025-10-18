package com.example.gpsmapcamera.cameraHelper

import android.Manifest
import android.media.*
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresPermission
import java.io.File
import java.nio.ByteBuffer

class VideoEncoder(
    private val width: Int,
    private val height: Int,
    private val bitRate: Int,
    private val frameRate: Int,
    private val outputFile: File
) {
    private lateinit var videoCodec: MediaCodec
    private lateinit var audioCodec: MediaCodec
    private lateinit var muxer: MediaMuxer
    private var videoTrackIndex = -1
    private var audioTrackIndex = -1
    private var muxerStarted = false
    private var recording = false
    private val muxerLock = Any()

    lateinit var inputSurface: Surface
        private set

    private var audioRecorder: AudioRecord? = null
    private var audioThread: Thread? = null
    private var videoDrainThread: Thread? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        try {
            // 1) Align width/height to macroblock (16) boundaries to avoid many OMX rejections
            val alignedWidth = (width + 15) / 16 * 16
            val alignedHeight = (height + 15) / 16 * 16
            Log.d(TAG, "Requested size ${width}x${height}, aligned to ${alignedWidth}x${alignedHeight}")

            // 2) Build format (we'll adjust color format & profile later if codec supports)
            val videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, alignedWidth, alignedHeight)
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            // Do NOT set color format or profile yet until we pick codec and inspect capabilities

            // 3) Choose codec: prefer Qualcomm hardware encoders, avoid HiSi (hisi)
            val chosenCodecName = chooseVideoEncoder(VIDEO_MIME_TYPE)
            Log.d(TAG, "Chosen video encoder: $chosenCodecName")

            videoCodec = if (chosenCodecName != null) {
                MediaCodec.createByCodecName(chosenCodecName)
            } else {
                // Fallback to system default encoder by type
                MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
            }

            // 4) Inspect codec capabilities and set color format/profile accordingly
            try {
                val codecInfo = videoCodec.codecInfo
                val caps = codecInfo.getCapabilitiesForType(VIDEO_MIME_TYPE)

                // Prefer COLOR_FormatSurface if advertised
                val supportedColorFormat = pickSuitableColorFormat(caps.colorFormats)
                Log.d(TAG, "Supported color format chosen: $supportedColorFormat")
                videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, supportedColorFormat)

                // If codec advertises profile levels, choose a safe profile/level (Baseline / Level 3) if available
                val chosenProfileLevel = pickSuitableProfileLevel(caps.profileLevels)
                chosenProfileLevel?.let {
                    videoFormat.setInteger(MediaFormat.KEY_PROFILE, it.profile)
                    videoFormat.setInteger(MediaFormat.KEY_LEVEL, it.level)
                    Log.d(TAG, "Set profile=${it.profile} level=${it.level}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not read codec capabilities: ${e.message}")
                // Leave format minimal (we already set bitrate/frame rate)
            }

            // 5) Configure codec
            videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            // 6) Create input surface and start
            inputSurface = videoCodec.createInputSurface()
            videoCodec.start()

            // ---- audio encoder setup (unchanged) ----
            val audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT)
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AUDIO_BUFFER_SIZE)

            audioCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE)
            audioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )
            audioCodec.start()
            audioRecorder?.startRecording()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            recording = true
            videoDrainThread = Thread { drainVideoEncoder() }.apply { start() }
            audioThread = Thread { drainAudioEncoder() }.apply { start() }

            Log.d(TAG, "Recording started (video:$chosenCodecName)")
        } catch (e: Exception) {
            Log.e(TAG, "startRecording failed: ${e.message}", e)
            // If configuration failed and we selected hardware codec, try fallback to a more compatible codec
            tryFallbackSoftEncoderOrNotify(e)
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun tryFallbackSoftEncoderOrNotify(originalError: Exception) {
        try {
            // Try fallback to Google's software encoder if present
            val softName = findCodecByNamePrefix("OMX.google") ?: findCodecByNamePrefix("c2.android")
            if (softName != null) {
                Log.w(TAG, "Falling back to software encoder: $softName")
                videoCodec = MediaCodec.createByCodecName(softName)

                val alignedWidth = (width + 15) / 16 * 16
                val alignedHeight = (height + 15) / 16 * 16
                val videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, alignedWidth, alignedHeight)
                videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

                videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                inputSurface = videoCodec.createInputSurface()
                videoCodec.start()

                // proceed with audio & muxer same as before
                val audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT)
                audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
                audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AUDIO_BUFFER_SIZE)

                audioCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE)
                audioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

                val minBufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                )
                audioRecorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize * 2
                )
                audioCodec.start()
                audioRecorder?.startRecording()

                muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

                recording = true
                videoDrainThread = Thread { drainVideoEncoder() }.apply { start() }
                audioThread = Thread { drainAudioEncoder() }.apply { start() }

                Log.d(TAG, "Recording started with fallback soft encoder")
                return
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Fallback soft encoder failed: ${ex.message}", ex)
        }

        // If we reach here, nothing helped; re-throw original or log error
        Log.e(TAG, "Unable to start video recording after fallback. Original error: ${originalError.message}", originalError)
        throw RuntimeException("VideoEncoder start failed: ${originalError.message}", originalError)
    }

    private fun pickSuitableColorFormat(colorFormats: IntArray): Int {
        // Prefer COLOR_FormatSurface; if not available, pick a common flexible format
        if (colorFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        }
        // Fallbacks: try flexible YUV formats
        val preferred = listOf(
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
        )
        for (pf in preferred) {
            if (colorFormats.contains(pf)) return pf
        }
        // If nothing matches, just return COLOR_FormatSurface (will likely fail but leave caller to handle)
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
    }

    private fun pickSuitableProfileLevel(profileLevels: Array<MediaCodecInfo.CodecProfileLevel>): MediaCodecInfo.CodecProfileLevel? {
        if (profileLevels.isEmpty()) return null
        // Prefer baseline profile and levels <= AVCLevel3
        val candidates = profileLevels.filter {
            it.profile == MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline ||
                    it.profile == MediaCodecInfo.CodecProfileLevel.AVCProfileMain ||
                    it.profile == MediaCodecInfo.CodecProfileLevel.AVCProfileHigh
        }.sortedBy { it.level }
        return candidates.firstOrNull()
    }

    private fun findCodecByNamePrefix(prefix: String): String? {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) continue
            if (info.name.startsWith(prefix, ignoreCase = true)) return info.name
        }
        return null
    }

    private fun chooseVideoEncoder(mime: String): String? {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val candidates = mutableListOf<MediaCodecInfo>()
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) continue
            try {
                val types = info.supportedTypes
                if (types.any { it.equals(mime, ignoreCase = true) }) {
                    candidates.add(info)
                }
            } catch (e: Exception) {
                // ignore codec that throws
            }
        }

        // Prefer Qualcomm hardware encoders (OMX.qcom / qcom / qualcomm) and avoid hisi
        val ordered = candidates.sortedWith(compareByDescending<MediaCodecInfo> { info ->
            val name = info.name.lowercase()
            when {
                name.contains("qcom") || name.contains("omx.qcom") || name.contains("qualcomm") -> 3
                name.contains("google") || name.contains("omx.google") -> 1
                name.contains("hisi") || name.contains("huawei") -> -10 // deprioritize hisi/huawei
                else -> 0
            }
        })

        // Pick first encoder that is not clearly HiSi
        for (info in ordered) {
            val name = info.name.lowercase()
            if (name.contains("hisi") || name.contains("huawei")) continue
            return info.name
        }

        // If nothing matches, return null to let system choose (createEncoderByType)
        return null
    }

    fun stopRecording() {
        if (!recording) return
        recording = false

        try {
            videoCodec.signalEndOfInputStream()
        } catch (e: Exception) {
            Log.e(TAG, "Error signaling end of video input stream", e)
        }

        try {
            audioRecorder?.stop()
            audioRecorder?.release()
            audioRecorder = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recorder", e)
        }

        videoDrainThread?.join()
        audioThread?.join()
        videoDrainThread = null
        audioThread = null

        try {
            videoCodec.stop()
            videoCodec.release()
            audioCodec.stop()
            audioCodec.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping codecs", e)
        }

        synchronized(muxerLock) {
            try {
                if (muxerStarted) muxer.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping muxer", e)
            }

            try {
                muxer.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing muxer", e)
            }
        }
    }

    private fun drainVideoEncoder() {
        val bufferInfo = MediaCodec.BufferInfo()

        while (recording) {
            val outputIndex = videoCodec.dequeueOutputBuffer(bufferInfo, 10_000)

            synchronized(muxerLock) {
                when {
                    outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                    outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        if (muxerStarted) throw RuntimeException("Video format changed twice")
                        val newFormat = videoCodec.outputFormat
                        videoTrackIndex = muxer.addTrack(newFormat)
                        if (audioTrackIndex != -1 && !muxerStarted) {
                            muxer.start()
                            muxerStarted = true
                            Log.d(TAG, "Muxer started after adding video track")
                        }
                    }
                    outputIndex >= 0 -> {
                        val encodedData: ByteBuffer? = videoCodec.getOutputBuffer(outputIndex)
                        if (encodedData == null) throw RuntimeException("Video encoderOutputBuffer $outputIndex was null")

                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }

                        videoCodec.releaseOutputBuffer(outputIndex, false)

                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return
                    }
                }
            }
        }
    }

    private fun drainAudioEncoder() {
        val bufferInfo = MediaCodec.BufferInfo()
        val audioBuffer = ByteArray(AUDIO_BUFFER_SIZE)

        while (recording) {
            val readSize = audioRecorder?.read(audioBuffer, 0, AUDIO_BUFFER_SIZE) ?: -1
            if (readSize > 0) {
                val inputBufferIndex = audioCodec.dequeueInputBuffer(10_000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = audioCodec.getInputBuffer(inputBufferIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(audioBuffer, 0, readSize)
                    audioCodec.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        readSize,
                        System.nanoTime() / 1000,
                        0
                    )
                }
            }

            synchronized(muxerLock) {
                val outputIndex = audioCodec.dequeueOutputBuffer(bufferInfo, 10_000)
                when {
                    outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                    outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        if (muxerStarted) throw RuntimeException("Audio format changed twice")
                        val newFormat = audioCodec.outputFormat
                        audioTrackIndex = muxer.addTrack(newFormat)
                        if (videoTrackIndex != -1 && !muxerStarted) {
                            muxer.start()
                            muxerStarted = true
                            Log.d(TAG, "Muxer started after adding audio track")
                        }
                    }
                    outputIndex >= 0 -> {
                        val encodedData = audioCodec.getOutputBuffer(outputIndex)
                        if (encodedData == null) throw RuntimeException("Audio encoderOutputBuffer $outputIndex was null")

                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(audioTrackIndex, encodedData, bufferInfo)
                        }

                        audioCodec.releaseOutputBuffer(outputIndex, false)
                    }
                }
            }
        }
    }

    fun isRecording() = recording

    companion object {
        private const val TAG = "VideoEncoder"
        private const val VIDEO_MIME_TYPE = "video/avc"
        private const val AUDIO_MIME_TYPE = "audio/mp4a-latm"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_COUNT = 1 // Mono
        private const val AUDIO_BIT_RATE = 128_000 // 128 kbps
        private const val AUDIO_BUFFER_SIZE = 1024 * 2 // 2KB buffer
    }
}
