/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.remote

class RemoteDataStream internal constructor(dto: Dto) : RemoteStream(dto) {
    override val contentType = ContentType.DATA

    /**
     *  文字列データを受信した際に発火するハンドラ。
     */
    var onDataHandler: ((data: String) -> Unit)? = null

    /**
     *  バイナリデータを受信した際に発火するハンドラ。
     */
    var onDataBufferHandler: ((data: ByteArray) -> Unit)? = null
    private var remoteDataStreamGlobalRef: Long

    init {
        remoteDataStreamGlobalRef = nativeAddListener(nativePointer)
    }

    private fun onData(data: String) {
        onDataHandler?.invoke(data)
    }

    private fun OnDataBuffer(data: ByteArray) {
        onDataBufferHandler?.invoke(data)
    }

    override fun dispose() {
        onDataHandler = null
        onDataBufferHandler = null
        nativeDispose(remoteDataStreamGlobalRef)
    }

    private external fun nativeAddListener(ptr: Long): Long
    private external fun nativeDispose(ptr: Long)
}
