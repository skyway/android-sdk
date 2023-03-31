/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * LocalPersonの操作を行うクラス。
 */
class LocalPerson internal constructor(dto: Dto) : Member(dto) {
    override val type = Type.PERSON
    override val subType = "person"

    /**
     *  常に[Member.Side.LOCAL]を返します。
     */
    override val side = Side.LOCAL

    /**
     * このLocalPersonがpublishした時に発火するハンドラ。
     */
    var onStreamPublishedHandler: ((publication: Publication) -> Unit)? = null

    /**
     * このLocalPersonがunpublishした時に発火するハンドラ。
     */
    var onStreamUnpublishedHandler: ((publication: Publication) -> Unit)? = null

    /**
     * このLocalPersonがsubscribeした時に発火するハンドラ。
     */
    var onPublicationSubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    /**
     * このLocalPersonがunsubscribeした時に発火するハンドラ。
     */
    var onPublicationUnsubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    private companion object {
        const val PUBLISH_TIMEOUT_SEC = 10000L
        const val SUBSCRIBE_TIMEOUT_SEC = 10000L
    }

    internal val publishMutex = Mutex()
    internal val subscribeMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        addEventListener()
    }

    /**
     *  Streamをpublishします。
     */
    suspend fun publish(
        localStream: LocalStream,
        options: Publication.Options? = null
    ): Publication? =
        withContext(Dispatchers.IO) {
            publishMutex.withLock {
                val optionsJson = options?.toJson() ?: "{}"
                val publicationJson =
                    nativePublish(nativePointer, localStream.nativePointer, optionsJson)
                        ?: return@withContext null
                return@withContext channel.addLocalPublication(publicationJson, localStream)
            }
        }

    /**
     *  Publicationをunpublishします。
     */
    suspend fun unpublish(publicationId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeUnpublish(nativePointer, publicationId)
    }

    suspend fun unpublish(publication: Publication): Boolean {
        return unpublish(publication.id)
    }

    /**
     *  Publicationをsubscribeします。
     */
    suspend fun subscribe(
        publicationId: String,
        options: Subscription.Options? = null
    ): Subscription? = withContext(Dispatchers.IO) {
        subscribeMutex.withLock {
            val optionsJson = options?.toJson() ?: "{}"
            val subscriptionJson =
                nativeSubscribe(nativePointer, publicationId, optionsJson)
                    ?: return@withContext null
            return@withContext channel.addLocalSubscription(subscriptionJson)
        }
    }

    suspend fun subscribe(
        publication: Publication,
        options: Subscription.Options? = null
    ): Subscription? {
        return subscribe(publication.id, options)
    }

    /**
     *  Publicationをunsubscribeします。
     */
    suspend fun unsubscribe(subscriptionId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeUnsubscribe(nativePointer, subscriptionId)
    }

    suspend fun unsubscribe(subscription: Subscription): Boolean {
        return unsubscribe(subscription.id)
    }

    override fun addEventListener() {
        nativeAddEventListener(channel.id, nativePointer)
    }

    private fun onStreamPublished(publicationId: String) {
        scope.launch {
            publishMutex.withLock {
                val publication = channel.findPublication(publicationId) ?: run {
                    Logger.logW("onStreamPublished: The publication($publicationId) is not found")
                    return@launch
                }
                onStreamPublishedHandler?.invoke(publication)
            }
        }
    }

    private fun onStreamUnpublished(publicationId: String) {
        val publication = channel.findPublication(publicationId) ?: run {
            Logger.logW("onStreamUnpublished: The publication($publicationId) is not found")
            return
        }
        onStreamUnpublishedHandler?.invoke(publication)
    }

    private fun onPublicationSubscribed(subscriptionId: String) {
        scope.launch {
            subscribeMutex.withLock {
                val subscription = channel.findSubscription(subscriptionId) ?: run {
                    Logger.logW("onPublicationSubscribed: The subscription($subscriptionId) is not found")
                    return@launch
                }
                onPublicationSubscribedHandler?.invoke(subscription)
            }
        }
    }

    private fun onPublicationUnsubscribed(subscriptionId: String) {
        val subscription = channel.findSubscription(subscriptionId) ?: run {
            Logger.logW("onPublicationUnsubscribed: The subscription($subscriptionId) is not found")
            return
        }
        onPublicationUnsubscribedHandler?.invoke(subscription)
    }

    // Redefined for the following reasons
    // Member events cannot be called in the bridge layer of lower versions of Android.
    private fun onLeft() {
        onLeftHandler?.invoke()
    }

    // Redefined for the following reasons
    // Member events cannot be called in the bridge layer of lower versions of Android.
    private fun onMetadataUpdated(metadata: String) {
        onMetadataUpdatedHandler?.invoke(metadata)
    }

    // Redefined for the following reasons
    // Member events cannot be called in the bridge layer of lower versions of Android.
    private fun onPublicationListChanged() {
        onPublicationListChangedHandler?.invoke();
    }

    // Redefined for the following reasons
    // Member events cannot be called in the bridge layer of lower versions of Android.
    private fun onSubscriptionListChanged() {
        onSubscriptionListChangedHandler?.invoke();
    }

    private external fun nativeAddEventListener(channelId: String, ptr: Long)
    private external fun nativePublish(ptr: Long, localStreamPtr: Long, options: String): String?
    private external fun nativeUnpublish(ptr: Long, publicationId: String): Boolean
    private external fun nativeSubscribe(ptr: Long, publicationId: String, options: String): String?
    private external fun nativeUnsubscribe(ptr: Long, subscriptionId: String): Boolean
}
