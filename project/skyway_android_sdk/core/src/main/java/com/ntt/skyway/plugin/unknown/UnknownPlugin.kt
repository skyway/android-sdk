package com.ntt.skyway.plugin.unknown

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.plugin.Plugin

object UnknownPlugin : Plugin {
    override val name: String
        get() = "Unknown"

    override fun createRemoteMember(dto: Member.Dto): RemoteMember {
        return Unknown(dto)
    }
}
