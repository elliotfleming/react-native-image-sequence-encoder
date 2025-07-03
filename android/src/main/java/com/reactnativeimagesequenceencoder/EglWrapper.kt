package com.reactnativeimagesequenceencoder

import android.graphics.Bitmap
import android.opengl.*
import android.view.Surface

/** Minimal EGL helper to draw Bitmaps onto an input Surface. */
internal class EglWrapper(
  private val surface: Surface,
  private val width: Int,
  private val height: Int
) {
  private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
  private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
  private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

  init {
    eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    EGL14.eglInitialize(eglDisplay, null, 0, null, 0)
    val attrib = intArrayOf(
      EGL14.EGL_RED_SIZE, 8,
      EGL14.EGL_GREEN_SIZE, 8,
      EGL14.EGL_BLUE_SIZE, 8,
      EGL14.EGL_ALPHA_SIZE, 8,
      EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
      EGL14.EGL_NONE
    )
    val configs = arrayOfNulls<EGLConfig>(1)
    val num = IntArray(1)
    EGL14.eglChooseConfig(eglDisplay, attrib, 0, configs, 0, 1, num, 0)
    val ctxAttrib = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
    eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttrib, 0)
    val surfAttrib = intArrayOf(EGL14.EGL_NONE)
    eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfAttrib, 0)
    EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
  }

  /** Draws the Bitmap into the current GL framebuffer (simple glTexSubImage2D). */
  fun drawBitmap(bmp: Bitmap) {
    val texIds = IntArray(1)
    GLES20.glGenTextures(1, texIds, 0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIds[0])
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    GLES20.glViewport(0, 0, width, height)

    // Simple fullscreen quad via OpenGL ES 2.0 fixed pipeline
    val triangleVertices = floatArrayOf(
      -1f, -1f,   0f, 1f,
       1f, -1f,   1f, 1f,
      -1f,  1f,   0f, 0f,
       1f,  1f,   1f, 0f
    )
    val vbo = IntArray(1)
    GLES20.glGenBuffers(1, vbo, 0)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
    GLES20.glBufferData(
      GLES20.GL_ARRAY_BUFFER,
      triangleVertices.size * 4,
      java.nio.ByteBuffer.allocateDirect(triangleVertices.size * 4)
        .order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(triangleVertices).position(0),
      GLES20.GL_STATIC_DRAW
    )
    // Use default fixed pipeline shaders via GLUtils.drawTexture() in API ≥ 28
    if (android.os.Build.VERSION.SDK_INT >= 28) {
      GLES20.glDisable(GLES20.GL_DEPTH_TEST)
      GLUtils.drawTexture(texIds[0], vbo[0], 2, 4)
    }
    GLES20.glDeleteTextures(1, texIds, 0)
    GLES20.glDeleteBuffers(1, vbo, 0)
  }

  fun setPresentationTime(nanos: Long) =
    EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nanos)

  fun swapBuffers() = EGL14.eglSwapBuffers(eglDisplay, eglSurface)

  fun release() {
    EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
    EGL14.eglDestroySurface(eglDisplay, eglSurface)
    EGL14.eglDestroyContext(eglDisplay, eglContext)
    EGL14.eglTerminate(eglDisplay)
    surface.release()
  }
}
