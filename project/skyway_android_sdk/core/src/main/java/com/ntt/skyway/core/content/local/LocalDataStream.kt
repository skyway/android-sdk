/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local

class LocalDataStream internal constructor(dto: Dto) : LocalStream(dto) {
    override val contentType = ContentType.DATA

    /**
     *  文字列を書き込みます。
     *
     *  @param data 書き込む文字列。
     */
    fun write(data: String) {
        nativeWrite(data, nativePointer)
    }

    /**
     *  データを書き込みます。
     *
     *  @param data 書き込む文字列。
     */
    fun write(data: ByteArray) {
        nativeWriteByteArray(data, nativePointer)
    }

    override fun dispose() {

    }

    private external fun nativeWrite(data: String, ptr: Long)
    private external fun nativeWriteByteArray(data: ByteArray, ptr: Long)
}
