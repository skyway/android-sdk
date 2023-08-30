/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room


import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
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
         *  エンコーディング設定一覧。
         *  詳しい設定例については開発者ドキュメントの[大規模会議アプリを実装する上での注意点](https://skyway.ntt.com/ja/docs/user-guide/tips/large-scale/)をご覧ください
         */
        val encodings: List<Encoding>? = null,
        /**
         *  publish時に有効にするか。
         */
        val isEnabled: Boolean? = null,
        /**
         *  最大のsubscriber数。
         *  SFURoomの利用時のみ有効です。
         *  最大値は99です。
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
        get() = room.subscriptions.filter { it.publication.id == this.id }

    /**
     * このRoomPublicationが所属する[Room]。
     */
    val codecCapabilities: List<Codec>
        get() = publication.codecCapabilities

    /**
     *  このRoomPublicationのエンコーディング設定一覧。
     *  詳しい設定例については開発者ドキュメントの[大規模会議アプリを実装する上での注意点](https://skyway.ntt.com/ja/docs/user-guide/tips/large-scale/)をご覧ください
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
    var onSubscribedHandler: ((subscription: RoomSubscription) -> Unit)? = null
        set(value) {
            field = value
            publication.onSubscribedHandler = {
                val subscription = room.createRoomSubscription(it)
                value?.invoke(subscription)
            }
        }

    /**
     * このRoomPublicationのUnsubscribeされた時に発火するハンドラ。
     */
    var onUnsubscribedHandler: ((subscription: RoomSubscription) -> Unit)? = null
        set(value) {
            field = value
            publication.onUnsubscribedHandler = {
                val subscription = room.createRoomSubscription(it)
                value?.invoke(subscription)
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

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)? = null
        set(value) {
            field = value
            if (publication.origin != null) {
                publication.origin?.onConnectionStateChangedHandler = {
                    value?.invoke(it)
                }
            } else {
                publication.onConnectionStateChangedHandler = {
                    value?.invoke(it)
                }
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
    suspend fun cancel(): Boolean = withContext(Dispatchers.Default) {
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
    suspend fun enable(): Boolean = withContext(Dispatchers.Default) {
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
    suspend fun disable(): Boolean = withContext(Dispatchers.Default) {
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

    /**
     *  送信するStreamを変更します。
     *  @param stream 変更先のStream。既にpublishしているstreamと同じcontentTypeである必要があります。
     */
    fun replaceStream(stream: LocalStream) {
        publication.replaceStream(stream)
    }

    /**
     *  統計情報を取得します。
     *  experimentalな機能です。
     *  @param remoteMemberId 対象の[RemoteMember]のID
     */
    @SkyWayOptIn
    fun getStats(remoteMemberId: String): WebRTCStats? {
        val origin = publication.origin
        if (origin == null) {
            return publication.getStats(remoteMemberId)
        } else {
            val botId =
                origin.subscriptions.find { it.subscriber.type == Member.Type.BOT }?.subscriber?.id
                    ?: run {
                        Logger.logE("Bot is not found")
                        return null
                    }
            return origin.getStats(botId)
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
