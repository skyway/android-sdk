/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger

class LocalDataStream internal constructor(dto: Dto) : LocalStream(dto) {
    override val contentType = ContentType.DATA

    /**
     *  文字列を書き込みます。
     *
     *  @param data 書き込む文字列。
     */
    fun write(data: String) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        nativeWrite(data, nativePointer)
    }

    /**
     *  データを書き込みます。
     *
     *  @param data 書き込む文字列。
     */
    fun write(data: ByteArray) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        nativeWriteByteArray(data, nativePointer)
    }

    override fun dispose() {

    }

    private external fun nativeWrite(data: String, ptr: Long)
    private external fun nativeWriteByteArray(data: ByteArray, ptr: Long)
}
