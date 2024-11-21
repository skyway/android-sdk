package com.ntt.skyway.room

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito



class RoomSubscriptionTest {
    private lateinit var room: Room
    private lateinit var channel: Channel
    private lateinit var subscription: Subscription
    private lateinit var roomSubscription: RoomSubscription

    @Before
    fun setUp() {
        channel = Mockito.mock(Channel::class.java)
        room = P2PRoom(channel)
        subscription = Mockito.mock(Subscription::class.java)
        roomSubscription = RoomSubscription(room, subscription)
    }

    @Test
    fun getId() {
        assertEquals(subscription.id, roomSubscription.id)
    }

    @Test
    fun getContentType() {
        assertEquals(subscription.contentType, roomSubscription.contentType)
    }

    @Test
    fun getPublication() {
        val publication = Mockito.mock(Publication::class.java)
        Mockito.`when`(subscription.publication).thenReturn(publication)
        assertEquals(subscription.publication.id, roomSubscription.publication.id)
    }

    @Test
    fun getSubscriber() {
        val member = Mockito.mock(LocalPerson::class.java)
        Mockito.`when`(subscription.subscriber).thenReturn(member)
        assertEquals(member, roomSubscription.subscriber?.member)
    }

    @Test
    fun getState() {
        assertEquals(subscription.state, roomSubscription.state)
    }

    @Test
    fun getPreferredEncodingId() {
        assertEquals(subscription.preferredEncodingId, roomSubscription.preferredEncodingId)
    }

    @Test
    fun getStream() {
        assertEquals(subscription.stream, roomSubscription.stream)
    }

    @Test
    fun changePreferredEncoding(): Unit = runBlocking {
        val preferredEncodingId = "id"
        roomSubscription.changePreferredEncoding(preferredEncodingId)
        Mockito.verify(subscription, Mockito.times(1)).changePreferredEncoding(preferredEncodingId)
    }

    @Test
    fun cancel(): Unit = runBlocking {
        roomSubscription.cancel()
        Mockito.verify(subscription, Mockito.times(1)).cancel()
    }

    @Test
    fun getRoom() {
        assertEquals(room, roomSubscription.room)
    }

    @Test
    fun getPreferredEncodingId_returns_correct_value() {
        val encodingId = "testEncodingId"
        Mockito.`when`(subscription.preferredEncodingId).thenReturn(encodingId)
        assertEquals(encodingId, roomSubscription.preferredEncodingId)
    }


    @Test
    fun test_onConnectionStateChangedHandler() {
        val testState = "connected"
        var receivedState: String? = null
        roomSubscription.onConnectionStateChangedHandler = { state -> receivedState = state }
        roomSubscription.onConnectionStateChangedHandler?.invoke(testState)
        assertEquals(testState, receivedState)
    }

//    Need to handle invalidEncodingId on the Subscription class
    @Test
    fun changePreferredEncoding_handles_invalid_encoding_id() = runBlocking {
        val invalidEncodingId = ""
        roomSubscription.changePreferredEncoding(invalidEncodingId)
        Mockito.verify(subscription, Mockito.times(1)).changePreferredEncoding(invalidEncodingId)
    }


}
