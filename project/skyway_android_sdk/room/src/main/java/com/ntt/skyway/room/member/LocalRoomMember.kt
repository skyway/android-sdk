/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.member

import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription

/**
 *  LocalRoomMemberの操作を行うクラス。
 */
abstract class LocalRoomMember internal constructor(
    /**
     * このLocalRoomMemberが所属する[Room]。
     */
    room: Room,
    internal open val localPerson: LocalPerson
) : RoomMember(room, localPerson) {

    /**
     *  常に[Member.Side.LOCAL]を返します。
     */
    override val side: Member.Side = Member.Side.LOCAL

    /**
     * StreamをPublishした時に発火するハンドラ。
     */
    var onStreamPublishedHandler: ((publication: RoomPublication) -> Unit)? = null
        set(value) {
            field = value
            localPerson.onStreamPublishedHandler = {
                val roomPublication = room.createRoomPublication(it)
                value?.invoke(roomPublication)
            }
        }

    /**
     * StreamをUnpublishした時に発火するハンドラ。
     */
    var onStreamUnpublishedHandler: ((publication: RoomPublication) -> Unit)? = null
        set(value) {
            field = value
            localPerson.onStreamUnpublishedHandler = {
                val roomPublication = room.createRoomPublication(it)
                value?.invoke(roomPublication)
            }
        }

    /**
     * PublicationをSubscribeした時に発火するハンドラ。
     * Subscriptionにはまだstreamがsetされていない可能性があります。
     */
    var onPublicationSubscribedHandler: ((subscription: RoomSubscription) -> Unit)? = null
        set(value) {
            field = value
            localPerson.onPublicationSubscribedHandler = {
                val roomPublication = room.createRoomSubscription(it)
                value?.invoke(roomPublication)
            }
        }

    /**
     * PublicationをUnsubscribeした時に発火するハンドラ。
     */
    var onPublicationUnsubscribedHandler: ((subscription: RoomSubscription) -> Unit)? = null
        set(value) {
            field = value
            localPerson.onPublicationSubscribedHandler = {
                val roomPublication = room.createRoomSubscription(it)
                value?.invoke(roomPublication)
            }
        }

    /**
     *  [LocalStream]をpublishします。既にpublish中のStreamは指定することができません。
     *  [Room.onStreamPublishedHandler]が発火します。
     *
     *  @param localStream publishするStream。
     */
    open suspend fun publish(
        localStream: LocalStream,
        options: RoomPublication.Options? = null
    ): RoomPublication? {
        val channelPublication = localPerson.publish(localStream, options?.toCore())
        return channelPublication?.let { room.createRoomPublication(it) }
    }

    /**
     *  [LocalStream]をunpublishします。
     *  [Room.onStreamUnpublishedHandler]が発火します。
     */
    open suspend fun unpublish(publication: RoomPublication): Boolean {
        return localPerson.unpublish(publication.id)
    }

    /**
     *  [publication]をsubscribeします。
     *  [Room.onPublicationSubscribedHandler]が発火します。
     */
    suspend fun subscribe(
        publication: RoomPublication,
        options: RoomSubscription.Options? = null
    ): RoomSubscription? {
        return subscribe(publication.id, options)
    }

    suspend fun subscribe(
        publicationId: String,
        options: RoomSubscription.Options? = null
    ): RoomSubscription? {
        val subscription =
            localPerson.subscribe(publicationId, options?.toCore()) ?: return null
        return room.createRoomSubscription(subscription)
    }

    /**
     *  [RoomPublication]をsubscribeします。
     *  [Room.onPublicationUnsubscribedHandler]が発火します。
     */
    suspend fun unsubscribe(subscription: Subscription): Boolean {
        return unsubscribe(subscription.id)
    }

    suspend fun unsubscribe(subscriptionId: String): Boolean {
        return localPerson.unsubscribe(subscriptionId)
    }

}
