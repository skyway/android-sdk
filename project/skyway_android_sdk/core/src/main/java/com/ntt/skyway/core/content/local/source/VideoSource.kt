/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local.source

import com.ntt.skyway.core.WebRTCManager
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.local.LocalVideoStream
import org.webrtc.*

/**
 *  映像入力に関する操作を行う抽象クラス。
 */
abstract class VideoSource {
    protected val source = WebRTCManager.createRTCVideoSource()
    protected val textureHelper: SurfaceTextureHelper = WebRTCManager.createSurfaceTextureHelper()
    protected val yuvConverter = YuvConverter()

    /**
     *  Publish可能な[com.ntt.skyway.core.content.Stream]を生成します。
     */
    fun createStream(): LocalVideoStream {
        val track = WebRTCManager.createRTCVideoTrack(source)
        val streamJson = nativeCreateVideoStream(track)
        return Factory.createLocalVideoStream(streamJson, this, track)
    }

    private external fun nativeCreateVideoStream(track: VideoTrack): String
}
