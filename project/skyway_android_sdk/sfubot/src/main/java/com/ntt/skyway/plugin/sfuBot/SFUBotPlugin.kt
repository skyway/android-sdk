/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.plugin.sfuBot

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.plugin.Plugin

/**
 * SFUBotをForwardingします。
 */
class SFUBotPlugin : Plugin {
    override val name: String
        get() = "sfu"

    override fun createRemoteMember(dto: Member.Dto): RemoteMember {
        return SFUBot(dto)
    }
}