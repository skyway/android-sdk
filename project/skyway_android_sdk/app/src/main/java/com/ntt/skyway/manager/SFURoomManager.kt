package com.ntt.skyway.manager

import androidx.lifecycle.MutableLiveData
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom


object SFURoomManager {
    var sfuRoom: SFURoom? = null
    var localPerson: LocalSFURoomMember? = null
    val membersLiveData = MutableLiveData<MutableList<RoomMember>>()

    fun updateMembersList() {
        if (sfuRoom == null) return
        membersLiveData.value = this.sfuRoom?.members?.toMutableList()
    }
}
