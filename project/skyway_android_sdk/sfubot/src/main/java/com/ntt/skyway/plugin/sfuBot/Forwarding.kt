/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.plugin.sfuBot

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Channel
import kotlinx.coroutines.*

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
            val CreateRetryCount = 10
            val RetryDelayTime = 100L

            val dto = Gson().fromJson(forwardingJson, JsonObject::class.java)

            val relayingPublicationId = dto.get("relayingPublicationId").asString

            var publication: Publication? = null
            runBlocking {
                repeat(CreateRetryCount) {
                    publication =
                        channel.publications.find { publication -> publication.id == relayingPublicationId }
                    if (publication != null) return@runBlocking;
                    delay(RetryDelayTime)
                }
            }
            if(publication == null) {
                throw IllegalStateException("RelayingPublication(${relayingPublicationId}) was not found")
            }
            return Forwarding(
                channel = channel,
                id = dto.get("id").asString,
                configure = Configure(dto.get("configure").asJsonObject.get("maxSubscribers").asInt),
                originPublication = originPublication,
                relayingPublication = publication!!,
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
         * 最大値は99です。
         */
        val maxSubscribers: Int
    )
}
