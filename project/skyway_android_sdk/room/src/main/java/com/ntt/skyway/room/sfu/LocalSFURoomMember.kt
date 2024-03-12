/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.sfu

import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import kotlinx.coroutines.sync.withLock

/**
 * LocalSFURoomMemberの操作を行うクラス。
 */
class LocalSFURoomMember internal constructor(
    private val sfuRoom: SFURoom,
    override val localPerson: LocalPerson
) : LocalRoomMember(sfuRoom, localPerson) {

    /**
     *  [LocalStream]をpublishします。
     *  [Room.onStreamPublishedHandler]が発火します。
     * @param options publish時のオプション。codecCapabilitiesを指定することはできません。
     */
    override suspend fun publish(
        localStream: LocalStream,
        options: RoomPublication.Options?
    ): RoomPublication? {
        sfuRoom.mutex.withLock {
            val channelOriginPublication =
                localPerson.publish(localStream, options?.toCore())
                    ?: return null
            val bot = sfuRoom.bot
            if (bot == null) {
                Logger.logE("SFUBot is not found")
                return null
            }
            val forwarding =
                bot.startForwarding(channelOriginPublication, options?.toConfigure()) ?: return null
            val relayingPublication = forwarding.relayingPublication
            return room.createRoomPublication(relayingPublication)
        }
    }

    /**
     *  [LocalStream]をunpublishします。
     *  [Room.onStreamUnpublishedHandler]が発火します。
     */
    override suspend fun unpublish(publication: RoomPublication): Boolean {
        val originPublication = publication.origin
        if (originPublication == null) {
            Logger.logE("The origin publication is not found ${publication.id}")
            return false
        }
        val bot = sfuRoom.bot
        if (bot == null) {
            Logger.logE("SFUBot is not found")
            return false
        }
        bot.forwardings.find { it.relayingPublication.id == publication.id }?.let {
            val result = bot.stopForwarding(it)
            if (!result) return false
        }

        return localPerson.unpublish(originPublication)
    }
}
