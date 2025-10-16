package com.example.gpsmapcamera.cameraHelper

import android.Manifest
import android.media.*
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresPermission
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

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
    private val muxerLock = Any() // Synchronization lock for muxer state

    lateinit var inputSurface: Surface
        private set

    private var audioRecorder: AudioRecord? = null
    private var audioThread: Thread? = null
    private var videoDrainThread: Thread? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        // Configure video encoder

        val alignedWidth = (width + 15) / 16 * 16
        val alignedHeight = (height + 15) / 16 * 16

        val videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, alignedWidth, alignedHeight)
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
        videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3)

        /*val videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height)
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
*/
        videoCodec = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
        videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = videoCodec.createInputSurface()
        videoCodec.start()

        // Configure audio encoder
        val audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT)
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AUDIO_BUFFER_SIZE)

        audioCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE)
        audioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        // Initialize AudioRecord
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

        // Initialize muxer
        muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        recording = true
        videoDrainThread = Thread { drainVideoEncoder() }.apply { start() }
        audioThread = Thread { drainAudioEncoder() }.apply { start() }
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
            // Read audio data from AudioRecord
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

            // Drain audio encoder
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