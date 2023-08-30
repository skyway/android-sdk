/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Memberの操作を行うクラス。
 */
abstract class Member internal constructor(
    /**
     * このMemberが所属するChannel。
     */
    val channel: Channel,
    /**
     * このMemberのID。
     */
    val id: String,
    /**
     * このMemberの名前。
     */
    val name: String,
    val nativePointer: Long
) {
    /**
     * Memberの所在。
     */
    enum class Side {
        LOCAL, REMOTE
    }

    /**
     * Memberの種別。
     */
    enum class Type {
        PERSON, BOT, UNKNOWN
    }

    /**
     * Memberの状態。
     */
    enum class State {
        JOINED, LEFT;

        companion object {
            internal fun fromString(state: String): State {
                return when (state) {
                    "joined" -> JOINED
                    "left" -> LEFT
                    else -> throw IllegalStateException("Invalid state")
                }
            }
        }
    }

    /**
     * Memberの初期設定。
     */
    data class Init(
        /**
         * 名前。
         */
        val name: String,
        /**
         * Metadata。
         */
        val metadata: String? = "",
        /**
         * 生存確認の間隔。
         */
        val keepAliveIntervalSec: Int = 1000,
        /**
         * 種別。
         */
        val type: Type = Type.PERSON,
        /**
         * 詳細な種別。
         */
        val subtype: String = "",
    )

    data class Dto(
        val channel: Channel,
        val id: String,
        val name: String,
        val nativePointer: Long
    )

    /**
     * このMemberの種別。
     */
    abstract val type: Type

    /**
     * このMemberの詳細な種別。
     */
    abstract val subType: String

    /**
     * このMemberの所在。
     */
    abstract val side: Side

    /**
     * このMemberのMetadata。
     */
    val metadata: String
        get() = nativeMetadata(nativePointer)

    /**
     * このMemberの状態。
     */
    val state: State
        get() = State.fromString(nativeState(nativePointer))

    /**
     * このMemberのPublication一覧。
     */
    val publications
        get() = channel.publications.filter { it.publisher == this }

    /**
     * このMemberのSubscription一覧。
     */
    val subscriptions
        get() = channel.subscriptions.filter { it.subscriber == this }

    /**
     * このMemberがChannelから退出した時に発火するハンドラ。
     */
    var onLeftHandler: (() -> Unit)? = null

    /**
     * このMemberのMetadataが更新されたときに発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    /**
     * このMemberのPublication一覧の数が変わった時に発火するハンドラ。
     */
    var onPublicationListChangedHandler: (() -> Unit)? = null

    /**
     * このMemberのSubscription一覧の数が変わった時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    constructor(dto: Dto) : this(
        dto.channel,
        dto.id,
        dto.name,
        dto.nativePointer
    )

    internal open fun addEventListener() {
        nativeAddEventListener(channel.id, nativePointer)
    }

    /**
     *  Metadataを更新します。
     */
    suspend fun updateMetadata(metadata: String): Boolean = withContext(Dispatchers.Default) {
        return@withContext nativeUpdateMetadata(nativePointer, metadata)
    }

    /**
     *  Channelから退室します。
     */
    suspend fun leave(): Boolean = withContext(Dispatchers.Default) {
        return@withContext nativeLeave(nativePointer)
    }

    private fun onLeft() {
        Logger.logI("🔔onLeft")
        scope.launch {
            onLeftHandler?.invoke()
        }
    }

    private fun onMetadataUpdated(metadata: String) {
        Logger.logI("🔔onMetadataUpdated")
        scope.launch {
            onMetadataUpdatedHandler?.invoke(metadata)
        }
    }

    private fun onPublicationListChanged() {
        Logger.logI("🔔onPublicationListChanged")
        scope.launch {
            onPublicationListChangedHandler?.invoke()
        }
    }

    private fun onSubscriptionListChanged() {
        Logger.logI("🔔onSubscriptionListChanged")
        scope.launch {
            onSubscriptionListChangedHandler?.invoke()
        }
    }


    private external fun nativeAddEventListener(channelId: String, ptr: Long)
    private external fun nativeMetadata(ptr: Long): String
    private external fun nativeState(ptr: Long): String
    private external fun nativeUpdateMetadata(ptr: Long, metadata: String): Boolean
    private external fun nativeLeave(ptr: Long): Boolean
}
