/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.sink

import android.media.AudioTrack
import java.nio.ByteBuffer

object AudioDestination {
    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    var onAudioBufferHandler: ((buffer: ByteBuffer) -> Unit)? = null

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    val audioTrack
        get() = null

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    fun changeTrack(audioTrack: AudioTrack) { }
}
