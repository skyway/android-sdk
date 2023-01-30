/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.plugin.sfuBot

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Channel

/**
 * Forwardingの操作を行うクラス。
 */
class Forwarding internal constructor(
    /**
     * このForwardingが所属するChannel。
     */
    val channel: Channel,
    /**
     * このForwardingのID。
     */
    val id: String,
    /**
     * このForwardingの設定。
     */
    val configure: Configure,
    /**
     * このForwardingに対するoriginのPublication。
     */
    val originPublication: Publication,
    /**
     * このForwardingに対するforwarding後のPublication。
     */
    val relayingPublication: Publication,
    internal val nativePointer: Long
) {

    internal companion object {
        fun create(
            channel: Channel,
            originPublication: Publication,
            forwardingJson: String
        ): Forwarding {
            val dto = Gson().fromJson(forwardingJson, JsonObject::class.java)

            val relayingPublicationId = dto.get("relayingPublicationId").asString
            val relayingPublication =
                channel.publications.find { publication -> publication.origin == originPublication }
                    ?: throw IllegalStateException("RelayingPublication(${relayingPublicationId}) was not found")

            return Forwarding(
                channel = channel,
                id = dto.get("id").asString,
                configure = Configure(dto.get("configure").asJsonObject.get("maxSubscribers").asInt),
                originPublication = originPublication,
                relayingPublication = relayingPublication,
                nativePointer = dto.get("nativePointer").asLong
            )
        }
    }

    /**
     * Forwardingの設定。
     */
    data class Configure(
        /**
         * 最大Subscriber数。
         */
        val maxSubscribers: Int
    )
}
