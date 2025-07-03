package com.reactnativeimagesequenceencoder

import android.graphics.BitmapFactory
import android.media.*
import android.os.Environment
import android.view.Surface
import com.facebook.react.bridge.*
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer

class ImageSequenceEncoderModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "ImageSequenceEncoder"

  /**
   * options: {
   *   folder:  "/cache/chat_frames/",
   *   fps:     30,
   *   width:   1280,
   *   height:  720,
   *   output:  "/data/data/.../movie.mp4"
   * }
   */
  @ReactMethod
  fun encode(options: ReadableMap, promise: Promise) = CoroutineScope(Dispatchers.IO).launch {
    try {
      val params = Params(options)
      encodeInternal(params)
      promise.resolve(params.output)
    } catch (t: Throwable) {
      promise.reject("ENCODER_ERROR", t)
    }
  }

  /* ------------------------------------------------------------------ */

  private data class Params(
    val folder: String,
    val fps: Int,
    val width: Int,
    val height: Int,
    val output: String
  ) {
    constructor(map: ReadableMap) : this(
      folder = map.getString("folder")!!,
      fps    = map.getInt("fps"),
      width  = map.getInt("width"),
      height = map.getInt("height"),
      output = map.getString("output")!!
    )
  }

  /* ------------------------------------------------------------------ */

  private fun encodeInternal(p: Params) {
    // Clean old file
    File(p.output).delete()

    /* ---------- MediaCodec setup ---------- */
    val format = MediaFormat.createVideoFormat("video/avc", p.width, p.height).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT,
        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
      setInteger(MediaFormat.KEY_BIT_RATE, 3_000_000)
      setInteger(MediaFormat.KEY_FRAME_RATE, p.fps)
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
    }

    val encoder = MediaCodec.createEncoderByType("video/avc")
    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val inputSurface: Surface = encoder.createInputSurface()
    encoder.start()

    val muxer = MediaMuxer(p.output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var trackIndex = -1
    var muxerStarted = false

    /* ---------- EGL wrapper draws Bitmaps → Surface ---------- */
    val egl = EglWrapper(inputSurface, p.width, p.height)

    var ptsUs = 0L
    val deltaUs = 1_000_000L / p.fps

    File(p.folder).listFiles()?.sortedBy { it.name }?.forEach { file ->
      val bmp = BitmapFactory.decodeFile(file.path)
      egl.drawBitmap(bmp)
      bmp.recycle()

      ptsUs += deltaUs
      egl.setPresentationTime(ptsUs * 1000)
      egl.swapBuffers()
    }
    egl.release()          // sends EOS via Surface end-of-stream

    /* ---------- Pull encoded data ---------- */
    val bufferInfo = MediaCodec.BufferInfo()
    var eos = false
    while (!eos) {
      val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
      when {
        outIndex >= 0 -> {
          val encoded: ByteBuffer = encoder.getOutputBuffer(outIndex)!!
          if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            bufferInfo.size = 0
          }
          if (bufferInfo.size > 0) {
            if (!muxerStarted) {
              trackIndex = muxer.addTrack(encoder.outputFormat)
              muxer.start()
              muxerStarted = true
            }
            encoded.position(bufferInfo.offset)
            encoded.limit(bufferInfo.offset + bufferInfo.size)
            muxer.writeSampleData(trackIndex, encoded, bufferInfo)
          }
          encoder.releaseOutputBuffer(outIndex, false)
          if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
            eos = true
          }
        }
        outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
          if (muxerStarted) throw RuntimeException("Format changed twice")
          trackIndex = muxer.addTrack(encoder.outputFormat)
          muxer.start()
          muxerStarted = true
        }
      }
    }

    encoder.stop(); encoder.release()
    muxer.stop(); muxer.release()
  }
}
