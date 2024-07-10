package com.ntt.skyway.room.sfu

import android.Manifest
import android.os.ConditionVariable
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID


class SFURoomTest {
    private val tag = this.javaClass.simpleName

    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Before
    fun setup() = runBlocking {
        TestUtil.setupSkyway()
    }

    @After
    fun tearDown() {
        Log.d(tag, "SkyWayContext.dispose()")
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

    @Test
    fun create() = runBlocking {
        val room = SFURoom.create()
        assertNotNull(room)
        assertNotNull(room?.bot)
    }

    @Test
    fun createWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val room = SFURoom.create(name, metadata)
        assertNotNull(room)
        assertEquals(room?.name, name)
        assertEquals(room?.metadata, metadata)
    }

    @Test
    fun create_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room1 = SFURoom.create(name)
        assertNotNull(room1)
        val room2 = SFURoom.create(name)
        assertNull(room2)
    }

    @Test
    fun findOrCreate() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.findOrCreate(name)
        assertNotNull(room)
    }

    @Test
    fun findOrCreate_WithAlreadyCreated() = runBlocking {
        val name = UUID.randomUUID().toString()
        SFURoom.create(name)
        val room = SFURoom.findOrCreate(name)
        assertNotNull(room)
    }

    @Test
    fun find() = runBlocking {
        val room1 = SFURoom.create()
        assertNotNull(room1)

        val room2 = SFURoom.find(id = room1?.id)
        assertNotNull(room2)
    }

    @Test
    fun find_ShouldReturnNullWithNotExistRoom() = runBlocking {
        val id = UUID.randomUUID().toString()
        val room2 = SFURoom.find(id = id)
        assertNull(room2)
    }

    @Test
    fun findByName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room1 = SFURoom.create(name)
        assertNotNull(room1)

        val room2 = SFURoom.find(room1?.name)
        assertNotNull(room2)
    }

    @Test
    fun findByName_ShouldReturnNullWithNotExistRoom() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room2 = SFURoom.find(name)
        assertNull(room2)
    }

    @Test
    fun join() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        assertNotNull(alice)
    }

    @Test
    fun joinWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMetadata = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName, aliceMetadata)
        val alice = room?.join(aliceMemberInit)
        assertNotNull(alice)
        assertEquals(alice?.name, aliceName)
        assertEquals(alice?.metadata, aliceMetadata)
    }

    @Test
    fun join_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        assertNotNull(alice)
        val aliceDuplicate = room?.join(aliceMemberInit)
        assertNull(aliceDuplicate)
    }

    @Test
    fun joinTwice() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        assertNotNull(alice)
        assertTrue(alice!!.leave())
        val alice2 = room.join(aliceMemberInit)
        assertNotNull(alice2)
        assertTrue(alice2!!.leave())
    }

    @Test
    fun updateMetadata() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        assertNotNull(room)
        room?.updateMetadata(metadata)
        assertEquals(room?.metadata, metadata)
    }

    @Test
    fun dispose() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        assertNotNull(room)
        room?.dispose()
        assertNotNull(room)
    }

    @OptIn(SkyWayOptIn::class)
    @Test
    fun getStats_WithMediaStream() = runBlocking {
        val aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        val aliceRoom = SFURoom.create()
        val aliceMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val alice = aliceRoom?.join(aliceMemberInit)

        val bobRoom = SFURoom.find(id = aliceRoom?.id)
        val bobMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val bob = bobRoom?.join(bobMemberInit)

        val options = RoomPublication.Options()
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)

        TestUtil.waitForFindSubscription(bob!!, publication!!)

        val subscription = publication.id.let { bob.subscribe(it) }
        assertNotNull(subscription)

        val pubStats = publication.getStats(bob.id)
        assertNotNull(pubStats)
        assertTrue(pubStats!!.reports.isNotEmpty())

        val subStats = subscription!!.getStats()
        assertNotNull(subStats)
        assertTrue(subStats!!.reports.isNotEmpty())
    }

    @Test
    fun getBotExited() = runBlocking {
        val room = SFURoom.create()
        val channel = Channel.find(id = room?.id)
        val bot = channel?.bots?.first()
        val cv = ConditionVariable()
        channel?.leave(bot!!)
        channel?.onMemberLeftHandler = {
            if(it.id == bot?.id) {
                cv.open()
            }
        }
        cv.block(3000)
        assertNull(room?.bot)
    }

    @Test
    fun room_Member_NameAndMetadataNullable() = runBlocking {
        val aliceRoom = SFURoom.create()
        assertNotNull(aliceRoom)
        assertTrue(aliceRoom?.name == null)
        assertTrue(aliceRoom?.metadata == null)
        val bobRoom = SFURoom.find(id = aliceRoom?.id)
        assertNotNull(bobRoom)
        val alice = aliceRoom?.join(RoomMember.Init())
        assertNotNull(alice)
        assertTrue(alice?.name == null)
        assertTrue(alice?.metadata == null)
        val bob = bobRoom?.join(RoomMember.Init())
        assertNotNull(bob)
        assertTrue(bob?.name == null)
        assertTrue(bob?.metadata == null)
        val bobRemoteMember = aliceRoom?.members?.firstOrNull { it.id != alice?.id }
        assertNotNull(bobRemoteMember)
        assertTrue(bobRemoteMember?.name == null)
        assertTrue(bobRemoteMember?.metadata == null)
    }
}
