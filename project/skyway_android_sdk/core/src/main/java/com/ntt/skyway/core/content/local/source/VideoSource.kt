/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local.source

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.WebRTCManager
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.util.Logger
import org.webrtc.*

/**
 *  映像入力に関する操作を行う抽象クラス。
 */
abstract class VideoSource {
    protected var source: org.webrtc.VideoSource? = null
    protected var textureHelper: SurfaceTextureHelper? = null
    protected val yuvConverter = YuvConverter()
    private var videoTrack: VideoTrack? = null
    protected var isInitialized = false

    internal fun initialize() {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        WebRTCManager.videoSourceList.add(this)
        source = WebRTCManager.createRTCVideoSource()
        textureHelper = WebRTCManager.createSurfaceTextureHelper()
    }

    /**
     *  Publish可能な[com.ntt.skyway.core.content.Stream]を生成します。
     */
    fun createStream(): LocalVideoStream {
        videoTrack = WebRTCManager.createRTCVideoTrack(source!!)
        val streamJson = nativeCreateVideoStream(videoTrack!!)
        return Factory.createLocalVideoStream(streamJson, this, videoTrack!!)
    }

    internal fun dispose() {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        this.isInitialized = false
        try {
            source?.dispose()
            textureHelper?.dispose()
        } catch(e: IllegalStateException) {
            Logger.logI("Source is already disposed")
        }
    }

    private external fun nativeCreateVideoStream(track: VideoTrack): String
}
