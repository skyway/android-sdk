package com.ntt.skyway.manager

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.plugin.sfuBot.SFUBot

class ChannelManager : Manager() {
    var channel: Channel? = null
    var localPerson: LocalPerson? = null
    var sfuBot: SFUBot? = null

    override suspend fun findOrCreate(name: String?): Boolean {
        channel = Channel.findOrCreate(name,"Create by Android")?:return false
        return true
    }

    override suspend fun find(name:String?,id:String?): Boolean {
        channel = Channel.find(name, id)?:return false
        return true
    }

    override suspend fun create(name: String?): Boolean {
        channel = Channel.create(name,"Create by Android")?:return false
        return true
    }

    override suspend fun join(name: String, metadata: String?): Boolean {
        val memberInit = Member.Init(name, metadata/* , keepaliveIntervalGapSec = 120 */)
        localPerson = channel?.join(memberInit)?:return false
        return true
    }

    override suspend fun close(): Boolean {
        if(channel == null) {
            return false
        }
        return channel!!.close()
    }

    override suspend fun dispose(): Boolean {
        if(channel == null) {
            return false
        }
        channel!!.dispose()
        return true
    }

    override fun toString(): String {
        return "Channel"
    }
}