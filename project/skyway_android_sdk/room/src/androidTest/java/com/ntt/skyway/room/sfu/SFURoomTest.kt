package com.ntt.skyway.room.sfu

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class SFURoomTest {
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
        val room = SFURoom.create()
        Assert.assertNotNull(room)
    }

    @Test
    fun createWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val room = SFURoom.create(name, metadata)
        Assert.assertNotNull(room)
        Assert.assertEquals(room?.name, name)
        Assert.assertEquals(room?.metadata, metadata)
    }

    @Test
    fun create_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room1 = SFURoom.create(name)
        Assert.assertNotNull(room1)
        val room2 = SFURoom.create(name)
        Assert.assertNull(room2)
    }

    @Test
    fun findOrCreate() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.findOrCreate(name)
        Assert.assertNotNull(room)
    }

    @Test
    fun findOrCreate_WithAlreadyCreated() = runBlocking {
        val name = UUID.randomUUID().toString()
        SFURoom.create(name)
        val room = SFURoom.findOrCreate(name)
        Assert.assertNotNull(room)
    }

    @Test
    fun find() = runBlocking {
        val room1 = SFURoom.create()
        Assert.assertNotNull(room1)

        val room2 = SFURoom.find(id = room1?.id)
        Assert.assertNotNull(room2)
    }

    @Test
    fun find_ShouldReturnNullWithNotExistRoom() = runBlocking {
        val id = UUID.randomUUID().toString()
        val room2 = SFURoom.find(id = id)
        Assert.assertNull(room2)
    }

    @Test
    fun findByName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room1 = SFURoom.create(name)
        Assert.assertNotNull(room1)

        val room2 = SFURoom.find(room1?.name)
        Assert.assertNotNull(room2)
    }

    @Test
    fun findByName_ShouldReturnNullWithNotExistRoom() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room2 = SFURoom.find(name)
        Assert.assertNull(room2)
    }

    @Test
    fun join() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        Assert.assertNotNull(room)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = RoomMember.Init(aliceName)
        val alice = room?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
    }

    @Test
    fun joinWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
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
        val room = SFURoom.create(name)
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
        val room = SFURoom.create(name)
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
    fun updateMetadata() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        Assert.assertNotNull(room)
        room?.updateMetadata(metadata)
        Assert.assertEquals(room?.metadata, metadata)
    }

    @Test
    fun dispose() = runBlocking {
        val name = UUID.randomUUID().toString()
        val room = SFURoom.create(name)
        Assert.assertNotNull(room)
        room?.dispose()
        Assert.assertNotNull(room)
    }
}
