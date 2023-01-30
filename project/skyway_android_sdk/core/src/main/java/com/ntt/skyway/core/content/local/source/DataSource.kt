/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local.source

import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.SkyWayContext

class DataSource {
    /**
     *  Publish可能な[com.ntt.skyway.core.content.Stream]を生成します。
     */
    fun createStream(): LocalDataStream {
        check(SkyWayContext.isSetup) {"Please setup SkyWayContext first"}
        val streamJson = nativeCreateDataStream()
        return Factory.createLocalDataStream(streamJson)
    }

    private external fun nativeCreateDataStream(): String
}
