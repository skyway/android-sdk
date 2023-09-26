package com.ntt.skyway.core.channel

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.util.*


class ChannelTest {
    val TAG = this.javaClass.simpleName

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
        Log.d(TAG, "SkyWayContext.dispose()")
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

    @Test
    fun create() = runBlocking {
        val channel = Channel.create()
        assertNotNull(channel)
    }

    @Test
    fun createWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val channel = Channel.create(name, metadata)
        assertNotNull(channel)
        assertEquals(channel?.name, name)
        assertEquals(channel?.metadata, metadata)
    }

    @Test
    fun create_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel1 = Channel.create(name)
        assertNotNull(channel1)
        val channel2 = Channel.create(name)
        assertNull(channel2)
    }

    @Test
    fun findOrCreate() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.findOrCreate(name)
        assertNotNull(channel)
    }

    @Test
    fun findOrCreate_WithAlreadyCreated() = runBlocking {
        val name = UUID.randomUUID().toString()
        Channel.create(name)
        val channel = Channel.findOrCreate(name)
        assertNotNull(channel)
    }

    @Test
    fun find() = runBlocking {
        val channel1 = Channel.create()
        assertNotNull(channel1)

        val channel2 = Channel.find(id = channel1?.id)
        assertNotNull(channel2)
    }

    @Test
    fun find_ShouldReturnNullWithNotExistChannel() = runBlocking {
        val id = UUID.randomUUID().toString()
        val channel = Channel.find(id = id)
        assertNull(channel)
    }

    @Test
    fun findByName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel1 = Channel.create(name)
        assertNotNull(channel1)

        val channel2 = Channel.find(channel1?.name)
        assertNotNull(channel2)
    }

    @Test
    fun findByName_ShouldReturnNullWithNotExistChannel() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.find(name)
        assertNull(channel)
    }

    @Test
    fun join() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName)
        val alice = channel?.join(aliceMemberInit)
        assertNotNull(alice)
    }

    @Test
    fun joinWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMetadata = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName, aliceMetadata)
        val alice = channel?.join(aliceMemberInit)
        assertNotNull(alice)
        assertEquals(alice?.name, aliceName)
        assertEquals(alice?.metadata, aliceMetadata)
    }

    @Test
    fun join_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName)
        val alice = channel?.join(aliceMemberInit)
        assertNotNull(alice)
        val aliceDuplicate = channel?.join(aliceMemberInit)
        assertNull(aliceDuplicate)
    }

    @Test
    fun joinTwice() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName)
        val alice = channel?.join(aliceMemberInit)
        assertNotNull(alice)
        assertTrue(alice!!.leave())
        val alice2 = channel.join(aliceMemberInit)
        assertNotNull(alice2)
        assertTrue(alice2!!.leave())
    }

    @Test
    fun joinTwenty() = runBlocking {
        val channelName = UUID.randomUUID().toString()
        val channels = mutableListOf<Channel>()
        val members = mutableListOf<LocalPerson>()

        repeat(20) {
            val channel = Channel.findOrCreate(channelName)
            assertNotNull(channel)
            channel?.let {
                channels.add(it)
            }

            val memberName = UUID.randomUUID().toString()
            val memberInit = Member.Init(memberName)
            val member = channel?.join(memberInit)

            assertNotNull(member)
            member?.let {
                members.add(it)
            }
        }
    }

    @Test
    fun updateMetadata() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        assertNotNull(channel)
        channel?.updateMetadata(metadata)
        assertEquals(channel?.metadata, metadata)
    }

    @Test
    fun dispose() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        assertNotNull(channel)
        channel?.dispose()
        assertNotNull(channel)
    }

    @Test
    fun getStats_WithMediaStream() = runBlocking {
        val aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        val aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString())
        val alice = aliceChannel?.join(aliceMemberInit)

        val bobChannel = Channel.find(id = aliceChannel?.id)
        val bobMemberInit = Member.Init(UUID.randomUUID().toString())
        val bob = bobChannel?.join(bobMemberInit)

        val options = Publication.Options()
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)

        TestUtil.waitForFindSubscription(bob!!, publication!!)

        val subscription = publication.id.let { bob.subscribe(it) }
        assertNotNull(subscription?.id)

        val pubStats = publication.getStats(bob.id)
        assertNotNull(pubStats)
        assertTrue(pubStats!!.reports.isNotEmpty())

        val subStats = subscription!!.getStats()
        assertNotNull(subStats)
        assertTrue(subStats!!.reports.isNotEmpty())
    }

    @Test
    fun getStats_WithDataStream() = runBlocking {
        val aliceLocalDataStream = DataSource().createStream()

        val aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString())
        val alice = aliceChannel?.join(aliceMemberInit)

        val bobChannel = Channel.find(id = aliceChannel?.id)
        val bobMemberInit = Member.Init(UUID.randomUUID().toString())
        val bob = bobChannel?.join(bobMemberInit)

        val options = Publication.Options()
        val publication = alice?.publish(aliceLocalDataStream, options)
        assertNotNull(publication)

        TestUtil.waitForFindSubscription(bob!!, publication!!)

        val subscription = publication.id.let { bob.subscribe(it) }
        assertNotNull(subscription?.id)

        val pubStats = publication.getStats(bob.id)
        assertNotNull(pubStats)
        assertTrue(pubStats!!.reports.isNotEmpty())

        val subStats = subscription!!.getStats()
        assertNotNull(subStats)
        assertTrue(subStats!!.reports.isNotEmpty())
    }

    @Test
    fun accessPropsAfterDispose() = runBlocking {
        val aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString())
        val alice = aliceChannel?.join(aliceMemberInit)
        aliceChannel?.dispose()
        assertTrue(alice?.metadata != "metadata")
    }
}
