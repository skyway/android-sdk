package com.ntt.skyway.room.p2p

import android.Manifest
import android.util.Log
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.util.TestUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class P2PRoomTest {
    val TAG = this.javaClass.simpleName

    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
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
        Log.d(TAG, "SkyWayContext.dispose()")
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

    @Test
    fun create() = runBlocking {
        val room = P2PRoom.create()
        Assert.assertNotNull(room)
    }

    @Test
    fun createWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val room = P2PRoom.create(name, metadata)
        Assert.assertNotNull(room)
        Assert.assertEquals(room?.name, name)
        Assert.assertEquals(room?.metadata, metadata)
    }

    @Test
    fun create_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room1 = P2PRoom.create(name)
        Assert.assertNotNull(room1)
        val room2 = P2PRoom.create(name)
        Assert.assertNull(room2)
    }

    @Test
    fun findOrCreate() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = P2PRoom.findOrCreate(name)
        Assert.assertNotNull(room)
    }

    @Test
    fun findOrCreate_WithAlreadyCreated() = runBlocking {
        val name = UUID.randomUUID().toString()
        P2PRoom.create(name)
        val room2 = P2PRoom.findOrCreate(name)
        Assert.assertNotNull(room2)
    }

    @Test
    fun find() = runBlocking {
        val room1 = P2PRoom.create()
        Assert.assertNotNull(room1)

        val room2 = P2PRoom.find(id = room1?.id)
        Assert.assertNotNull(room2)
    }

    @Test
    fun find_ShouldReturnNullWithNotExistRoom() = runBlocking {
        val id = UUID.randomUUID().toString()
        val room2 = P2PRoom.find(id = id)
        Assert.assertNull(room2)
    }

    @Test
    fun findByName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room1 = P2PRoom.create(name)
        Assert.assertNotNull(room1)

        val room2 = P2PRoom.find(room1?.name)
        Assert.assertNotNull(room2)
    }

    @Test
    fun findByName_ShouldReturnNullWithNotExistRoom() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room2 = P2PRoom.find(name)
        Assert.assertNull(room2)
    }

    @Test
    fun join() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = P2PRoom.create(name)
        Assert.assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
    }

    @Test
    fun joinWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = P2PRoom.create(name)
        Assert.assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMetadata = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName, aliceMetadata)
        val alice = room?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
        Assert.assertEquals(alice?.name, aliceName)
        Assert.assertEquals(alice?.metadata, aliceMetadata)
    }

    @Test
    fun join_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = P2PRoom.create(name)
        Assert.assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
        val aliceDuplicate = room?.join(aliceMemberInit)
        Assert.assertNull(aliceDuplicate)
    }

    @Test
    fun joinTwice() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = P2PRoom.create(name)
        Assert.assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
        Assert.assertTrue(alice!!.leave())
        val alice2 = room.join(aliceMemberInit)
        Assert.assertNotNull(alice2)
        Assert.assertTrue(alice2!!.leave())
    }

    @Test
    fun joinTwenty() = runBlocking {
        val roomName = UUID.randomUUID().toString()
        val rooms = mutableListOf<P2PRoom>()
        val members = mutableListOf<LocalP2PRoomMember>()

        repeat(20) {
            val room = P2PRoom.findOrCreate(roomName)
            Assert.assertNotNull(room)
            room?.let {
                rooms.add(it)
            }

            val memberName = UUID.randomUUID().toString()
            val memberInit = RoomMember.Init(memberName)
            val member = room?.join(memberInit)

            Assert.assertNotNull(member)
            member?.let {
                members.add(it)
            }
        }
    }

    @Test
    fun updateMetadata() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val room = P2PRoom.create(name)
        Assert.assertNotNull(room)
        room?.updateMetadata(metadata)
        Assert.assertEquals(room?.metadata, metadata)
    }

    @Test
    fun dispose() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = P2PRoom.create(name)
        Assert.assertNotNull(room)
        room?.dispose()
        Assert.assertNotNull(room)
    }

    @OptIn(SkyWayOptIn::class)
    @Test
    fun getStats_WithMediaStream() = runBlocking {
        val aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        val aliceRoom = P2PRoom.create()
        val aliceMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val alice = aliceRoom?.join(aliceMemberInit)

        val bobRoom = P2PRoom.find(id = aliceRoom?.id)
        val bobMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val bob = bobRoom?.join(bobMemberInit)

        val options = RoomPublication.Options()
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)

        TestUtil.waitForFindSubscription(bob!!, publication!!)

        val subscription = publication.id.let { bob.subscribe(it) }
        Assert.assertNotNull(subscription?.id)

        val pubStats = publication.getStats(bob.id)
        Assert.assertNotNull(pubStats)
        Assert.assertTrue(pubStats!!.reports.isNotEmpty())

        val subStats = subscription!!.getStats()
        Assert.assertNotNull(subStats)
        Assert.assertTrue(subStats!!.reports.isNotEmpty())
    }

    @OptIn(SkyWayOptIn::class)
    @Test
    fun getStats_WithDataStream() = runBlocking {
        val aliceLocalDataStream = DataSource().createStream()

        val aliceRoom = P2PRoom.create()
        val aliceMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val alice = aliceRoom?.join(aliceMemberInit)

        val bobRoom = P2PRoom.find(id = aliceRoom?.id)
        val bobMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val bob = bobRoom?.join(bobMemberInit)

        val options = RoomPublication.Options()
        val publication = alice?.publish(aliceLocalDataStream, options)
        Assert.assertNotNull(publication)

        TestUtil.waitForFindSubscription(bob!!, publication!!)

        val subscription = publication.id.let { bob.subscribe(it) }
        Assert.assertNotNull(subscription?.id)

        val pubStats = publication.getStats(bob.id)
        Assert.assertNotNull(pubStats)
        Assert.assertTrue(pubStats!!.reports.isNotEmpty())

        val subStats = subscription!!.getStats()
        Assert.assertNotNull(subStats)
        Assert.assertTrue(subStats!!.reports.isNotEmpty())
    }

    @Test
    fun accessPropsAfterDispose() = runBlocking {
        val aliceRoom = P2PRoom.create()
        val aliceMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val alice = aliceRoom?.join(aliceMemberInit)
        aliceRoom?.dispose()
        Assert.assertTrue(alice?.metadata != "metadata")
    }
}
