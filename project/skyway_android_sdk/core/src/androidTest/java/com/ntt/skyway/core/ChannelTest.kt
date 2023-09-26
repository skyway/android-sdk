package com.ntt.skyway.core

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class ChannelTest {
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
        val channel = Channel.create()
        Assert.assertNotNull(channel)
    }

    @Test
    fun createWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val metadata = UUID.randomUUID().toString()
        val channel = Channel.create(name, metadata)
        Assert.assertNotNull(channel)
        Assert.assertEquals(channel?.name, name)
        Assert.assertEquals(channel?.metadata, metadata)
    }

    @Test
    fun create_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel1 = Channel.create(name)
        Assert.assertNotNull(channel1)
        val channel2 = Channel.create(name)
        Assert.assertNull(channel2)
    }

    @Test
    fun findOrCreate() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.findOrCreate(name)
        Assert.assertNotNull(channel)
    }

    @Test
    fun findOrCreate_WithAlreadyCreated() = runBlocking {
        val name = UUID.randomUUID().toString()
        Channel.create(name)
        val channel = Channel.findOrCreate(name)
        Assert.assertNotNull(channel)
    }

    @Test
    fun find() = runBlocking {
        val channel1 = Channel.create()
        Assert.assertNotNull(channel1)

        val channel2 = Channel.find(id = channel1?.id)
        Assert.assertNotNull(channel2)
    }

    @Test
    fun find_ShouldReturnNullWithNotExistChannel() = runBlocking {
        val id = UUID.randomUUID().toString()
        val channel = Channel.find(id = id)
        Assert.assertNull(channel)
    }

    @Test
    fun findByName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel1 = Channel.create(name)
        Assert.assertNotNull(channel1)

        val channel2 = Channel.find(channel1?.name)
        Assert.assertNotNull(channel2)
    }

    @Test
    fun findByName_ShouldReturnNullWithNotExistChannel() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.find(name)
        Assert.assertNull(channel)
    }

    @Test
    fun join() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        Assert.assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName)
        val alice = channel?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
    }

    @Test
    fun joinWithOptions() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        Assert.assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMetadata = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName, aliceMetadata)
        val alice = channel?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
        Assert.assertEquals(alice?.name, aliceName)
        Assert.assertEquals(alice?.metadata, aliceMetadata)
    }

    @Test
    fun join_ShouldReturnNullWithDuplicatedName() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        Assert.assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName)
        val alice = channel?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
        val aliceDuplicate = channel?.join(aliceMemberInit)
        Assert.assertNull(aliceDuplicate)
    }

    @Test
    fun joinTwice() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        Assert.assertNotNull(channel)
        val aliceName = UUID.randomUUID().toString()
        val aliceMemberInit = Member.Init(aliceName)
        val alice = channel?.join(aliceMemberInit)
        Assert.assertNotNull(alice)
        Assert.assertTrue(alice!!.leave())
        val alice2 = channel.join(aliceMemberInit)
        Assert.assertNotNull(alice2)
        Assert.assertTrue(alice2!!.leave())
    }

    @Test
    fun joinTwenty() = runBlocking {
        val channelName = UUID.randomUUID().toString()
        val channels = mutableListOf<Channel>()
        val members = mutableListOf<LocalPerson>()

        repeat(20) {
            val channel = Channel.findOrCreate(channelName)
            Assert.assertNotNull(channel)
            channel?.let {
                channels.add(it)
            }

            val memberName = UUID.randomUUID().toString()
            val memberInit = Member.Init(memberName)
            val member = channel?.join(memberInit)

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
        val channel = Channel.create(name)
        Assert.assertNotNull(channel)
        channel?.updateMetadata(metadata)
        Assert.assertEquals(channel?.metadata, metadata)
    }

    @Test
    fun dispose() = runBlocking {
        val name = UUID.randomUUID().toString()
        val channel = Channel.create(name)
        Assert.assertNotNull(channel)
        channel?.dispose()
        Assert.assertNotNull(channel)
    }

    @OptIn(SkyWayOptIn::class)
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

        val aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString())
        val alice = aliceChannel?.join(aliceMemberInit)

        val bobChannel = Channel.find(id = aliceChannel?.id)
        val bobMemberInit = Member.Init(UUID.randomUUID().toString())
        val bob = bobChannel?.join(bobMemberInit)

        val options = Publication.Options()
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
        val aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString())
        val alice = aliceChannel?.join(aliceMemberInit)
        aliceChannel?.dispose()
        Assert.assertTrue(alice?.metadata != "metadata")
    }
}
