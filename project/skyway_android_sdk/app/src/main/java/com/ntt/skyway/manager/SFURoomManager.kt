package com.ntt.skyway.manager

import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom


class SFURoomManager : Manager() {
    var sfuRoom: SFURoom? = null
    var localPerson: LocalSFURoomMember? = null

    override suspend fun findOrCreate(name: String?): Boolean {
        sfuRoom = SFURoom.findOrCreate(name)?:return false
        return true
    }

    override suspend fun find(name:String?,id:String?): Boolean {
        sfuRoom = SFURoom.find(name, id)?:return false
        return true
    }

    override suspend fun create(name: String?): Boolean {
        sfuRoom = SFURoom.create(name)?:return false
        return true
    }

    override suspend fun join(name: String, metadata: String?): Boolean {
        val memberInit = RoomMember.Init(name, metadata)
        localPerson = sfuRoom?.join(memberInit)?:return false
        return true
    }

    override suspend fun close(): Boolean {
        if (sfuRoom == null) {
            return false
        }
        return sfuRoom!!.close()
    }

    override suspend fun dispose(): Boolean {
        if (sfuRoom == null) {
            return false
        }
        sfuRoom?.dispose()
        return true
    }

    override fun toString(): String {
        return "SFURoom"
    }
}
