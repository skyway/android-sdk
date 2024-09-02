/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.p2p

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.member.RoomMember

/**
 * P2PRoomの操作を行うクラス。
 */
class P2PRoom internal constructor(private val channel: Channel) : Room(channel) {
    companion object {
        /**
         *  P2PRoomを探します。
         */
        @JvmStatic
        suspend fun find(name: String? = null, id: String? = null): P2PRoom? {
            val channel = Channel.find(name, id)
            return channel?.let { P2PRoom(it) }
        }

        /**
         *  P2PRoomを作成します。
         */
        @JvmStatic
        suspend fun create(name: String? = null, metadata: String? = null): P2PRoom? {
            check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }
            val channel = Channel.create(name, metadata)
            return channel?.let { P2PRoom(it) }
        }

        /**
         *  P2PRoomを探し、見つからなかった場合は作成します。
         */
        @JvmStatic
        suspend fun findOrCreate(name: String? = null, metadata: String? = null): P2PRoom? {
            check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }
            val channel = Channel.findOrCreate(name, metadata)
            return channel?.let { P2PRoom(it) }
        }
    }

    override val type: Type = Type.P2P

    init {
        initEventHandler()
    }

    /**
     *  Roomへ参加します。
     */
    override suspend fun join(memberInit: RoomMember.Init): LocalP2PRoomMember? {
        val channelMemberInit = Member.Init(
            memberInit.name,
            memberInit.metadata,
            memberInit.keepAliveIntervalSec,
            memberInit.keepaliveIntervalGapSec,
            Member.Type.PERSON,
            "",
        )
        val localPerson = channel.join(channelMemberInit) ?: return null
        return createRoomMember(localPerson) as LocalP2PRoomMember
    }

    // Impl of Channel EventHandler
    private fun initEventHandler() {
        channel.onMemberJoinedHandler = {
            onMemberJoined(it)
        }
        channel.onMemberLeftHandler = {
            onMemberLeft(it)
        }
        channel.onMemberMetadataUpdatedHandler = { member, metadata ->
            onMemberMetadataUpdated(member, metadata)
        }
        channel.onStreamPublishedHandler = {
            onStreamPublished(it)
        }
        channel.onStreamUnpublishedHandler = {
            onStreamUnpublished(it)
        }
        channel.onPublicationEnabledHandler = {
            onPublicationEnabled(it)
        }
        channel.onPublicationDisabledHandler = {
            onPublicationDisabled(it)
        }
        channel.onPublicationMetadataUpdatedHandler = { publication, metadata ->
            onPublicationMetadataUpdated(publication, metadata)
        }

        channel.onPublicationSubscribedHandler = {
            onPublicationSubscribed(it)
        }
        channel.onPublicationUnsubscribedHandler = {
            onPublicationUnsubscribed(it)
        }
    }

    private fun onMemberJoined(member: Member) {
        val roomMember = createRoomMember(member)
        roomMember?.let { onMemberJoinedHandler?.invoke(it) }
        onMemberListChangedHandler?.invoke()
    }

    private fun onMemberLeft(member: Member) {
        val roomMember = createRoomMember(member) ?: return
        onMemberLeftHandler?.invoke(roomMember)
        onMemberListChangedHandler?.invoke()
    }

    private fun onMemberMetadataUpdated(member: Member, metadata: String) {
        val roomMember = createRoomMember(member) ?: return
        onMemberMetadataUpdatedHandler?.invoke(roomMember, metadata)
    }

    private fun onStreamPublished(publication: Publication) {
        onStreamPublishedHandler?.invoke(createRoomPublication(publication))
        onPublicationListChangedHandler?.invoke()
    }

    private fun onStreamUnpublished(publication: Publication) {
        onStreamUnpublishedHandler?.invoke(createRoomPublication(publication))
        onPublicationListChangedHandler?.invoke()
    }

    private fun onPublicationEnabled(publication: Publication) {
        val roomPublication = createRoomPublication(publication)
        onPublicationEnabledHandler?.invoke(roomPublication)
    }

    private fun onPublicationDisabled(publication: Publication) {
        val roomPublication = createRoomPublication(publication)
        onPublicationDisabledHandler?.invoke(roomPublication)
    }

    private fun onPublicationMetadataUpdated(publication: Publication, metadata: String) {
        val roomPublication = createRoomPublication(publication)
        onPublicationMetadataUpdatedHandler?.invoke(roomPublication, metadata)
    }

    private fun onPublicationSubscribed(subscription: Subscription) {
        val roomSubscription = createRoomSubscription(subscription)
        onPublicationSubscribedHandler?.invoke(roomSubscription)
        onSubscriptionListChangedHandler?.invoke()
    }

    private fun onPublicationUnsubscribed(subscription: Subscription) {
        onPublicationUnsubscribedHandler?.invoke(createRoomSubscription(subscription))
        onSubscriptionListChangedHandler?.invoke()
    }
}
