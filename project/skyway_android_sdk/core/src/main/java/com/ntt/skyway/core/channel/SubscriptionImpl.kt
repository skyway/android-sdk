/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.core.content.remote.RemoteStream
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionImpl internal constructor(
    override val channel: Channel,
    override val id: String,
    override val subscriber: Member,
    override val publication: Publication,
    override val contentType: ContentType,
    override val nativePointer: Long,
    internal var internalStream: RemoteStream?,
    private val repository: Repository
) : Subscription {
    override val state: Subscription.State
        get() = Subscription.State.fromString(nativeState(nativePointer))

    override val preferredEncodingId: String
        get() = nativePreferredEncodingId(nativePointer)

    override val stream: RemoteStream?
        get() = internalStream

    override var onCanceledHandler: (() -> Unit)? = null

    override var onConnectionStateChangedHandler: ((state: String) -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        nativeAddEventListener(channel.id, nativePointer)
    }

    override fun changePreferredEncoding(id: String) {
        nativeChangePreferredEncoding(nativePointer, id)
    }

    override fun getStats(): WebRTCStats? {
        nativeGetStats(nativePointer)?.let {
            return Factory.createWebRTCStats(it)
        }
        return null
    }

    override suspend fun cancel(): Boolean = withContext(Dispatchers.Default) {
        return@withContext nativeCancel(nativePointer)
    }

    private fun onCanceled() {
        Logger.logI("🔔onCanceled")
        scope.launch {
            onCanceledHandler?.invoke()
        }
    }

    private fun onConnectionStateChanged(state: String) {
        Logger.logI("🔔onConnectionStateChanged: $state")
        scope.launch {
            onConnectionStateChangedHandler?.invoke(state)
        }
    }

    private external fun nativeAddEventListener(channelId: String, ptr: Long)
    private external fun nativeState(ptr: Long): String
    private external fun nativePreferredEncodingId(ptr: Long): String
    private external fun nativeCancel(ptr: Long): Boolean
    private external fun nativeEnable(ptr: Long): Boolean
    private external fun nativeDisable(ptr: Long): Boolean
    private external fun nativeChangePreferredEncoding(ptr: Long, id: String)
    private external fun nativeGetStats(ptr: Long): String?
}
