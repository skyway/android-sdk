package com.ntt.skyway.manager

import androidx.lifecycle.MutableLiveData
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom


object RoomManager {
    var room: P2PRoom? = null
    var localPerson: LocalP2PRoomMember? = null
    val membersLiveData = MutableLiveData<MutableList<RoomMember>>()

    fun updateMembersList() {
        if (room == null) return
        membersLiveData.value = this.room?.members?.toMutableList()
    }
}
