package com.ntt.skyway.plugin.unknown

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMemberImpl

class Unknown  internal constructor(dto: Member.Dto) : RemoteMemberImpl(dto) {
    override val type: Member.Type = Member.Type.UNKNOWN
    override val subType: String = "unknown"

    protected override fun onLeft() {
        super.onLeft()
    }

    protected override fun onMetadataUpdated(metadata: String) {
        super.onMetadataUpdated(metadata)
    }

    protected override fun onPublicationListChanged() {
        super.onPublicationListChanged()
    }

    protected override fun onSubscriptionListChanged() {
        super.onSubscriptionListChanged()
    }
}
