package com.ntt.skyway.room.sfu

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class LocalSFURoomMemberTest {
    private lateinit var room: SFURoom
    private lateinit var channel: Channel
    private lateinit var localPerson: LocalPerson
    private lateinit var localSFURoomMember: LocalSFURoomMember

    @Before
    fun setUp() {
        channel = Mockito.mock(Channel::class.java)
        room = SFURoom(channel)
        localPerson = Mockito.mock(LocalPerson::class.java)
        localSFURoomMember = LocalSFURoomMember(room, localPerson)
    }

    @Test
    fun `getLocalPerson$room_debug`() {
        Assert.assertEquals(localPerson, localSFURoomMember.localPerson)
    }

    @Test
    fun getRoom() {
        Assert.assertEquals(room, localSFURoomMember.room)
    }

    @Test
    fun getId() {
        Assert.assertEquals(localPerson.id, localSFURoomMember.id)
    }

    @Test
    fun getName() {
        Assert.assertEquals(localPerson.name, localSFURoomMember.name)
    }

    @Test
    fun getMetadata() {
        Assert.assertEquals(localPerson.metadata, localSFURoomMember.metadata)
    }

    @Test
    fun getSide() {
        Assert.assertEquals(Member.Side.LOCAL, localSFURoomMember.side)
    }

    @Test
    fun getState() {
        Assert.assertEquals(localPerson.state, localSFURoomMember.state)
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
        localSFURoomMember.updateMetadata(metadata)
        Mockito.verify(localPerson, Mockito.times(1)).updateMetadata(metadata)
    }

    @Test
    fun leave(): Unit = runBlocking {
        localSFURoomMember.leave()
        Mockito.verify(localPerson, Mockito.times(1)).leave()
    }

    @Test
    fun publish(): Unit = runBlocking {
        val localStream = Mockito.mock(LocalStream::class.java)
        localSFURoomMember.publish(localStream)
        Mockito.verify(localPerson, Mockito.times(1)).publish(localStream)
    }

// Can not test because SFUBot is impl class
//    @Test
//    fun unpublish(): Unit = runBlocking {
//        val publication = Mockito.mock(Publication::class.java)
//        val origin = Mockito.mock(Publication::class.java)
//        Mockito.`when`(publication.origin).thenReturn(origin)
//        val roomPublication = RoomPublication(room, publication)
//        localSFURoomMember.unpublish(roomPublication)
//        Mockito.verify(localPerson, Mockito.times(1)).unpublish(origin)
//    }

    @Test
    fun subscribe(): Unit = runBlocking {
        val publicationId = "id"
        localSFURoomMember.subscribe(publicationId)
        Mockito.verify(localPerson, Mockito.times(1)).subscribe(publicationId)
    }

    @Test
    fun unsubscribe(): Unit = runBlocking {
        val subscriptionId = "id"
        localSFURoomMember.unsubscribe(subscriptionId)
        Mockito.verify(localPerson, Mockito.times(1)).unsubscribe(subscriptionId)
    }
}
