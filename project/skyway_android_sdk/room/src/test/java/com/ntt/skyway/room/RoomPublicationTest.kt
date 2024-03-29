package com.ntt.skyway.room

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class RoomPublicationTest {
    private lateinit var room: Room
    private lateinit var channel: Channel
    private lateinit var publication: Publication
    private lateinit var roomPublication: RoomPublication

    @Before
    fun setUp() {
        channel = Mockito.mock(Channel::class.java)
        room = P2PRoom(channel)
        publication = Mockito.mock(Publication::class.java)
        roomPublication = RoomPublication(room, publication)
    }

    @Test
    fun getId() {
        assertEquals(publication.id, roomPublication.id)
    }

    @Test
    fun getContentType() {
        assertEquals(publication.contentType, roomPublication.contentType)
    }

    @Test
    fun getMetadata() {
        assertEquals(publication.metadata, roomPublication.metadata)
    }

    @Test
    fun getPublisher() {
        val member = Mockito.mock(LocalPerson::class.java)
        Mockito.`when`(publication.publisher).thenReturn(member)
        assertNotNull(roomPublication.publisher)
        assertEquals(member, roomPublication.publisher?.member)
    }

    @Test
    fun getSubscriptions() {
        val subscription = Mockito.mock(Subscription::class.java)
        Mockito.`when`(subscription.publication).thenReturn(publication)
        Mockito.`when`(channel.subscriptions).thenReturn(setOf(subscription))
        val subscriptions = roomPublication.subscriptions
        assertEquals(1, subscriptions.size)
    }

    @Test
    fun getState() {
        assertEquals(publication.state, roomPublication.state)
    }

    @Test
    fun getCodecCapabilities() {
        assertEquals(publication.codecCapabilities, roomPublication.codecCapabilities)
    }

    @Test
    fun getEncodings() {
        assertEquals(publication.encodings, roomPublication.encodings)
    }

    @Test
    fun getStream() {
        assertEquals(publication.stream, roomPublication.stream)
    }

    @Test
    fun `getOrigin$room_debug`() {
        assertEquals(publication.origin, roomPublication.origin)
    }

    @Test
    fun cancel(): Unit = runBlocking {
        roomPublication.cancel()
        Mockito.verify(publication, Mockito.times(1)).cancel()
    }

    @Test
    fun enable(): Unit = runBlocking {
        roomPublication.enable()
        Mockito.verify(publication, Mockito.times(1)).enable()
    }

    @Test
    fun disable(): Unit = runBlocking {
        roomPublication.disable()
        Mockito.verify(publication, Mockito.times(1)).disable()
    }

    @Test
    fun updateEncodings() {
        val encodings = mutableListOf(Encoding("id"))
        roomPublication.updateEncodings(encodings)
        Mockito.verify(publication, Mockito.times(1)).updateEncodings(encodings)
    }

    @Test
    fun replaceStream() {
        val localStream = Mockito.mock(LocalStream::class.java)
        roomPublication.replaceStream(localStream)
        Mockito.verify(publication, Mockito.times(1)).replaceStream(localStream)
    }

    @Test
    fun getRoom() {
        assertEquals(room, roomPublication.room)
    }
}
