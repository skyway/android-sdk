package com.ntt.skyway.plugin.remotePerson

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.plugin.Plugin

/**
 *  SkyWayでRemotePersonを扱えるようにするためのクラス。
 */
class RemotePersonPlugin: Plugin {
    override val name: String
        get() = "Person"

    override fun createRemoteMember(dto: Member.Dto): RemoteMember {
        return RemotePerson(dto)
    }
}