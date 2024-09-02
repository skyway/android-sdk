/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.util.Logger

/**
 * Memberの操作を行うクラス。
 */
interface Member {
    /**
     * Memberの初期設定。
     */
    data class Init(
        /**
         * 名前。
         */
        val name: String? = null,
        /**
         * Metadata。
         */
        val metadata: String? = null,
        /**
         * 生存確認の間隔。
         */
        val keepAliveIntervalSec: Int = 30,
        /**
         * 生存確認の間隔を超えてChannelからMemberが削除されるまでの時間。
         */
        val keepaliveIntervalGapSec: Int = 30,
        /**
         * 種別。
         */
        val type: Type = Type.PERSON,
        /**
         * 詳細な種別。
         */
        val subtype: String = "",
    )

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
                    else -> {
                        Logger.logE("Invalid state string($state). LEFT is return as default state")
                        return LEFT
                    }
                }
            }
        }
    }

    data class Dto(
        val channel: Channel,
        val id: String,
        val name: String?,
        val nativePointer: Long
    )

    /**
     * このMemberが所属するChannel。
     */
    val channel: Channel

    /**
     * このMemberのID。
     */
    val id: String

    /**
     * このMemberの名前。
     */
    val name: String?

    val nativePointer: Long

    /**
     * このMemberの種別。
     */
    val type: Type

    /**
     * このMemberの詳細な種別。
     */
    val subType: String

    /**
     * このMemberの所在。
     */
    val side: Side

    /**
     * このMemberのMetadata。
     */
    val metadata: String?

    /**
     * このMemberの状態。
     */
    val state: State

    /**
     * このMemberのPublication一覧。
     */
    val publications: List<Publication>

    /**
     * このMemberのSubscription一覧。
     */
    val subscriptions: List<Subscription>

    /**
     * このMemberがChannelから退出した時に発火するハンドラ。
     */
    var onLeftHandler: (() -> Unit)?

    /**
     * このMemberのMetadataが更新されたときに発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)?

    /**
     * このMemberのPublication一覧の数が変わった時に発火するハンドラ。
     */
    var onPublicationListChangedHandler: (() -> Unit)?

    /**
     * このMemberのSubscription一覧の数が変わった時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)?

    /**
     *  Metadataを更新します。
     */
    suspend fun updateMetadata(metadata: String): Boolean

    /**
     *  Channelから退室します。
     */
    suspend fun leave(): Boolean
}
