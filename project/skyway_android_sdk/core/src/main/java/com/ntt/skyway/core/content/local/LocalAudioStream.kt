/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local

import com.ntt.skyway.core.content.local.source.AudioSource
import org.webrtc.AudioTrack


class LocalAudioStream internal constructor(private val source: AudioSource, val track: AudioTrack, dto: Dto) : LocalStream(dto) {
    override val contentType = ContentType.AUDIO

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    fun setVolume(volume: Double) {
        track.setVolume(volume)
    }

    override fun dispose() {
        track.dispose()
    }
}
