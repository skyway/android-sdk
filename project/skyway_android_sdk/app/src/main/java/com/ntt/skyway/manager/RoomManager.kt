package com.ntt.skyway.manager

import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom


class RoomManager : Manager() {
    var room: P2PRoom? = null
    var localPerson: LocalP2PRoomMember? = null

    override suspend fun findOrCreate(name: String?): Boolean {
        room = P2PRoom.findOrCreate(name)?:return false
        return true
    }

    override suspend fun find(name:String?,id:String?): Boolean {
        room = P2PRoom.find(name, id)?:return false
        return true
    }

    override suspend fun create(name: String?): Boolean {
        room = P2PRoom.create(name)?:return false
        return true
    }

    override suspend fun join(name: String, metadata: String?): Boolean {
        val memberInit = RoomMember.Init(name, metadata)
        localPerson = room?.join(memberInit)?:return false
        if (localPerson == null) return false
        return true
    }

    override suspend fun close(): Boolean {
        if (room == null) {
            return false
        }
        return room!!.close()
    }

    override suspend fun dispose(): Boolean {
        if (room == null) {
            return false
        }
        room?.dispose()
        return true
    }

    override fun toString(): String {
        return "P2PRoom"
    }
}
