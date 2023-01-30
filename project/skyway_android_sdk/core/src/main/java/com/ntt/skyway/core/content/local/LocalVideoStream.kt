/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local

import com.ntt.skyway.core.content.local.source.VideoSource
import com.ntt.skyway.core.content.sink.Renderer
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
        check(renderer.isSetup) { "Please setup Renderer first" }
        track.addSink(renderer.sink)
        renderers.add(renderer)
    }

    /**
     *  指定した[Renderer]を取り除きます。
     */
    fun removeRenderer(renderer: Renderer) {
        track.removeSink(renderer.sink)
        renderers.remove(renderer)
    }

    /**
     *  全ての[Renderer]を取り除きます。
     */
    fun removeAllRenderer() {
        for (renderer in renderers) {
            removeRenderer(renderer)
        }
    }

    override fun dispose() {
        removeAllRenderer()
        track.dispose()
    }
}
