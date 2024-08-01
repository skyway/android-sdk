/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.core.content.remote.RemoteStream
import com.ntt.skyway.core.util.Logger

/**
 * Subscriptionの操作を行うクラス。
 */
interface Subscription {
    /**
     * Subscribe時の設定。
     */
    data class Options(
        val preferredEncodingId: String? = null
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    /**
     * Subscriptionの状態。
     */
    enum class State {
        ENABLED, DISABLED, CANCELED;

        companion object {
            internal fun fromString(state: String): State {
                return when (state) {
                    "enabled" -> ENABLED
                    "disabled" -> DISABLED
                    "canceled" -> CANCELED
                    else -> {
                        Logger.logE("Invalid state string($state). CANCELED is return as default state")
                        return CANCELED
                    }
                }
            }
        }
    }

    /**
     * このSubscriptionが所属する[Channel]。
     */
    val channel: Channel

    /**
     * このSubscriptionのID。
     */
    val id: String

    /**
     * このSubscriptionの[ContentType]。
     */
    val contentType: ContentType

    val nativePointer: Long

    /**
     * このSubscriptionのStream。
     * `LocalPerson.subscribe`の返り値でSubscriptionを入手した場合、入手時点で値がsetされています。
     * その他、イベントの発火によってSubscriptionを取得した場合、まだ値がsetされていない可能性があります。
     */
    val stream: RemoteStream?

    /**
     * このSubscriptionのSubscriber。
     */
    val subscriber: Member

    /**
     * このSubscriptionに対するPublication。
     */
    val publication: Publication

    /**
     * このSubscriptionの状態。
     *
     */
    val state: State

    /**
     *  このSubscriptionの優先エンコーディングID。
     */
    val preferredEncodingId: String

    /**
     *  subscribeがキャンセルされた際に発火するハンドラ。
     */
    @Deprecated(
        message = "v2.1.5から非推奨になりました。" +
                "代わりに LocalPerson.onPublicationUnsubscribedHandler " +
                "もしくは Channel.onPublicationUnsubscribedHandler を利用してください。",
        replaceWith = ReplaceWith(
            expression = "LocalPerson.onPublicationUnsubscribedHandler",
            imports = [
                "com.ntt.skyway.core.channel.member.LocalPerson"
            ]
        )
    )
    var onCanceledHandler: (() -> Unit)?

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)?

    /**
     * 受信するエンコード設定を切り替えます。
     */
    fun changePreferredEncoding(id: String)

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    fun getStats(): WebRTCStats?

    /**
     *  subscribeを中止します。
     */
    @Deprecated(
        message = "v2.1.5から非推奨になりました。" +
                "代わりに LocalPerson.unsubscribe もしくは " +
                "RemotePerson.unsubscribe を利用してください。",
        replaceWith = ReplaceWith(
            expression = "LocalPerson.unsubscribe",
            imports = [
                "com.ntt.skyway.core.channel.member.LocalPerson"
            ]
        )
    )
    suspend fun cancel(): Boolean
}
