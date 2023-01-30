package com.ntt.skyway.manager

import androidx.lifecycle.MutableLiveData
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.plugin.sfuBot.SFUBot

object ChannelManager {
    var channel: Channel? = null
    val members = arrayListOf<Member>()
    var localPerson: LocalPerson? = null
    var sfuBot: SFUBot? = null
    val membersLiveData = MutableLiveData<MutableList<Member>>()

    fun update() {
        this.members.clear()
        membersLiveData.postValue(this.channel?.members?.toMutableList())
    }
}