package com.ntt.skyway.core

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class SubscriptionTest {
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
    fun setup() = runBlocking{
        TestUtil.setupSkyway()

        aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()
        bobLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        aliceChannel = Channel.create()
        val aliceMemberInit = Member.Init(UUID.randomUUID().toString())
        alice = aliceChannel?.join(aliceMemberInit)

        bobChannel = Channel.find(id = aliceChannel?.id)
        val bobMemberInit = Member.Init(UUID.randomUUID().toString())
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
    fun cancel() = runBlocking {
        val options = Publication.Options(metadata = "metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)

        TestUtil.waitForFindSubscription(bob!!,publication!!)

        val subscription = publication.id.let { bob?.subscribe(it) }
        Assert.assertNotNull(subscription?.id)
        subscription?.cancel()
        Assert.assertEquals(subscription!!.state, Subscription.State.CANCELED)
    }


}
