/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room


import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.plugin.sfuBot.Forwarding
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RoomPublicationの操作を行うクラス。
 */
class RoomPublication internal constructor(
    /**
     * このRoomPublicationが所属する[Room]。
     */
    val room: Room,
    private val publication: Publication
) {
    /**
     *  Publish時の設定。
     */
    data class Options(
        /**
         *  Metadata。
         */
        val metadata: String? = null,
        /**
         *  コーデック一覧。
         */
        val codecCapabilities: List<Codec>? = null,
        /**
         *  エンコード設定一覧。
         */
        val encodings: List<Encoding>? = null,
        /**
         *  publish時に有効にするか。
         */
        val isEnabled: Boolean? = null,
        /**
         *  最大のsubscriber数。
         */
        val maxSubscribers: Int = 10
    ) {
        internal fun toCore(): Publication.Options {
            return Publication.Options(metadata, codecCapabilities, encodings, isEnabled)
        }

        internal fun toConfigure(): Forwarding.Configure {
            return Forwarding.Configure(maxSubscribers)
        }
    }

    /**
     * このRoomPublicationのID。
     */
    val id: String
        get() = publication.id

    /**
     * このRoomPublicationの[Stream.ContentType]。
     */
    val contentType: Stream.ContentType
        get() = publication.contentType

    /**
     *  このRoomPublicationのMetadata。
     */
    val metadata: String
        get() = if (origin == null) {
            publication.metadata
        } else {
            publication.origin!!.metadata
        }

    /**
     * このRoomPublicationのPublisher。
     */
    val publisher: RoomMember?
        get() = getPrivatePublisher()

    /**
     *  このRoomPublicationに対する[Subscription]の一覧。
     */
    val subscriptions
        get() = room.subscriptions.filter { it.publication?.id == this.id }

    /**
     * このRoomPublicationが所属する[Room]。
     */
    val codecCapabilities: List<Codec>
        get() = publication.codecCapabilities

    /**
     *  このRoomPublicationのエンコード設定。
     */
    val encodings: List<Encoding>
        get() = publication.encodings

    /**
     *  このRoomPublicationの状態。
     */
    val state: Publication.State
        get() = if (origin == null) {
            publication.state
        } else {
            publication.origin!!.state
        }

    /**
     * このRoomPublicationのStream。
     */
    val stream: Stream?
        get() = _getStream()

    /**
     * このRoomPublicationのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null
        set(value) {
            field = value
            publication.onMetadataUpdatedHandler = {
                value?.invoke(it)
            }
        }

    /**
     * このRoomPublicationがUnpublishされた時に発火するハンドラ。
     */
    var onUnpublishedHandler: (() -> Unit)? = null
        set(value) {
            field = value
            publication.onUnpublishedHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomPublicationのSubscribeされた時に発火するハンドラ。
     */
    var onSubscribedHandler: (() -> Unit)? = null
        set(value) {
            field = value
            publication.onSubscribedHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomPublicationのUnsubscribeされた時に発火するハンドラ。
     */
    var onUnsubscribedHandler: (() -> Unit)? = null
        set(value) {
            field = value
            publication.onUnsubscribedHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomPublicationに対するSubscriptionの数が変更された時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)? = null
        set(value) {
            field = value
            publication.onSubscriptionListChangedHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomPublicationの通信が有効になった時に発火するハンドラ。
     * [Publication.enable]が実行された時に発火します。
     */
    var onEnabledHandler: (() -> Unit)? = null
        set(value) {
            field = value
            publication.onEnabledHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomPublicationの通信が一時停止された時に発火するハンドラ。
     * [Publication.disable]が実行された時に発火します。
     */
    var onDisabledHandler: (() -> Unit)? = null
        set(value) {
            field = value
            publication.onDisabledHandler = {
                value?.invoke()
            }
        }

    internal val origin: Publication?
        get() = publication.origin

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is RoomPublication) return false
        return id == other.id
    }

    private fun getPrivatePublisher(): RoomMember? {
        val publisher =
            publication.origin?.publisher ?: publication.publisher
        return room.createRoomMember(publisher)
    }

    private fun _getStream(): Stream? {
        if (publication.origin == null) {
            return publication.stream
        }
        return publication.origin?.stream
    }

    /**
     *  publishを中止します。
     *  [onUnpublishedHandler]が発火します。
     */
    suspend fun cancel(): Boolean = withContext(Dispatchers.IO) {
        if (publication.origin == null) {
            return@withContext publication.cancel()
        }
        return@withContext publication.origin!!.cancel()
    }

    /**
     *  通信を開始します。[disable]によって停止していた場合は再開します。
     *  [onEnabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationEnabledHandler]が発火します。
     */
    suspend fun enable(): Boolean = withContext(Dispatchers.IO) {
        if (publication.origin == null) {
            return@withContext publication.enable()
        }
        return@withContext publication.origin!!.enable()
    }

    /**
     *  通信を一時停止します。
     *  [onDisabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationDisabledHandler]が発火します。
     */
    suspend fun disable(): Boolean = withContext(Dispatchers.IO) {
        if (publication.origin == null) {
            return@withContext publication.disable()
        }
        return@withContext publication.origin!!.disable()
    }

    /**
     *  通信に利用するエンコードを変更します。
     *  [onEnabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationEnabledHandler]が発火します。
     *
     *  @param encodings 設定するエンコード設定
     */
    fun updateEncodings(encodings: List<Encoding>) {
        if (publication.origin == null) {
            publication.updateEncodings(encodings)
        }
        publication.origin?.updateEncodings(encodings)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
