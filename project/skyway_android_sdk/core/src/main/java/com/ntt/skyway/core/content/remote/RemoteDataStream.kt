/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        remoteDataStreamGlobalRef = nativeAddListener(nativePointer)
    }

    private fun onData(data: String) {
        scope.launch {
            onDataHandler?.invoke(data)
        }
    }

    private fun OnDataBuffer(data: ByteArray) {
        scope.launch {
            onDataBufferHandler?.invoke(data)
        }
    }

    override fun dispose() {
        onDataHandler = null
        onDataBufferHandler = null
        nativeDispose(remoteDataStreamGlobalRef)
    }

    private external fun nativeAddListener(ptr: Long): Long
    private external fun nativeDispose(ptr: Long)
}
