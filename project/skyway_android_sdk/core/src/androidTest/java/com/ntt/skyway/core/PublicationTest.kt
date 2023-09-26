package com.ntt.skyway.core

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class PublicationTest {
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

    private var alice: LocalPerson? = null
    private var bob: LocalPerson? = null
    private var aliceChannel: Channel? = null
    private var bobChannel: Channel? = null

    private lateinit var aliceLocalVideoStream: LocalVideoStream
    private lateinit var bobLocalVideoStream: LocalVideoStream

    @Before
    fun setup() = runBlocking {
        TestUtil.setupSkyway()

        aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()
        bobLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString(), "")
        alice = aliceChannel?.join(aliceMemberInit)

        bobChannel = Channel.find(id = aliceChannel?.id)
        val bobMemberInit = Member.Init(UUID.randomUUID().toString(), "")
        bob = bobChannel?.join(bobMemberInit)

    }

    @After
    fun tearDown() {
        Log.d(TAG, "SkyWayContext.dispose()")
        aliceChannel?.dispose()
        bobChannel?.dispose()
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

    @Test
    fun publish_EnableWhenPublished() = runBlocking {
        val options = Publication.Options(isEnabled = true)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.state, Publication.State.ENABLED)
    }

    @Test
    fun publish_DisabledWhenPublished() = runBlocking {
        val options = Publication.Options(isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.state, Publication.State.DISABLED)
    }

    @Test
    fun publish_Enable() = runBlocking {
        val options = Publication.Options(isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.state, Publication.State.DISABLED)
        Assert.assertTrue(publication!!.enable())
        Assert.assertEquals(publication.state, Publication.State.ENABLED)
    }

    @Test
    fun publish_Disable() = runBlocking {
        val options = Publication.Options(isEnabled = true)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.state, Publication.State.ENABLED)
        Assert.assertTrue(publication!!.disable())
        Assert.assertEquals(publication.state, Publication.State.DISABLED)
    }
}
