package com.ntt.skyway.core.channel

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember

internal class Factory(private val channel: Channel) {
    companion object {
        fun createChannel(channelJson: String): Channel {
            val dto = Gson().fromJson(channelJson, JsonObject::class.java)
            val channel = Channel(
                id = dto.get("id").asString,
                name = dto.get("name").asString,
                nativePointer = dto.get("nativePointer").asLong
            )

            dto.get("members").asJsonArray.forEach {
                channel.addRemoteMemberIfNeeded(it.toString())
            }

            dto.get("publications").asJsonArray.forEach {
                channel.addRemotePublicationIfNeeded(it.toString())
            }

            dto.get("subscriptions").asJsonArray.forEach {
                channel.addRemoteSubscriptionIfNeeded(it.toString())
            }

            return channel
        }
    }

    fun createLocalPerson(memberJson: String): LocalPerson {
        val dto = Gson().fromJson(memberJson, JsonObject::class.java)
        return LocalPerson(
            Member.Dto(
                channel = channel,
                id = dto.get("id").asString,
                name = dto.get("name").asString,
                nativePointer = dto.get("nativePointer").asLong
            )
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
        val plugin = SkyWayContext.findPlugin(subtype)

        checkNotNull(plugin) {
            return@checkNotNull "Plugin($subtype) is not found"
        }

        return plugin.createRemoteMember(memberDto)
    }

    fun createPublication(publicationJson: String, stream: Stream?): Publication {
        val dto = Gson().fromJson(publicationJson, JsonObject::class.java)
        val origin = if (dto.get("originId").asString == "") null else channel.findPublication(
            dto.get(
                "originId"
            ).asString
        )

        val publisherId = dto.get("publisherId").asString
        val publisher =
            channel.findMember(publisherId)
                ?: throw IllegalStateException("Publisher(${publisherId}) was not found")

        return Publication(
            channel = channel,
            id = dto.get("id").asString,
            contentType = Stream.ContentType.fromString(dto.get("contentType").asString),
            publisher = publisher,
            origin = origin,
            codecCapabilities = Codec.fromJsonArray(dto.get("codecCapabilities").asJsonArray),
            nativePointer = dto.get("nativePointer").asLong,
            internalStream = stream
        )
    }

    fun createSubscription(subscriptionJson: String): Subscription {
        val dto = Gson().fromJson(subscriptionJson, JsonObject::class.java)
        val contentType = Stream.ContentType.fromString(dto.get("contentType").asString)
        val stream = if (dto.get("stream") != null) {
            Factory.createRemoteStream(contentType, dto.get("stream").toString())
        } else null

        val subscriberId = dto.get("subscriberId").asString
        val subscriber = channel.findMember(subscriberId)
            ?: throw IllegalStateException("Subscriber(${subscriberId}) was not found")

        val publicationId = dto.get("publicationId").asString
        val publication = channel.findPublication(publicationId)
            ?: throw IllegalStateException("Publication(${publicationId}) was not found")

        return Subscription(
            channel = channel,
            id = dto.get("id").asString,
            contentType = contentType,
            subscriber = subscriber,
            publication = publication,
            nativePointer = dto.get("nativePointer").asLong,
            stream = stream
        )
    }
}
