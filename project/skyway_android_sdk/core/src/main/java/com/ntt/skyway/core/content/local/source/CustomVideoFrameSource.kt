/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local.source

import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.SystemClock
import org.webrtc.CapturerObserver
import org.webrtc.TextureBufferImpl
import org.webrtc.VideoFrame
import org.webrtc.VideoFrame.TextureBuffer.Type.RGB
import java.util.concurrent.TimeUnit

/**
 *  任意のフレームによる映像入力に関する操作を行うクラス。
 */
class CustomVideoFrameSource(width: Int, height: Int) : VideoSource() {
    private val observer: CapturerObserver
    private val textures = IntArray(1)
    private val buffer: TextureBufferImpl

    init {
        initialize()
        observer = source!!.capturerObserver
        buffer = TextureBufferImpl(
            width,
            height,
            RGB,
            textures[0],
            Matrix(),
            textureHelper?.handler,
            yuvConverter,
            null
        )
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
    }

    /**
     *  映像フレームを更新します。
     *
     *  @param bitmap 汎用的な[Bitmap]。
     *  @param rotation 画像の回転角度。
     */
    fun updateFrame(bitmap: Bitmap, rotation: Int) {
        textureHelper?.handler?.post {
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            val i420Buf = yuvConverter.convert(buffer)
            val captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime())
            val videoFrame = VideoFrame(i420Buf, rotation, captureTimeNs)
            observer.onFrameCaptured(videoFrame)
            videoFrame.release()
        }
    }
}
