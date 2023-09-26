/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteStream
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Subscriptionの操作を行うクラス。
 */
class Subscription internal constructor(
    /**
     * このSubscriptionが所属する[Channel]。
     */
    val channel: Channel,
    /**
     * このSubscriptionのID。
     */
    val id: String,
    private val subscriberId: String,
    private val publicationId: String,
    /**
     * このSubscriptionの[ContentType]。
     */
    val contentType: ContentType,
    internal val nativePointer: Long,
    stream: RemoteStream?
) {
    /**
     * Subscribe時の設定。
     */
    data class Options(
//        val isEnabled: Boolean?,
        val preferredEncodingId: String? = null) {
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
                    else -> throw IllegalStateException("Invalid state")
                }
            }
        }
    }

    /**
     * このSubscriptionのSubscriber。
     */
    val subscriber: Member
        get() = channel.findMember(subscriberId)!!

    /**
     * このSubscriptionに対するPublication。
     */
    val publication: Publication
        get() = channel.findPublication(publicationId)!!

    /**
     * このSubscriptionの状態。
     *
     */
    val state: State
        get() = State.fromString(nativeState(nativePointer))

    /**
     *  このSubscriptionの優先エンコーディングID。
     */
    val preferredEncodingId: String
        get() = nativePreferredEncodingId(nativePointer)

    /**
     * このSubscriptionのStream。
     */
    var stream: RemoteStream? = stream
        internal set

    /**
     *  subscribeがキャンセルされた際に発火するハンドラ。
     */
    var onCanceledHandler: (() -> Unit)? = null
//    var onEnabledHandler: (() -> Unit)? = null
//    var onDisabledHandler: (() -> Unit)? = null

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        nativeAddEventListener(channel.id, nativePointer)
    }

//    suspend fun enable(): Boolean = withContext(Dispatchers.Default) {
//        return@withContext nativeEnable(nativePointer)
//    }
//
//    suspend fun disable(): Boolean = withContext(Dispatchers.Default) {
//        return@withContext nativeDisable(nativePointer)
//    }

    /**
     * 受信するエンコード設定を切り替えます。
     */
    fun changePreferredEncoding(id: String) {
        nativeChangePreferredEncoding(nativePointer, id)
    }

    /**
     *  統計情報を取得します。
     *  experimentalな機能です。
     */
    fun getStats(): WebRTCStats? {
        nativeGetStats(nativePointer)?.let {
            return Factory.createWebRTCStats(it)
        }
        return null
    }

    /**
     *  subscribeを中止します。
     */
    suspend fun cancel(): Boolean = withContext(Dispatchers.Default) {
        return@withContext nativeCancel(nativePointer)
    }

    private fun onCanceled() {
        Logger.logI("🔔onCanceled")
        scope.launch {
            onCanceledHandler?.invoke()
        }
    }

//    private fun onEnabled() {
//        onEnabledHandler?.invoke()
//    }
//
//    private fun onDisabled() {
//        onDisabledHandler?.invoke()
//    }

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
    private external fun nativeGetStats(ptr: Long):String?
}
