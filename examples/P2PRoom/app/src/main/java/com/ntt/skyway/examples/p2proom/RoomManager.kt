package com.ntt.skyway.examples.p2proom

import androidx.lifecycle.MutableLiveData
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom


object RoomManager {
    var room: P2PRoom? = null
    var localPerson: LocalP2PRoomMember? = null
    var isSkywayContextSetup = false
}
