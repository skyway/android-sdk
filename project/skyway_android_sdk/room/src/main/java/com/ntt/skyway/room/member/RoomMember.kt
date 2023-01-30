/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.member

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RoomMemberの操作を行う抽象クラス。
 */
abstract class RoomMember internal constructor(
    /**
     * このRoomMemberが所属する[Room]。
     */
    val room: Room, internal val member: Member) {

    /**
     * RoomMemberの初期設定。
     */
    data class Init(
        /**
         * RoomMemberの名前。
         */
        val name: String,

        /**
         * RoomMemberのMetadata。
         */
        val metadata: String? = "",

        /**
         * 生存確認の間隔。
         */
        val keepAliveIntervalSec: Int = 1000
    )

    /**
     * このRoomMemberのID。
     */
    val id: String
        get() = member.id

    /**
     * このRoomMemberの名前。
     */
    val name: String
        get() = member.name

    /**
     * このRoomMemberのMetadata。
     */
    val metadata: String
        get() = member.metadata

    /**
     * このRoomMemberの所在。
     */
    open val side: Member.Side
        get() = member.side

    /**
     * このRoomMemberの状態。
     */
    val state: Member.State
        get() = member.state

    /**
     * このRoomMemberのPublication一覧。
     */
    val publications
        get() = room.publications.filter { it.publisher?.id == this.id }

    /**
     * このRoomMemberのSubscription一覧。
     */
    val subscriptions
        get() = room.subscriptions.filter { it.subscriber?.id == this.id }

    /**
     * このRoomMemberが退出した時に発火するハンドラ。
     */
    var onLeftHandler: (() -> Unit)? = null
        set(value) {
            field = value
            member.onLeftHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomMemberのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null
        set(value) {
            field = value
            member.onMetadataUpdatedHandler = {
                value?.invoke(it)
            }
        }

    /**
     * このRoomMemberのPublicationの数が変化した時に発火するハンドラ。
     */
    var onPublicationListChangedHandler: (() -> Unit)? = null
        set(value) {
            field = value
            member.onPublicationListChangedHandler = {
                value?.invoke()
            }
        }

    /**
     * このRoomMemberのSubscriptionの数が変化した時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)? = null
        set(value) {
            field = value
            member.onSubscriptionListChangedHandler = {
                value?.invoke()
            }
        }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is RoomMember) return false
        return id == other.id
    }

    /**
     *  Metadataを更新します。
     */
    suspend fun updateMetadata(metadata: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext member.updateMetadata(metadata)
    }

    /**
     *  Roomから退室します。
     */
    suspend fun leave(): Boolean = withContext(Dispatchers.IO) {
        return@withContext member.leave()
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
