package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object SampleVideoGenerator {
    private const val TAG = "SampleVideoGenerator"
    private const val MIME_TYPE = "video/avc"
    private const val WIDTH = 640
    private const val HEIGHT = 360
    private const val BIT_RATE = 1_000_000
    private const val FRAME_RATE = 30
    private const val I_FRAME_INTERVAL = 1
    private const val DURATION_SECONDS = 10

    suspend fun getOrCreateSampleVideo(context: Context): Uri = withContext(Dispatchers.IO) {
        val targetFile = File(context.cacheDir, "tmpro_sample_demo.mp4")
        if (targetFile.exists() && targetFile.length() > 5000) {
            return@withContext Uri.fromFile(targetFile)
        }

        try {
            generateSyntheticVideo(targetFile)
            if (targetFile.exists() && targetFile.length() > 5000) {
                return@withContext Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Synthetic video generation failed, fallback to url or placeholder", e)
        }

        // Return direct URI or fallback
        Uri.fromFile(targetFile)
    }

    private fun generateSyntheticVideo(outputFile: File) {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, WIDTH, HEIGHT).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }

        val encoder = MediaCodec.createEncoderByType(MIME_TYPE)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val totalFrames = FRAME_RATE * DURATION_SECONDS

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val paintSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A0AEC0")
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        val paintBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8B5CF6")
            style = Paint.Style.FILL
        }

        val colors = intArrayOf(
            Color.parseColor("#0F172A"),
            Color.parseColor("#1E1B4B"),
            Color.parseColor("#172554"),
            Color.parseColor("#042F2E"),
            Color.parseColor("#18181B")
        )

        for (frame in 0 until totalFrames) {
            val canvas: Canvas = inputSurface.lockCanvas(null)
            try {
                // Background cycle
                val colorIdx = (frame / (FRAME_RATE * 2)) % colors.size
                canvas.drawColor(colors[colorIdx])

                // Animated bar
                val progress = frame.toFloat() / totalFrames
                val barWidth = WIDTH * progress
                val barPaint = Paint().apply {
                    color = Color.parseColor("#06B6D4")
                }
                canvas.drawRect(0f, HEIGHT - 10f, barWidth, HEIGHT.toFloat(), barPaint)

                // TM PRO Badge
                val badgeRect = RectF(WIDTH / 2f - 90f, 60f, WIDTH / 2f + 90f, 100f)
                canvas.drawRoundRect(badgeRect, 20f, 20f, paintBadge)

                val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("TM PRO VIDEO", WIDTH / 2f, 88f, badgeTextPaint)

                // Timecode
                val currentSec = frame / FRAME_RATE
                val currentFrameInSec = frame % FRAME_RATE
                val timecode = String.format("00:%02d.%02d", currentSec, (currentFrameInSec * 100) / FRAME_RATE)
                canvas.drawText("Sample Edit Clip", WIDTH / 2f, 170f, paintText)
                canvas.drawText(timecode, WIDTH / 2f, 220f, paintText)
                canvas.drawText("Timeline & Trimming Ready", WIDTH / 2f, 270f, paintSub)
            } finally {
                inputSurface.unlockCanvasAndPost(canvas)
            }

            // Drain encoder
            drainEncoder(encoder, muxer, bufferInfo) { index ->
                trackIndex = index
                muxerStarted = true
            }
        }

        // Send EOS
        encoder.signalEndOfInputStream()
        var eos = false
        while (!eos) {
            val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!muxerStarted) {
                    trackIndex = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
            } else if (status >= 0) {
                val encodedData = encoder.getOutputBuffer(status)
                if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    eos = true
                }
                encoder.releaseOutputBuffer(status, false)
            }
        }

        try {
            encoder.stop()
            encoder.release()
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up encoder/muxer", e)
        }
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        onMuxerStart: (Int) -> Unit
    ) {
        while (true) {
            val status = encoder.dequeueOutputBuffer(bufferInfo, 2500)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val trackIndex = muxer.addTrack(encoder.outputFormat)
                muxer.start()
                onMuxerStart(trackIndex)
            } else if (status >= 0) {
                val encodedData = encoder.getOutputBuffer(status)
                if (encodedData != null && bufferInfo.size > 0) {
                    try {
                        muxer.writeSampleData(0, encodedData, bufferInfo)
                    } catch (e: Exception) {
                        // ignore if muxer not yet ready
                    }
                }
                encoder.releaseOutputBuffer(status, false)
            }
        }
    }
}
