/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.member

import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.plugin.remotePerson.RemotePerson
import com.ntt.skyway.room.Room

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
    suspend fun subscribe(publicationId: String): Subscription? {
        return remoteMember.subscribe(publicationId)
    }

    /**
     *  [com.ntt.skyway.room.RoomSubscription]をunsubscribeします。
     *  [Room.onPublicationUnsubscribedHandler]が発火します。
     */
    suspend fun unsubscribe(subscriptionsId: String): Boolean {
        return remoteMember.unsubscribe(subscriptionsId)
    }
}
