package com.ntt.skyway.room

import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class FactoryTest {

    private lateinit var factory: Factory
    private lateinit var room: Room

    @Before
    fun setup() {
        room = Mockito.mock(Room::class.java)
        Mockito.`when`(room.type).thenReturn(Room.Type.P2P)
        factory = Factory(room)
    }

    @Test
    fun createLocalRoomMember() {
        val localPerson = Mockito.mock(LocalPerson::class.java)
        val localRoomMember = factory.createLocalRoomMember(localPerson = localPerson)
        assertEquals(localPerson, localRoomMember.localPerson)
    }

// [TODO] Can not test because RemotePerson is impl class.
//    @Test
//    fun createRemoteRoomMember() {
//        val remotePerson = Mockito.mock(RemotePerson::class.java)
//        val remoteRoomMember = factory.createRemoteRoomMember(remotePerson)
//        assertEquals(remoteRoomMember.remoteMember, remotePerson)
//    }

    @Test
    fun createRoomPublication() {
        val publication = Mockito.mock(Publication::class.java)
        Mockito.`when`(publication.id).thenReturn("id")
        val roomPublication = factory.createRoomPublication(publication)
        assertEquals(publication.id, roomPublication.id)
    }

    @Test
    fun createRoomSubscription() {
        val subscription = Mockito.mock(Subscription::class.java)
        Mockito.`when`(subscription.id).thenReturn("id")
        val roomSubscription = factory.createRoomSubscription(subscription)
        assertEquals(subscription.id, roomSubscription.id)
    }
}
