/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room

import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.remote.RemoteStream
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.content.WebRTCStats
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RoomSubscriptionの操作を行うクラス。
 */
class RoomSubscription internal constructor(
    /**
     * このRoomSubscriptionが所属する[Room]。
     */
    val room: Room,
    private val subscription: Subscription
) {

    /**
     * Subscribe時の設定。
     */
    data class Options(
        /**
         *  subscribe時に選択するエンコーディング設定。
         *  [RoomPublication.Options.encodings]から選択する項目のidを設定してください
         */
        val preferredEncodingId: String? = null
    ) {
        internal fun toCore(): Subscription.Options {
            return Subscription.Options(preferredEncodingId)
        }
    }

    /**
     * このRoomSubscriptionのID。
     */
    val id: String
        get() = subscription.id

    /**
     * このRoomSubscriptionの[Stream.ContentType]。
     */
    val contentType: Stream.ContentType
        get() = subscription.contentType

    /**
     * このRoomPublicationに対する[RoomPublication]。
     */
    val publication: RoomPublication
        get() = room.createRoomPublication(subscription.publication)

    /**
     * このRoomSubscriptionのSubscriber。
     */
    val subscriber: RoomMember?
        get() = room.createRoomMember(subscription.subscriber)

    /**
     * このRoomSubscriptionの状態。
     */
    val state: Subscription.State
        get() = subscription.state

    /**
     * このRoomSubscriptionの優先エンコーディングID。
     */
    val preferredEncodingId: String
        get() = subscription.preferredEncodingId

    /**
     * このRoomSubscriptionの[Stream]。
     */
    val stream: RemoteStream?
        get() = subscription.stream

    /**
     *  subscribeがキャンセルされた際に発火するハンドラ。
     */
    var onCanceledHandler: (() -> Unit)? = null
        set(value) {
            field = value
            subscription.onCanceledHandler = {
                value?.invoke()
            }
        }
//    var onEnabledHandler: (() -> Unit)? = null
//    var onDisabledHandler: (() -> Unit)? = null

    /**
     *  メディア通信の状態が変化した際に発火するハンドラ。
     */
    var onConnectionStateChangedHandler: ((state: String) -> Unit)? = null
        set(value) {
            field = value
            subscription.onConnectionStateChangedHandler = {
                value?.invoke(it)
            }
        }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is RoomSubscription) return false
        return id == other.id
    }

    /**
     *  subscribeを中止します。
     */
    suspend fun changePreferredEncoding(preferredEncodingId: String) = withContext(Dispatchers.IO) {
        subscription.changePreferredEncoding(preferredEncodingId)
    }

    /**
     *  統計情報を取得します。
     *  experimentalな機能です。
     */
    @SkyWayOptIn
    fun getStats(): WebRTCStats? {
        return subscription.getStats()
    }

    /**
     *  publishを中止します。
     *  [onUnsubscribedHandler]が発火します。
     */
    suspend fun cancel(): Boolean = withContext(Dispatchers.IO) {
        return@withContext subscription.cancel()
    }

//    suspend fun enable(): Boolean = withContext(Dispatchers.IO) {
//        return@withContext subscription.enable()
//    }
//
//    suspend fun disable(): Boolean = withContext(Dispatchers.IO) {
//        return@withContext subscription.disable()
//    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
