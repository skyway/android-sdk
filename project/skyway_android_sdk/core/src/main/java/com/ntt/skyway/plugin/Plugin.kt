package com.ntt.skyway.plugin

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember

interface Plugin {
    val name: String
    fun createRemoteMember(dto: Member.Dto): RemoteMember
}