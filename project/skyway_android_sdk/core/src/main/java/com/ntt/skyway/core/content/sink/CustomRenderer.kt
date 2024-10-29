/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.sink

import com.ntt.skyway.core.SkyWayOptIn
import org.webrtc.VideoSink
import java.nio.ByteBuffer

/**
 *  フレームデータとして映像出力に関する操作を行うクラス。
 */
@SkyWayOptIn
class CustomRenderer : Renderer {
    data class VideoFrameBuffer(
        val width: Int,
        val height: Int,
        val dataY: ByteBuffer,
        val dataU: ByteBuffer,
        val dataV: ByteBuffer,
        val strideY: Int,
        val strideU: Int,
        val strideV: Int
    )

    override var isSetup: Boolean = true

    /**
     *  @suppress
     */
    override val sink = VideoSink { videoFrame ->
        videoFrame?.buffer?.toI420()?.apply {
            val buffer = VideoFrameBuffer(
                width,
                height,
                dataY,
                dataU,
                dataV,
                strideY,
                strideU,
                strideV
            )
            onFrameHandler?.invoke(buffer)
        }
    }

    override fun dispose() {
        onFrameHandler = null
    }

    /**
     *  映像フレームを取得した際に発火するハンドラ。
     */
    var onFrameHandler: ((buffer: VideoFrameBuffer) -> Unit)? = null
}
