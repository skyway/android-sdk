/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.ntt.skyway.core.content.Stream.ContentType
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteStream
import com.ntt.skyway.core.channel.member.Member
import kotlinx.coroutines.Dispatchers
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
    /**
     * このSubscriptionの[ContentType]。
     */
    val contentType: ContentType,
    /**
     * このSubscriptionのSubscriber。
     */
    val subscriber: Member,
    /**
     * このSubscriptionに対するPublication。
     */
    val publication: Publication,
    internal val nativePointer: Long,
    /**
     * このSubscriptionのStream。
     */
    val stream: RemoteStream?
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
     * このSubscriptionの状態。
     *
     */
    val state: State
        get() = State.fromString(nativeState(nativePointer))

    /**
     *  subscribeがキャンセルされた際に発火するハンドラ。
     */
    var onCanceledHandler: (() -> Unit)? = null
//    var onEnabledHandler: (() -> Unit)? = null
//    var onDisabledHandler: (() -> Unit)? = null

    init {
        nativeAddEventListener(nativePointer)
    }

    /**
     *  subscribeを中止します。
     */
    suspend fun cancel(): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeCancel(nativePointer)
    }

//    suspend fun enable(): Boolean = withContext(Dispatchers.IO) {
//        return@withContext nativeEnable(nativePointer)
//    }
//
//    suspend fun disable(): Boolean = withContext(Dispatchers.IO) {
//        return@withContext nativeDisable(nativePointer)
//    }

    /**
     * 受信するエンコード設定を切り替えます。
     */
    fun changePreferredEncoding(id: String) {
        nativeChangePreferredEncoding(nativePointer, id)
    }

    private fun onCanceled() {
        onCanceledHandler?.invoke()
    }

//    private fun onEnabled() {
//        onEnabledHandler?.invoke()
//    }
//
//    private fun onDisabled() {
//        onDisabledHandler?.invoke()
//    }

    private external fun nativeAddEventListener(ptr: Long)
    private external fun nativeState(ptr: Long): String
    private external fun nativeCancel(ptr: Long): Boolean
    private external fun nativeEnable(ptr: Long): Boolean
    private external fun nativeDisable(ptr: Long): Boolean
    private external fun nativeChangePreferredEncoding(ptr: Long, id: String)
}
