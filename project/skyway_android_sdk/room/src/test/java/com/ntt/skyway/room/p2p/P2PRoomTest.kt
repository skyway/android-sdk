package com.ntt.skyway.room.p2p

import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class P2PRoomTest {
    private lateinit var room: P2PRoom
    private lateinit var channel: Channel

    @Before
    fun setup() {
        channel = Mockito.mock(Channel::class.java)
        room = P2PRoom(channel)
    }

    @Test
    fun id() {
        assertEquals(channel.id, room.id)
    }

    @Test
    fun getType() {
        assertEquals(Room.Type.P2P, room.type)
    }

    @Test
    fun join(): Unit = runBlocking {
        val memberName = "test"
        val memberInit = Member.Init(name = memberName)

        val localPerson = Mockito.mock(LocalPerson::class.java)
        Mockito.`when`(channel.join(memberInit)).thenReturn(localPerson)

        val roomMemberInit = RoomMember.Init(name = memberName)
        val member = room.join(roomMemberInit)
        assertNotNull(member)
        assertEquals(localPerson, member?.localPerson)
    }
}
