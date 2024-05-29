package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.LocalPersonImpl
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.plugin.unknown.UnknownPlugin

internal class Factory(private val channel: ChannelImpl) {
    companion object {
        fun createChannel(channelJson: String): Channel {
            val dto = Gson().fromJson(channelJson, JsonObject::class.java)
            val channel = ChannelImpl(
                id = dto.get("id").asString,
                name = dto.get("name").asString.takeUnless { it.isBlank() },
                nativePointer = dto.get("nativePointer").asLong
            )

            dto.get("members").asJsonArray.forEach {
                channel.repository.addMemberIfNeeded(it.toString())
            }

            dto.get("publications").asJsonArray.forEach {
                channel.repository.addPublicationIfNeeded(it.toString(), null)
            }

            dto.get("subscriptions").asJsonArray.forEach {
                channel.repository.addSubscriptionIfNeeded(it.toString())
            }

            return channel
        }
    }

    fun createMember(memberJson: String): Member {
        val dto = Gson().fromJson(memberJson, JsonObject::class.java)
        return if (dto.get("side").asString == "local") {
            createLocalPerson(memberJson)
        } else {
            createRemoteMember(memberJson)
        }
    }

    private fun createLocalPerson(memberJson: String): LocalPerson {
        val dto = Gson().fromJson(memberJson, JsonObject::class.java)
        return LocalPersonImpl(
            Member.Dto(
                channel = channel,
                id = dto.get("id").asString,
                name = dto.get("name").asString.takeUnless { it.isBlank() },
                nativePointer = dto.get("nativePointer").asLong,
            ),
            repository = channel.repository
        )
    }

    private fun createRemoteMember(memberJson: String): RemoteMember {
        val dto = Gson().fromJson(memberJson, JsonObject::class.java)
        val memberDto = Member.Dto(
            channel = channel,
            id = dto.get("id").asString,
            name = dto.get("name").asString.takeUnless { it.isBlank() },
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

        val publisherJson = Gson().toJson(dto.get("publisher").asJsonObject)
        val publisher = channel.repository.addMemberIfNeeded(publisherJson)

        return PublicationImpl(
            channel = channel,
            id = dto.get("id").asString,
            publisher = publisher,
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

        val subscriberJson = Gson().toJson(dto.get("subscriber").asJsonObject)
        val subscriber = channel.repository.addMemberIfNeeded(subscriberJson)

        val publicationJson = Gson().toJson(dto.get("publication").asJsonObject)
        val publication = channel.repository.addPublicationIfNeeded(publicationJson, null)

        val contentType = Stream.ContentType.fromString(dto.get("contentType").asString)
        val stream = if (dto.get("stream") != null) {
            Factory.createRemoteStream(contentType, dto.get("stream").toString())
        } else null

        return SubscriptionImpl(
            channel = channel,
            id = dto.get("id").asString,
            subscriber = subscriber,
            publication = publication,
            contentType = contentType,
            nativePointer = dto.get("nativePointer").asLong,
            internalStream = stream
        )
    }
}
