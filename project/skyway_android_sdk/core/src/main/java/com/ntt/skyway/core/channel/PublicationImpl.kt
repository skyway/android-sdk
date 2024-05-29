/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.core.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicationImpl internal constructor(
    override val channel: Channel,
    override val id: String,
    override val publisher: Member,
    override val contentType: ContentType,
    private val originId: String,
    override val codecCapabilities: List<Codec>,
    override val nativePointer: Long,
    private var internalStream: LocalStream?,
    private val repository: Repository
) : Publication {
    override val origin: Publication?
        get() = if (originId == "") null else repository.findPublication(originId)

    override val metadata: String
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return ""
            }
            return nativeMetadata(nativePointer)
        }

    override val state: Publication.State
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return Publication.State.CANCELED
            }
            return Publication.State.fromString(nativeState(nativePointer))
        }

    override val subscriptions
        get() = repository.availableSubscriptions.filter { it.publication == this }

    override val encodings: List<Encoding>
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return listOf()
            }
            val jsonArr = JsonParser.parseString(nativeEncodings(nativePointer)).asJsonArray
            return Encoding.fromJsonArray(jsonArr.asJsonArray)
        }

    override var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    override var onUnpublishedHandler: (() -> Unit)? = null

    override var onSubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    override var onUnsubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    override var onSubscriptionListChangedHandler: (() -> Unit)? = null

    override var onEnabledHandler: (() -> Unit)? = null

    override var onDisabledHandler: (() -> Unit)? = null

    override var onConnectionStateChangedHandler: ((state: String) -> Unit)? = null

    override val stream: LocalStream?
        get() = internalStream

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        nativeAddEventListener(channel.id, nativePointer)
    }

    override suspend fun updateMetadata(metadata: String): Boolean =
        withContext(Dispatchers.Default) {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return@withContext false
            }
            return@withContext nativeUpdateMetadata(nativePointer, metadata)
        }

    override suspend fun cancel(): Boolean = withContext(Dispatchers.Default) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext false
        }
        return@withContext nativeCancel(nativePointer)
    }

    override suspend fun enable(): Boolean = withContext(Dispatchers.Default) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext false
        }
        return@withContext nativeEnable(nativePointer)
    }

    override suspend fun disable(): Boolean = withContext(Dispatchers.Default) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext false
        }
        return@withContext nativeDisable(nativePointer)
    }

    override fun updateEncodings(encodings: List<Encoding>) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        val encodingsJson = Gson().toJson(encodings)
        nativeUpdateEncodings(nativePointer, encodingsJson)
    }

    override fun replaceStream(stream: LocalStream): Boolean {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return false
        }
        this.internalStream = stream
        return nativeReplaceStream(nativePointer, stream.nativePointer)
    }

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    override fun getStats(remoteMemberId: String): WebRTCStats? {
        nativeGetStats(remoteMemberId, nativePointer)?.let {
            return Factory.createWebRTCStats(it)
        }
        return null
    }

    private fun onMetadataUpdated(metadata: String) {
        Logger.logI("🔔onMetadataUpdated")
        scope.launch {
            onMetadataUpdatedHandler?.invoke(metadata)
        }
    }

    private fun onUnpublished() {
        Logger.logI("🔔onUnpublished")
        scope.launch {
            onUnpublishedHandler?.invoke()
        }
    }

    private fun onSubscribed(subscriptionJson: String) {
        Logger.logI("🔔onSubscribed")
        val subscription = repository.addSubscriptionIfNeeded(subscriptionJson)
        scope.launch {
            onSubscribedHandler?.invoke(subscription)
        }
    }

    private fun onUnsubscribed(subscriptionJson: String) {
        Logger.logI("🔔onUnsubscribed")
        val subscriptionId = Util.getObjectId(subscriptionJson)
        val subscription = repository.findSubscription(subscriptionId) ?: run {
            Logger.logW("onUnsubscribed: The subscription is not found")
            return
        }
        scope.launch {
            onUnsubscribedHandler?.invoke(subscription)
        }
    }

    private fun onSubscriptionListChanged() {
        Logger.logI("🔔onSubscriptionListChanged")
        scope.launch {
            onSubscriptionListChangedHandler?.invoke()
        }
    }

    private fun onEnabled() {
        Logger.logI("🔔onEnabled")
        scope.launch {
            onEnabledHandler?.invoke()
        }
    }

    private fun onDisabled() {
        Logger.logI("🔔onDisabled")
        scope.launch {
            onDisabledHandler?.invoke()
        }
    }

    private fun onConnectionStateChanged(state: String) {
        Logger.logI("🔔onConnectionStateChanged: $state")
        scope.launch {
            onConnectionStateChangedHandler?.invoke(state)
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    private external fun nativeAddEventListener(channelId: String, ptr: Long)
    private external fun nativeMetadata(ptr: Long): String
    private external fun nativeState(ptr: Long): String
    private external fun nativeEncodings(ptr: Long): String
    private external fun nativeUpdateMetadata(ptr: Long, metadata: String): Boolean
    private external fun nativeCancel(ptr: Long): Boolean
    private external fun nativeEnable(ptr: Long): Boolean
    private external fun nativeDisable(ptr: Long): Boolean
    private external fun nativeUpdateEncodings(ptr: Long, encodings: String)
    private external fun nativeReplaceStream(ptr: Long, localStreamPtr: Long): Boolean
    private external fun nativeGetStats(data: String, ptr: Long): String?
}
