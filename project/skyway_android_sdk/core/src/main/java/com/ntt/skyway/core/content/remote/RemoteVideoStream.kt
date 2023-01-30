/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.remote

import com.ntt.skyway.core.content.sink.Renderer
import org.webrtc.VideoTrack

class RemoteVideoStream internal constructor(dto: Dto) : RemoteStream(dto) {
    override val contentType = ContentType.VIDEO

    private val track: VideoTrack
    private var renderers: ArrayList<Renderer> = arrayListOf()

    init {
        track = VideoTrack(nativeGetTrack(nativePointer))
    }

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

    private external fun nativeGetTrack(ptr: Long): Long
}
