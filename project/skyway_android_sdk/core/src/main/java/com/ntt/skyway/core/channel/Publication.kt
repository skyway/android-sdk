/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger

/**
 * Publicationの操作を行うクラス。
 */
interface Publication {
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
         *  publish時にenableかdisableか。
         */
        val isEnabled: Boolean? = null
    ) {
        internal fun toJson(): String {
            return Gson().toJson(this)
        }
    }

    /**
     *  Publicationの状態。
     *  enable/disableは有効/無効。CANCELEDはPublish終了後の状態です。
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
     * このPublicationが所属する[Channel]。
     */
    val channel: Channel

    /**
     * このPublicationのID。
     */
    val id: String

    /**
     * このPublicationの[ContentType]。
     */
    val contentType: ContentType

    /**
     * このPublicationのコーデック一覧。
     */
    val codecCapabilities: List<Codec>

    val nativePointer: Long

    /**
     * このPublicationのStream。
     */
    val stream: Stream?

    /**
     * このPublicationのPublisher。
     */
    val publisher: Member

    /**
     * このPublicationのOrigin。
     */
    val origin: Publication?

    /**
     *  このPublicationのMetadata。
     */
    val metadata: String

    /**
     *  このPublicationの状態。
     */
    val state: State

    /**
     *  このPublicationに対する[Subscription]の一覧。
     */
    val subscriptions: List<Subscription>

    /**
     *  このPublicationのエンコーディング設定一覧。
     *  詳しい設定例については開発者ドキュメントの[大規模会議アプリを実装する上での注意点](https://skyway.ntt.com/ja/docs/user-guide/tips/large-scale/)をご覧ください
     */
    val encodings: List<Encoding>

    /**
     * このPublicationのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)?

    /**
     * このPublicationがUnpublishされた時に発火するハンドラ。
     */
    var onUnpublishedHandler: (() -> Unit)?

    /**
     * このPublicationのSubscribeされた時に発火するハンドラ。
     */
    var onSubscribedHandler: ((subscription: Subscription) -> Unit)?

    /**
     * このPublicationのUnsubscribeされた時に発火するハンドラ。
     */
    var onUnsubscribedHandler: ((subscription: Subscription) -> Unit)?

    /**
     * このPublicationに対するSubscriptionの数が変更された時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)?

    /**
     * このPublicationの通信が有効になった時に発火するハンドラ。
     * [Publication.enable]が実行された時に発火します。
     */
    var onEnabledHandler: (() -> Unit)?

    /**
     * このPublicationの通信が一時停止された時に発火するハンドラ。
     * [Publication.disable]が実行された時に発火します。
     */
    var onDisabledHandler: (() -> Unit)?

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)?

    /**
     *  metadataを更新します。
     *  [onMetadataUpdatedHandler]が発火します。
     */
    suspend fun updateMetadata(metadata: String): Boolean

    /**
     *  publishを中止します。
     *  [onUnpublishedHandler]が発火します。
     */
    suspend fun cancel(): Boolean

    /**
     *  通信を開始します。[disable]によって停止していた場合は再開します。
     *  [onEnabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationEnabledHandler]が発火します。
     */
    suspend fun enable(): Boolean

    /**
     *  通信を一時停止します。
     *  [onDisabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationDisabledHandler]が発火します。
     */
    suspend fun disable(): Boolean

    /**
     *  通信に利用するエンコードを変更します。
     *  [onEnabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationEnabledHandler]が発火します。
     */
    fun updateEncodings(encodings: List<Encoding>)

    /**
     *  送信するStreamを変更します。
     *  @param stream 変更先のStream。既にpublishしているstreamと同じcontentTypeである必要があります。
     */
    fun replaceStream(stream: LocalStream): Boolean

    /**
     *  統計情報を取得します。
     *  experimentalな機能です。
     *  @param remoteMemberId 対象の[RemoteMember]のID
     */
    fun getStats(remoteMemberId: String): WebRTCStats?
}
