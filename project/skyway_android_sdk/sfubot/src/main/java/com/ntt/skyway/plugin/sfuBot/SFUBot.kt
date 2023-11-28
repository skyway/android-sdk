/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.plugin.sfuBot

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.ChannelImpl
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMemberImpl
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SFUBot internal constructor(dto: Member.Dto) : RemoteMemberImpl(dto) {
    companion object {
        /**
         * SFUBotを作成します。
         */
        @JvmStatic
        suspend fun createBot(channel: Channel): SFUBot? = withContext(Dispatchers.Default) {
            val sfuBotJson =
                nativeCreateBot((channel as ChannelImpl).nativePointer) ?: return@withContext null
            val dto = Gson().fromJson(sfuBotJson, JsonObject::class.java)
            val sfuBotDto = Member.Dto(
                channel = channel,
                id = dto.get("id").asString,
                name = dto.get("name").asString,
                nativePointer = dto.get("nativePointer").asLong,
            )
            return@withContext SFUBot(sfuBotDto)
        }

        @JvmStatic
        private external fun nativeCreateBot(channelPtr: Long): String?
    }

    /**
     * 常に[Member.Type.BOT]を返します。
     */
    override val type: Member.Type = Member.Type.BOT

    /**
     * 常に"sfu"を返します。
     */
    override val subType: String = "sfu"

    /**
     * このSFUBotのForwarding一覧。
     */
    val forwardings
        get() = _forwardings.toSet()

    private val _forwardings = mutableListOf<Forwarding>()

    /**
     * PublicationをForwardingします。
     * @param publication forwarding対象のPublication。codecCapabilitiesを指定することはできません。
     */
    suspend fun startForwarding(
        publication: Publication,
        configure: Forwarding.Configure? = null
    ): Forwarding? =
        withContext(Dispatchers.Default) {
            val forwardingJson = nativeStartForwarding(
                nativePointer,
                publication.nativePointer,
                configure?.maxSubscribers
            )
                ?: return@withContext null
            val forwarding = Forwarding.create(channel, publication, forwardingJson)
            _forwardings.add(forwarding)
            return@withContext forwarding
        }

    /**
     * Forwarding停止します。
     */
    suspend fun stopForwarding(forwarding: Forwarding): Boolean = withContext(Dispatchers.Default) {
        _forwardings.remove(forwarding)
        return@withContext nativeStopForwarding(nativePointer, forwarding.nativePointer)
    }

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

    private external fun nativeStartForwarding(
        ptr: Long,
        publicationPtr: Long,
        maxSubscribers: Int?
    ): String?

    private external fun nativeStopForwarding(ptr: Long, forwardingPtr: Long): Boolean
}
