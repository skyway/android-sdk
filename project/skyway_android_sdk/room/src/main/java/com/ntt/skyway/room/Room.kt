/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.plugin.remotePerson.RemotePerson
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/**
 * Roomの操作を行うクラス。
 */
abstract class Room internal constructor(private val channel: Channel) {
    /**
     * Roomの種別。
     */
    enum class Type {
        P2P, SFU
    }

    /**
     *  このRoomのID。
     */
    val id: String
        get() = channel.id

    /**
     *  このRoomのname。
     */
    val name: String?
        get() = channel.name

    /**
     *  このRoomの種別。
     */
    abstract val type: Type

    /**
     *  このRoomのMetadata。
     */
    val metadata: String?
        get() = channel.metadata

    /**
     *  このRoomの状態。
     */
    val state: Channel.State
        get() = channel.state

    internal val mutex = Mutex()

    /**
     * このRoom内の[RoomMember]一覧。
     */
    val members: Set<RoomMember>
        get() = getPrivateMembers()

    /**
     * このRoom内にこのSDKから参加しているLocalRoomMember。
     */
    val localRoomMember: LocalRoomMember?
        get() = if (members.find { it.side == Member.Side.LOCAL } != null) {
            members.find { it.side == Member.Side.LOCAL } as LocalRoomMember
        } else {
            null
        }

    /**
     * このRoom内のPublicationの一覧。
     */
    val publications: Set<RoomPublication>
        get() = getPrivatePublications()

    /**
     * このRoom内のSubscriptionの一覧。
     */
    val subscriptions: Set<RoomSubscription>
        get() = getPrivateSubscriptions()

    /**
     * このRoomが閉じられた時に発火するハンドラ。
     */
    var onClosedHandler: (() -> Unit)? = null

    /**
     * このRoomのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    /**
     * このRoom内のMemberの数が変更された時に発火するハンドラ。
     */
    var onMemberListChangedHandler: (() -> Unit)? = null

    /**
     * このRoomにMemberが入室した時に発火するハンドラ。
     */
    var onMemberJoinedHandler: ((member: RoomMember) -> Unit)? = null

    /**
     * このRoomからMemberが退出した時に発火するハンドラ。
     */
    var onMemberLeftHandler: ((member: RoomMember) -> Unit)? = null

    /**
     * このRoom内のMemberのMetadataが更新された時に発火するハンドラ。
     */
    var onMemberMetadataUpdatedHandler: ((member: RoomMember, metadata: String) -> Unit)? = null

    /**
     * このRoom内のPublicationの数が変更された時に発火するハンドラ。
     */
    var onPublicationListChangedHandler: (() -> Unit)? = null

    /**
     * このRoomにStreamがPublishされた時に発火するハンドラ。
     */
    var onStreamPublishedHandler: ((publication: RoomPublication) -> Unit)? = null

    /**
     * このRoomにStreamがUnpublishされた時に発火するハンドラ。
     */
    var onStreamUnpublishedHandler: ((publication: RoomPublication) -> Unit)? = null

    /**
     * このRoom内のPublicationがEnableになった時に発火するハンドラ。
     */
    var onPublicationEnabledHandler: ((publication: RoomPublication) -> Unit)? = null

    /**
     * PublicationがDisableになった時に発火するハンドラ。
     */
    var onPublicationDisabledHandler: ((publication: RoomPublication) -> Unit)? = null

    /**
     * PublicationのMetadataが更新された時に発火するハンドラ。
     */
    var onPublicationMetadataUpdatedHandler: ((publication: RoomPublication, metadata: String) -> Unit)? =
        null

    /**
     * SubscriptionのList変わった時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)? = null

    /**
     * StreamがSubscribeされた時に発火するハンドラ。
     * Subscriptionにはまだstreamがsetされていない可能性があります。
     */
    var onPublicationSubscribedHandler: ((subscription: RoomSubscription) -> Unit)? = null

