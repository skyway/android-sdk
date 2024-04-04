/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class RemoteMemberImpl internal constructor(
    final override val channel: Channel,
    override val id: String,
    override val name: String?,
    final override val nativePointer: Long
) : RemoteMember() {
    override val metadata: String?
        get() = nativeMetadata(nativePointer).takeUnless { it.isBlank() }

    override val state: Member.State
        get() = Member.State.fromString(nativeState(nativePointer))

    override val publications
        get() = channel.publications.filter { it.publisher == this }

    override val subscriptions
        get() = channel.subscriptions.filter { it.subscriber == this }

    override var onLeftHandler: (() -> Unit)? = null

    override var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    override var onPublicationListChangedHandler: (() -> Unit)? = null

    override var onSubscriptionListChangedHandler: (() -> Unit)? = null

    /**
     *  常に[Member.Side.REMOTE]を返します。
     */
    override val side = Member.Side.REMOTE

    protected val scope = CoroutineScope(Dispatchers.Default)

    constructor(dto: Member.Dto) : this(
        dto.channel,
        dto.id,
        dto.name,
        dto.nativePointer
    )

    init {
        nativeAddEventListener(channel.id, nativePointer)
    }

    override suspend fun updateMetadata(metadata: String): Boolean =
        withContext(Dispatchers.Default) {
            return@withContext nativeUpdateMetadata(nativePointer, metadata)
        }

    override suspend fun leave(): Boolean = withContext(Dispatchers.Default) {
        return@withContext nativeLeave(nativePointer)
    }

    // need override from inherited class for Android 7
    protected open fun onLeft() {
        Logger.logI("🔔onLeft")
        scope.launch {
            onLeftHandler?.invoke()
        }
    }

    protected open fun onMetadataUpdated(metadata: String) {
        Logger.logI("🔔onMetadataUpdated")
        scope.launch {
            onMetadataUpdatedHandler?.invoke(metadata)
        }
    }

    protected open fun onPublicationListChanged() {
        Logger.logI("🔔onPublicationListChanged")
        scope.launch {
            onPublicationListChangedHandler?.invoke()
        }
    }

    protected open fun onSubscriptionListChanged() {
        Logger.logI("🔔onSubscriptionListChanged")
        scope.launch {
            onSubscriptionListChangedHandler?.invoke()
        }
    }

    private external fun nativeAddEventListener(channelId: String, ptr: Long)
    private external fun nativeMetadata(ptr: Long): String
    private external fun nativeState(ptr: Long): String
    private external fun nativeUpdateMetadata(ptr: Long, metadata: String): Boolean
    private external fun nativeLeave(ptr: Long): Boolean
}
