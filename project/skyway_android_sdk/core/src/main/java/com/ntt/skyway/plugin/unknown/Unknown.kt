package com.ntt.skyway.plugin.unknown

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMemberImpl

class Unknown  internal constructor(dto: Member.Dto) : RemoteMemberImpl(dto) {
    override val type: Member.Type = Member.Type.UNKNOWN
    override val subType: String = "unknown"
}
