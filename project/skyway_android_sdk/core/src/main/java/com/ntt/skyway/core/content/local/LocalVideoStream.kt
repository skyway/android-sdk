/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.local.source.VideoSource
import com.ntt.skyway.core.content.sink.Renderer
import com.ntt.skyway.core.util.Logger
import org.webrtc.VideoTrack

class LocalVideoStream internal constructor(
    private var source: VideoSource,
    private val track: VideoTrack,
    dto: Dto
) : LocalStream(dto) {
    private var renderers: ArrayList<Renderer> = arrayListOf()
    override val contentType = ContentType.VIDEO

    /**
     *  [Renderer]を追加します。
     *  追加されたRendererには映像が出力されます。
     */
    fun addRenderer(renderer: Renderer) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        track.addSink(renderer.sink)
        renderers.add(renderer)
    }

    /**
     *  指定した[Renderer]を取り除きます。
     */
    fun removeRenderer(renderer: Renderer) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        track.removeSink(renderer.sink)
        renderers.remove(renderer)
    }

    /**
     *  全ての[Renderer]を取り除きます。
     */
    fun removeAllRenderer() {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        for (renderer in renderers) {
            track.removeSink(renderer.sink)
        }
        renderers.clear()
    }

    override fun dispose() {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        removeAllRenderer()
        track.dispose()
    }
}
