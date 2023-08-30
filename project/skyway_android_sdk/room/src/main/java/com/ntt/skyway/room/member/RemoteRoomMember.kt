/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.member

import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.plugin.remotePerson.RemotePerson
import com.ntt.skyway.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RemoteRoomMemberの操作を行うクラス。
 */
open class RemoteRoomMember internal constructor(room: Room, internal val remoteMember: RemotePerson) :
    RoomMember(room, remoteMember) {

    /**
     *  常に[Member.Side.REMOTE]を返します。
     */
    override val side: Member.Side = Member.Side.REMOTE

    /**
     *  [com.ntt.skyway.room.RoomPublication]をsubscribeします。
     *  [Room.onPublicationSubscribedHandler]が発火します。
     */
    suspend fun subscribe(publicationId: String): Subscription? = withContext(Dispatchers.Default) {
        return@withContext remoteMember.subscribe(publicationId)
    }

    /**
     *  [com.ntt.skyway.room.RoomSubscription]をunsubscribeします。
     *  [Room.onPublicationUnsubscribedHandler]が発火します。
     */
    suspend fun unsubscribe(subscriptionsId: String): Boolean = withContext(Dispatchers.Default) {
        return@withContext remoteMember.unsubscribe(subscriptionsId)
    }

//    fun getStats(publication: RoomPublication): String {
//        var corePublication = publication.channelPublication
//        return if(corePublication.origin != null) {
//            // In the case of SFURoom, it's wrong to access remote_person_ because the real statistics
//            // are retrieved from bot Here is fetching bot or remote person
//            var remoteMember:RemoteMember  = corePublication.publisher as RemoteMember
//            remoteMember.getStats(corePublication.origin!!)
//        } else {
//            remoteMember.getStats(corePublication)
//        }
//    }

//    fun getStats(subscription: RoomSubscription): String {
//        var coreSubscription = subscription.channelSubscription
//        var remoteMember: RemoteMember = coreSubscription.publication.publisher as RemoteMember
//        return remoteMember.getStats(coreSubscription)
//    }

}
