package com.ntt.skyway.room.p2p

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.room.RoomPublication
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class LocalP2PRoomMemberTest {
    private lateinit var room: P2PRoom
    private lateinit var channel: Channel
    private lateinit var localPerson: LocalPerson
    private lateinit var localP2PRoomMember: LocalP2PRoomMember

    @Before
    fun setUp() {
        channel = Mockito.mock(Channel::class.java)
        room = P2PRoom(channel)
        localPerson = Mockito.mock(LocalPerson::class.java)
        localP2PRoomMember = LocalP2PRoomMember(room, localPerson)
    }

    @Test
    fun `getLocalPerson$room_debug`() {
        assertEquals(localPerson, localP2PRoomMember.localPerson)
    }

    @Test
    fun getRoom() {
        assertEquals(room, localP2PRoomMember.room)
    }

    @Test
    fun getId() {
        assertEquals(localPerson.id, localP2PRoomMember.id)
    }

    @Test
    fun getName() {
        assertEquals(localPerson.name, localP2PRoomMember.name)
    }

    @Test
    fun getMetadata() {
        assertEquals(localPerson.metadata, localP2PRoomMember.metadata)
    }

    @Test
    fun getSide() {
        assertEquals(Member.Side.LOCAL, localP2PRoomMember.side)
    }

    @Test
    fun getState() {
        assertEquals(localPerson.state, localP2PRoomMember.state)
    }

    @Test
    fun publications() {

    }

    @Test
    fun subscriptions() {

    }

    @Test
    fun updateMetadata(): Unit = runBlocking {
        val metadata = "metadata"
        localP2PRoomMember.updateMetadata(metadata)
        Mockito.verify(localPerson, Mockito.times(1)).updateMetadata(metadata)
    }

    @Test
    fun leave(): Unit = runBlocking {
        localP2PRoomMember.leave()
        Mockito.verify(localPerson, Mockito.times(1)).leave()
    }

    @Test
    fun publish(): Unit = runBlocking {
        val localStream = Mockito.mock(LocalStream::class.java)
        localP2PRoomMember.publish(localStream)
        Mockito.verify(localPerson, Mockito.times(1)).publish(localStream)
    }

    @Test
    fun publish_fails_when_LocalPerson_Returns_Null() = runBlocking {
        val localStream = Mockito.mock(LocalStream::class.java)
        Mockito.`when`(localPerson.publish(localStream, null)).thenReturn(null)

        val result = localP2PRoomMember.publish(localStream)
        Assert.assertNull(result)
    }


    @Test
    fun unpublish(): Unit = runBlocking {
        val publication = Mockito.mock(Publication::class.java)
        val publicationId = "id"
        Mockito.`when`(publication.id).thenReturn(publicationId)
        val roomPublication = RoomPublication(room, publication)
        localP2PRoomMember.unpublish(roomPublication)
        Mockito.verify(localPerson, Mockito.times(1)).unpublish(publicationId)
    }

    @Test
    fun subscribe(): Unit = runBlocking {
        val publicationId = "id"
        localP2PRoomMember.subscribe(publicationId)
        Mockito.verify(localPerson, Mockito.times(1)).subscribe(publicationId)
    }

    @Test
    fun unsubscribe(): Unit = runBlocking {
        val subscriptionId = "id"
        localP2PRoomMember.unsubscribe(subscriptionId)
        Mockito.verify(localPerson, Mockito.times(1)).unsubscribe(subscriptionId)
    }
}
