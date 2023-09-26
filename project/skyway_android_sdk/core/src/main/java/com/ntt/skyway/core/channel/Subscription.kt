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
    var onCanceledHandler: (() -> Unit)?

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)?

    /**
     * 受信するエンコード設定を切り替えます。
     */
    fun changePreferredEncoding(id: String)

    /**
     *  統計情報を取得します。
     *  experimentalな機能です。
     */
    fun getStats(): WebRTCStats?

    /**
     *  subscribeを中止します。
     */
    suspend fun cancel(): Boolean
}
