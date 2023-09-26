package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.LocalPersonImpl
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.plugin.unknown.UnknownPlugin

internal class Factory(private val channel: ChannelImpl) {
    companion object {
        fun createChannel(channelJson: String): Channel {
            val dto = Gson().fromJson(channelJson, JsonObject::class.java)
            val channel = ChannelImpl(
                id = dto.get("id").asString,
                name = dto.get("name").asString,
                nativePointer = dto.get("nativePointer").asLong
            )

            dto.get("members").asJsonArray.forEach {
                channel.repository.addRemoteMemberIfNeeded(it.toString())
            }

            dto.get("publications").asJsonArray.forEach {
                channel.repository.addRemotePublicationIfNeeded(it.toString())
            }

            dto.get("subscriptions").asJsonArray.forEach {
                channel.repository.addRemoteSubscriptionIfNeeded(it.toString())
            }

            return channel
        }
    }

    fun createLocalPerson(memberJson: String): LocalPerson {
        val dto = Gson().fromJson(memberJson, JsonObject::class.java)
        return LocalPersonImpl(
            Member.Dto(
                channel = channel,
                id = dto.get("id").asString,
                name = dto.get("name").asString,
                nativePointer = dto.get("nativePointer").asLong,
            ),
            repository = channel.repository
        )
    }

    fun createRemoteMember(memberJson: String): RemoteMember {
        val dto = Gson().fromJson(memberJson, JsonObject::class.java)
        val memberDto = Member.Dto(
            channel = channel,
            id = dto.get("id").asString,
            name = dto.get("name").asString,
            nativePointer = dto.get("nativePointer").asLong,
        )
        val subtype = dto.get("subtype").asString
        val plugin = SkyWayContext.findPlugin(subtype) ?: UnknownPlugin

        if (plugin.name == UnknownPlugin.name) {
            Logger.logI("Plugin($subtype) is not found. Use UnknownPlugin instead.")
        }

        return plugin.createRemoteMember(memberDto)
    }

    fun createPublication(publicationJson: String, stream: LocalStream?): Publication {
        val dto = Gson().fromJson(publicationJson, JsonObject::class.java)

        return PublicationImpl(
            channel = channel,
            id = dto.get("id").asString,
            publisherId = dto.get("publisherId").asString,
            contentType = Stream.ContentType.fromString(dto.get("contentType").asString),
            originId = dto.get("originId").asString,
            codecCapabilities = Codec.fromJsonArray(dto.get("codecCapabilities").asJsonArray),
            nativePointer = dto.get("nativePointer").asLong,
            internalStream = stream,
            repository = channel.repository,
        )
    }

    fun createSubscription(subscriptionJson: String): Subscription {
        val dto = Gson().fromJson(subscriptionJson, JsonObject::class.java)
        val contentType = Stream.ContentType.fromString(dto.get("contentType").asString)
        val stream = if (dto.get("stream") != null) {
            Factory.createRemoteStream(contentType, dto.get("stream").toString())
        } else null

        return SubscriptionImpl(
            channel = channel,
            id = dto.get("id").asString,
            subscriberId = dto.get("subscriberId").asString,
            publicationId = dto.get("publicationId").asString,
            contentType = contentType,
            nativePointer = dto.get("nativePointer").asLong,
            internalStream = stream,
            repository = channel.repository
        )
    }
}
