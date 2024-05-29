/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.sink

import org.webrtc.VideoSink

interface Renderer {
    var isSetup: Boolean
    val sink: VideoSink?

    fun dispose()
}
