package com.example.regressiontest.manager

import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.p2p.P2PRoom

class RoomManager(
    var room: P2PRoom? = null,
    var localPerson: LocalRoomMember? = null
)
