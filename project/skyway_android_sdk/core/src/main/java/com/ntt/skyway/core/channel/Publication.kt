/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.*
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.local.LocalStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Publicationの操作を行うクラス。
 */
class Publication internal constructor(
    /**
     * このPublicationが所属する[Channel]。
     */
    val channel: Channel,
    /**
     * このPublicationのID。
     */
    val id: String,
    /**
     * このPublicationの[ContentType]。
     */
    val contentType: ContentType,
    /**
     * このPublicationのPublisher。
     */
    val publisher: Member,
    /**
     * このPublicationのOrigin。
     */
    val origin: Publication?,
    /**
     * このPublicationのコーデック一覧。
     */
    val codecCapabilities: List<Codec>,
    val nativePointer: Long,
    /**
     * このPublicationのStream。
     */
    private var internalStream: Stream?
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
                    else -> throw IllegalStateException("Invalid state")
                }
            }
        }
    }

    /**
     *  このPublicationのMetadata。
     */
    val metadata: String
        get() = nativeMetadata(nativePointer)

    /**
     *  このPublicationの状態。
     */
    val state: State
        get() = State.fromString(nativeState(nativePointer))

    /**
     *  このPublicationに対する[Subscription]の一覧。
     */
    val subscriptions
        get() = channel.subscriptions.filter { it.publication == this }

    /**
     *  このPublicationのエンコーディング設定一覧。
     *  詳しい設定例については開発者ドキュメントの[大規模会議アプリを実装する上での注意点](https://skyway.ntt.com/ja/docs/user-guide/tips/large-scale/)をご覧ください
     */
    val encodings:List<Encoding>
        get(){
            val jsonArr = JsonParser.parseString(nativeEncodings(nativePointer)).asJsonArray
            return Encoding.fromJsonArray(jsonArr.asJsonArray)
        }

    /**
     * このPublicationのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    /**
     * このPublicationがUnpublishされた時に発火するハンドラ。
     */
    var onUnpublishedHandler: (() -> Unit)? = null

    /**
     * このPublicationのSubscribeされた時に発火するハンドラ。
     */
    var onSubscribedHandler: (() -> Unit)? = null

    /**
     * このPublicationのUnsubscribeされた時に発火するハンドラ。
     */
    var onUnsubscribedHandler: (() -> Unit)? = null

    /**
     * このPublicationに対するSubscriptionの数が変更された時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)? = null

    /**
     * このPublicationの通信が有効になった時に発火するハンドラ。
     * [Publication.enable]が実行された時に発火します。
     */
    var onEnabledHandler: (() -> Unit)? = null

    /**
     * このPublicationの通信が一時停止された時に発火するハンドラ。
     * [Publication.disable]が実行された時に発火します。
     */
    var onDisabledHandler: (() -> Unit)? = null

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)? = null

    val stream: Stream?
        get() = internalStream

    init {
        nativeAddEventListener(channel.id, nativePointer)
    }

    /**
     *  metadataを更新します。
     *  [onMetadataUpdatedHandler]が発火します。
     */
    suspend fun updateMetadata(metadata: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeUpdateMetadata(nativePointer, metadata)
    }

    /**
     *  publishを中止します。
     *  [onUnpublishedHandler]が発火します。
     */
    suspend fun cancel(): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeCancel(nativePointer)
    }

    /**
     *  通信を開始します。[disable]によって停止していた場合は再開します。
     *  [onEnabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationEnabledHandler]が発火します。
     */
    suspend fun enable(): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeEnable(nativePointer)
    }

    /**
     *  通信を一時停止します。
     *  [onDisabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationDisabledHandler]が発火します。
     */
    suspend fun disable(): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeDisable(nativePointer)
    }

    /**
     *  通信に利用するエンコードを変更します。
     *  [onEnabledHandler]が発火します。
     *  また、入室している[Channel]に対して[Channel.onPublicationEnabledHandler]が発火します。
     */
    fun updateEncodings(encodings: List<Encoding>) {
        val encodingsJson = Gson().toJson(encodings)
        nativeUpdateEncodings(nativePointer, encodingsJson)
    }

    /**
     *  送信するStreamを変更します。
     *  @param stream 変更先のStream。既にpublishしているstreamと同じcontentTypeである必要があります。
     */
    fun replaceStream(stream: LocalStream) {
        this.internalStream = stream
        nativeReplaceStream(nativePointer, stream.nativePointer)
    }

    /**
     *  統計情報を取得します。
     *  experimentalな機能です。
     *  @param remoteMemberId 対象の[RemoteMember]のID
     */
    fun getStats(remoteMemberId:String): WebRTCStats? {
        nativeGetStats(remoteMemberId,nativePointer)?.let {
            return Factory.createWebRTCStats(it)
        }
        return null;
    }

    private fun onMetadataUpdated(metadata: String) {
        onMetadataUpdatedHandler?.invoke(metadata)
    }

    private fun onUnpublished() {
        onUnpublishedHandler?.invoke()
    }

    private fun onSubscribed() {
        onSubscribedHandler?.invoke()
    }

    private fun onUnsubscribed() {
        onUnsubscribedHandler?.invoke()
    }

    private fun onSubscriptionListChanged() {
        onSubscriptionListChangedHandler?.invoke()
    }

    private fun onEnabled() {
        onEnabledHandler?.invoke()
    }

    private fun onDisabled() {
        onDisabledHandler?.invoke()
    }

    private fun onConnectionStateChanged(state: String) {
        onConnectionStateChangedHandler?.invoke(state)
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
    private external fun nativeGetStats(data: String, ptr: Long):String?
}
