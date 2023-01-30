package com.ntt.skyway.room

import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.plugin.remotePerson.RemotePerson
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RemoteRoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom

internal class Factory(private val room: Room) {
    fun createLocalRoomMember(room: Room, localPerson: LocalPerson): LocalRoomMember {
        return when (room.type) {
            Room.Type.P2P -> LocalP2PRoomMember(room, localPerson)
            Room.Type.SFU -> LocalSFURoomMember(room as SFURoom, localPerson)
        }
    }

    fun createRemoteRoomMember(remotePerson: RemotePerson): RemoteRoomMember {
        return RemoteRoomMember(room, remotePerson)
    }

    fun createRoomPublication(channelPublication: Publication): RoomPublication {
        return RoomPublication(room, channelPublication)
    }

    fun createRoomSubscription(channelSubscription: Subscription): RoomSubscription {
        return RoomSubscription(room, channelSubscription)
    }
}
