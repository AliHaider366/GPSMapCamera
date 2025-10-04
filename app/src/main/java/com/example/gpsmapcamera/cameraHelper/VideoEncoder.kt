package com.example.gpsmapcamera.cameraHelper

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import android.view.View
import android.graphics.Canvas
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

class VideoEncoder(
    private val width: Int,
    private val height: Int,
    private val bitRate: Int,
    private val frameRate: Int,
    private val outputFile: File
) {
    private lateinit var codec: MediaCodec
    private lateinit var muxer: MediaMuxer
    private var trackIndex = -1
    private var muxerStarted = false
    private var recording = false

    lateinit var inputSurface: Surface
        private set

    private var drainThread: Thread? = null

    fun startRecording() {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        codec = MediaCodec.createEncoderByType(MIME_TYPE)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        inputSurface = codec.createInputSurface()
        codec.start()

        muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        recording = true
        drainThread = Thread { drainEncoder() }.apply { start() }
    }

    fun stopRecording() {
        if (!recording) return
        recording = false

        try {
            codec.signalEndOfInputStream()
        } catch (e: Exception) {
            Log.e(TAG, "Error signaling end of input stream", e)
        }

        drainThread?.join()
        drainThread = null

        try {
            codec.stop()
            codec.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping codec", e)
        }

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

    private fun drainEncoder() {
        val bufferInfo = MediaCodec.BufferInfo()

        while (recording) {
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)

            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) throw RuntimeException("format changed twice")
                    val newFormat = codec.outputFormat
                    trackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                }
                outputIndex >= 0 -> {
                    val encodedData: ByteBuffer? = codec.getOutputBuffer(outputIndex)
                    if (encodedData == null) throw RuntimeException("encoderOutputBuffer $outputIndex was null")

                    if (bufferInfo.size != 0) {
                        if (!muxerStarted) throw RuntimeException("muxer hasn't started")
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }

                    codec.releaseOutputBuffer(outputIndex, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                }
            }
        }
    }

    fun isRecording() = recording

    companion object {
        private const val TAG = "VideoEncoder"
        private const val MIME_TYPE = "video/avc"
    }
}
