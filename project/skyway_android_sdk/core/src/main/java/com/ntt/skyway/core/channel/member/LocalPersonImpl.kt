/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Repository
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  @suppress
 */
class LocalPersonImpl internal constructor(
    override val channel: Channel,
    override val id: String,
    override val name: String?,
    override val nativePointer: Long,
    private val repository: Repository
) : LocalPerson() {
    override val type = Member.Type.PERSON
    override val subType = "person"

    override val side = Member.Side.LOCAL

    override val metadata: String?
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return null
            }
            return nativeMetadata(nativePointer).takeUnless { it.isBlank() }
        }

    override val state: Member.State
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return Member.State.LEFT
            }
            return Member.State.fromString(nativeState(nativePointer))
        }

    override val publications
        get() = channel.publications.filter { it.publisher == this }

    override val subscriptions
        get() = channel.subscriptions.filter { it.subscriber == this }

    override var onLeftHandler: (() -> Unit)? = null

    override var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    override var onStreamPublishedHandler: ((publication: Publication) -> Unit)? = null

    override var onStreamUnpublishedHandler: ((publication: Publication) -> Unit)? = null

    override var onPublicationListChangedHandler: (() -> Unit)? = null

    override var onPublicationSubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    override var onPublicationUnsubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    override var onSubscriptionListChangedHandler: (() -> Unit)? = null

    private companion object {
        const val PUBLISH_TIMEOUT_SEC = 10000L
        const val SUBSCRIBE_TIMEOUT_SEC = 10000L
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    constructor(dto: Member.Dto, repository: Repository) : this(
        dto.channel, dto.id, dto.name, dto.nativePointer, repository
    )

    init {
        nativeAddEventListener(channel.id, nativePointer)
    }

    override suspend fun updateMetadata(metadata: String): Boolean =
        withContext(channel._threadContext) {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return@withContext false
            }
            return@withContext nativeUpdateMetadata(nativePointer, metadata)
        }

    override suspend fun leave(): Boolean = withContext(channel._threadContext) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext false
        }
        return@withContext nativeLeave(nativePointer)
    }

    override suspend fun publish(
        localStream: LocalStream, options: Publication.Options?
    ): Publication? = withContext(channel._threadContext) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext null
        }
        val optionsJson = options?.toJson() ?: "{}"
        val publicationJson =
            nativePublish(nativePointer, localStream.nativePointer, optionsJson)
                ?: return@withContext null
        return@withContext repository.addPublicationIfNeeded(publicationJson, localStream)
    }

    override suspend fun unpublish(publicationId: String): Boolean =
        withContext(channel._threadContext) {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return@withContext false
            }
            return@withContext nativeUnpublish(nativePointer, publicationId)
        }

    override suspend fun unpublish(publication: Publication): Boolean {
        return unpublish(publication.id)
    }

    override suspend fun subscribe(
        publicationId: String, options: Subscription.Options?
    ): Subscription? = withContext(channel._threadContext) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext null
        }

        val optionsJson = options?.toJson() ?: "{}"
        val subscriptionJson = nativeSubscribe(nativePointer, publicationId, optionsJson)
            ?: return@withContext null
        return@withContext repository.addSubscriptionIfNeeded(subscriptionJson)
    }

    override suspend fun subscribe(
        publication: Publication, options: Subscription.Options?
    ): Subscription? {
        return subscribe(publication.id, options)
    }

    override suspend fun unsubscribe(subscriptionId: String): Boolean =
        withContext(channel._threadContext) {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return@withContext false
            }
            return@withContext nativeUnsubscribe(nativePointer, subscriptionId)
        }

    override suspend fun unsubscribe(subscription: Subscription): Boolean {
        return unsubscribe(subscription.id)
    }

    private fun onLeft() {
        Logger.logI("🔔onLeft")
        scope.launch {
            onLeftHandler?.invoke()
        }
    }

    private fun onMetadataUpdated(metadata: String) {
        Logger.logI("🔔onMetadataUpdated")
        scope.launch {
            onMetadataUpdatedHandler?.invoke(metadata)
        }
    }

    private fun onStreamPublished(publicationJson: String) {
        scope.launch {
            Logger.logI("🔔onStreamPublished")
            val publication = repository.addPublicationIfNeeded(publicationJson, null)
            onStreamPublishedHandler?.invoke(publication)
        }
    }

    private fun onStreamUnpublished(publicationId: String) {
        Logger.logI("🔔onStreamUnpublished")
        val publication = repository.findPublication(publicationId) ?: run {
            Logger.logW("onStreamUnpublished: The publication($publicationId) is not found")
            return
        }
        scope.launch {
            onStreamUnpublishedHandler?.invoke(publication)
        }
    }

    private fun onPublicationListChanged() {
        Logger.logI("🔔onPublicationListChanged")
        scope.launch {
            onPublicationListChangedHandler?.invoke();
        }
    }

    private fun onPublicationSubscribed(subscriptionJson: String) {
        scope.launch {
            Logger.logI("🔔onPublicationSubscribed")
            val subscription = repository.addSubscriptionIfNeeded(subscriptionJson)
            onPublicationSubscribedHandler?.invoke(subscription)
        }
    }

    private fun onPublicationUnsubscribed(subscriptionId: String) {
        Logger.logI("🔔onPublicationUnsubscribed")
        val subscription = repository.findSubscription(subscriptionId) ?: run {
            Logger.logW("onPublicationUnsubscribed: The subscription($subscriptionId) is not found")
            return
        }
        scope.launch {
            onPublicationUnsubscribedHandler?.invoke(subscription)
        }
    }

    private fun onSubscriptionListChanged() {
        Logger.logI("🔔onSubscriptionListChanged")
        scope.launch {
            onSubscriptionListChangedHandler?.invoke();
        }
    }

    private external fun nativeAddEventListener(channelId: String, ptr: Long)
    private external fun nativeMetadata(ptr: Long): String
    private external fun nativeState(ptr: Long): String
    private external fun nativeUpdateMetadata(ptr: Long, metadata: String): Boolean
    private external fun nativePublish(ptr: Long, localStreamPtr: Long, options: String): String?
    private external fun nativeUnpublish(ptr: Long, publicationId: String): Boolean
    private external fun nativeSubscribe(ptr: Long, publicationId: String, options: String): String?
    private external fun nativeUnsubscribe(ptr: Long, subscriptionId: String): Boolean
    private external fun nativeLeave(ptr: Long): Boolean
}