    /**
     * StreamがUnsubscribeされた時に発火するハンドラ。
     */
    var onPublicationUnsubscribedHandler: ((subscription: RoomSubscription) -> Unit)? = null

//    /**
//     * SubscriptionがEnableになった時に発火するハンドラ。
//     */
//    var onSubscriptionEnabledHandler: ((subscription: RoomSubscription) -> Unit)? = null
//
//    /**
//     * SubscriptionがDisableになった時に発火するハンドラ。
//     */
//    var onSubscriptionDisabledHandler: ((subscription: RoomSubscription) -> Unit)? = null

    /**
     * 各種イベントでエラーが起きた時に発火するハンドラ。
     */
    var onErrorHandler: ((e: Exception) -> Unit)? = null

    private val factory: Factory = Factory(this)

    init {
        initEventHandler()
    }

    /**
     *  metadataを更新します。
     *  [onMetadataUpdatedHandler]が発火します。
     *
     *  @param metadata 更新後のMetadata
     */
    suspend fun updateMetadata(metadata: String):Boolean = withContext(Dispatchers.Default) {
        return@withContext channel.updateMetadata(metadata)
    }

    /**
     *  Roomに入室します。
     *  [onMemberJoinedHandler]が発火します。
     *
     *  @param memberInit 入室させるRoomMemberの初期設定
     */
    abstract suspend fun join(memberInit: RoomMember.Init): LocalRoomMember?

    /**
     *  指定したメンバーをRoomから退室させます。
     *  [onMemberLeftHandler]が発火します。
     *
     *  @param member 退室させる[RoomMember]
     */
    suspend fun leave(member: RoomMember): Boolean = withContext(Dispatchers.Default) {
        return@withContext channel.leave(member.member)
    }

    /**
     *  Roomを閉じます。
     *  [onClosedHandler]が発火します。
     */
    suspend fun close(): Boolean = withContext(Dispatchers.Default) {
        return@withContext channel.close()
    }

    /**
     *  Roomを破棄します。
     *  破棄されたRoomではイベントが発火しなくなります。
     *  またRoom内の[RoomMember]、[RoomPublication]、[RoomSubscription]も破棄されます。
     */
    fun dispose() {
        channel.dispose()
    }

    private fun getPrivateMembers(): Set<RoomMember> {
        val roomMembers = mutableListOf<RoomMember>()
        for (mem in channel.members) {
            if (type == Type.SFU && mem.type == Member.Type.BOT) continue
            createRoomMember(mem)?.let { roomMembers.add(it) }
        }
        return roomMembers.toSet()
    }

    private fun getPrivatePublications(): Set<RoomPublication> {
        val roomPublication = mutableListOf<RoomPublication>()
        for (pub in channel.publications) {
            if (type == Type.SFU && pub.origin == null) continue
            roomPublication.add(createRoomPublication(pub))
        }
        return roomPublication.toSet()
    }

    private fun getPrivateSubscriptions(): Set<RoomSubscription> {
        val roomSubscriptions = mutableListOf<RoomSubscription>()
        for (sub in channel.subscriptions) {
            if (type == Type.SFU && sub.subscriber.type == Member.Type.BOT) continue
            roomSubscriptions.add(createRoomSubscription(sub))
        }
        return roomSubscriptions.toSet()
    }

    internal fun createRoomMember(channelMember: Member): RoomMember? {
        return when (channelMember) {
            is RemotePerson -> {
                factory.createRemoteRoomMember(channelMember)
            }
            is LocalPerson -> {
                factory.createLocalRoomMember(channelMember)
            }
            else -> null
        }
    }

    internal fun createRoomPublication(channelPublication: Publication): RoomPublication {
        return factory.createRoomPublication(channelPublication)
    }

    internal fun createRoomSubscription(channelSubscription: Subscription): RoomSubscription {
        return factory.createRoomSubscription(channelSubscription)
    }

    // Impl of Channel EventHandler
    private fun initEventHandler() {
        channel.onClosedHandler = {
            onClosedHandler?.invoke()
        }

        channel.onMetadataUpdatedHandler = {
            onMetadataUpdatedHandler?.invoke(it)
        }

        channel.onErrorHandler = {
            onErrorHandler?.invoke(it)
        }
    }
}
