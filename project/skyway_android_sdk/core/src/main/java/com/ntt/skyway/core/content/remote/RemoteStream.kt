/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.remote

import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.WebRTCStats

abstract class RemoteStream internal constructor(dto: Dto) : Stream(dto) {
    override val side = Side.REMOTE
}
