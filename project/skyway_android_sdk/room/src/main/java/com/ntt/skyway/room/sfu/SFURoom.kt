/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.sfu

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.plugin.sfuBot.SFUBot
import com.ntt.skyway.plugin.sfuBot.SFUBotPlugin
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * SFURoomの操作を行うクラス。
 */
class SFURoom internal constructor(private val channel: Channel) : Room(channel) {
    companion object {
        /**
         *  SFURoomを探します。
         */
        @JvmStatic
        suspend fun find(name: String? = null, id: String? = null): SFURoom? {
            check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }
            if (SkyWayContext.findPlugin("sfu") == null) {
                SkyWayContext.registerPlugin(SFUBotPlugin())
            }
            val channel = Channel.find(name, id)
            if (channel != null) {
                if (channel.bots.isEmpty()) {
                    SFUBot.createBot(channel)
                }
            }
            return channel?.let { SFURoom(it) }
        }

        /**
         *  SFURoomを作成します。
         */
        @JvmStatic
        suspend fun create(name: String? = null, metadata: String? = null): SFURoom? {
            check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }
            if (SkyWayContext.findPlugin("sfu") == null) {
                SkyWayContext.registerPlugin(SFUBotPlugin())
            }
            val channel = Channel.create(name, metadata)
            channel?.let { SFUBot.createBot(it) }
            return channel?.let { SFURoom(it) }
        }

        /**
         *  SFURoomを探し、見つからなかった場合は作成します。
         */
        @JvmStatic
        suspend fun findOrCreate(name: String? = null, metadata: String? = null): SFURoom? {
            check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }
            if (SkyWayContext.findPlugin("sfu") == null) {
                SkyWayContext.registerPlugin(SFUBotPlugin())
            }
            val channel = Channel.findOrCreate(name, metadata)
            if (channel != null) {
                if (channel.bots.isEmpty()) {
                    SFUBot.createBot(channel)
                }
            }
            return channel?.let { SFURoom(it) }
        }
    }

    override val type: Type = Type.SFU
    internal val bot: SFUBot?
        get() {
            val bot = channel.bots.firstOrNull { it.subType == "sfu" }
            return if (bot == null) null else bot as SFUBot
        }
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        initEventHandler()
    }

    /**
     *  SFURoomに入室します。
     */
    override suspend fun join(memberInit: RoomMember.Init): LocalSFURoomMember? {
        val channelMemberInit = Member.Init(
            memberInit.name,
            memberInit.metadata,
            memberInit.keepAliveIntervalSec,
            memberInit.keepaliveIntervalGapSec,
            Member.Type.PERSON,
            "",
        )
        val localPerson = channel.join(channelMemberInit) ?: return null
        return createRoomMember(localPerson) as LocalSFURoomMember
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
        if (member.type == Member.Type.BOT) return
        val roomMember = createRoomMember(member)
        roomMember?.let { onMemberJoinedHandler?.invoke(it) }
        onMemberListChangedHandler?.invoke()
    }

    private fun onMemberLeft(member: Member) {
        if (member.type == Member.Type.BOT) return
        val roomMember = createRoomMember(member)
        roomMember?.let { onMemberLeftHandler?.invoke(it) }
        onMemberListChangedHandler?.invoke()
    }

    private fun onMemberMetadataUpdated(member: Member, metadata: String) {
        if (member.type == Member.Type.BOT) return
        val roomMember = createRoomMember(member)
        roomMember?.let { onMemberMetadataUpdatedHandler?.invoke(it, metadata) }
    }

    private fun onStreamPublished(publication: Publication) {
        scope.launch {
            mutex.withLock {
                if (publication.origin == null) return@launch
                // Pushing only relayed publications
                val roomPublication = createRoomPublication(publication)
                onStreamPublishedHandler?.invoke(roomPublication)
                onPublicationListChangedHandler?.invoke()
            }
        }
    }

    private fun onStreamUnpublished(publication: Publication) {
        if (publication.origin == null) return
        val roomPublication = createRoomPublication(publication)
        onStreamUnpublishedHandler?.invoke(roomPublication)
        onPublicationListChangedHandler?.invoke()
    }

    private fun onPublicationEnabled(publication: Publication) {
        if (publication.origin == null) return
        val roomPublication = createRoomPublication(publication)
        onPublicationEnabledHandler?.invoke(roomPublication)
    }

    private fun onPublicationDisabled(publication: Publication) {
        if (publication.origin == null) return
        val roomPublication = createRoomPublication(publication)
        onPublicationDisabledHandler?.invoke(roomPublication)
    }

    private fun onPublicationMetadataUpdated(publication: Publication, metadata: String) {
        val roomPublication = createRoomPublication(publication)
        onPublicationMetadataUpdatedHandler?.invoke(roomPublication, metadata)
    }

    private fun onPublicationSubscribed(subscription: Subscription) {
        if (subscription.subscriber.type == Member.Type.BOT) return
        val roomSubscription = createRoomSubscription(subscription)
        onPublicationSubscribedHandler?.invoke(roomSubscription)
        onSubscriptionListChangedHandler?.invoke()
    }

    private fun onPublicationUnsubscribed(subscription: Subscription) {
        if (subscription.subscriber.type == Member.Type.BOT) return
        val roomSubscription = createRoomSubscription(subscription)
        onPublicationUnsubscribedHandler?.invoke(roomSubscription)
        onSubscriptionListChangedHandler?.invoke()
    }
}
