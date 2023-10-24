/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local.source

import com.ntt.skyway.core.WebRTCManager
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.util.Logger
import org.webrtc.*

/**
 *  映像入力に関する操作を行う抽象クラス。
 */
abstract class VideoSource {
    protected lateinit var source: org.webrtc.VideoSource
    protected lateinit var textureHelper: SurfaceTextureHelper
    protected val yuvConverter = YuvConverter()
    private var videoTrack: VideoTrack? = null
    protected var isInitialized = false

    internal fun initialize() {
        WebRTCManager.videoSourceList.add(this)
        source = WebRTCManager.createRTCVideoSource()
        textureHelper = WebRTCManager.createSurfaceTextureHelper()
    }

    /**
     *  Publish可能な[com.ntt.skyway.core.content.Stream]を生成します。
     */
    fun createStream(): LocalVideoStream {
        videoTrack = WebRTCManager.createRTCVideoTrack(source)
        val streamJson = nativeCreateVideoStream(videoTrack!!)
        return Factory.createLocalVideoStream(streamJson, this, videoTrack!!)
    }

    internal fun dispose() {
        this.isInitialized = false
        try {
            source.dispose()
        } catch(e: IllegalStateException) {
            Logger.logI("Source is already disposed")
        }
        textureHelper.dispose()
    }

    private external fun nativeCreateVideoStream(track: VideoTrack): String
}
