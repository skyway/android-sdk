/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.remote

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import org.webrtc.AudioTrack

class RemoteAudioStream internal constructor(dto: Dto) : RemoteStream(dto) {
    override val contentType = ContentType.AUDIO

    private val track: AudioTrack

    init {
        track = AudioTrack(nativeGetTrack(nativePointer))
    }

    /**
     *  音量を変更します。
     *
     *  @param volume 音量（0 ~ 10）。
     */
    fun setVolume(volume: Double) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        track.setVolume(volume)
    }

    override fun dispose() {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        track.dispose()
    }

    private external fun nativeGetTrack(ptr: Long): Long
}
