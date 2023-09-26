package com.ntt.skyway.plugin.unknown

import com.ntt.skyway.core.channel.member.RemoteMember

class Unknown  internal constructor(dto: Dto) : RemoteMember(dto) {
    override val type: Type = Type.UNKNOWN
    override val subType: String = "unknown"
}
