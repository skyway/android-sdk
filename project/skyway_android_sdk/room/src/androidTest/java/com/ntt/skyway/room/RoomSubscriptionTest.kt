package com.ntt.skyway.room

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class RoomSubscriptionTest {
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

    private var alice: LocalP2PRoomMember? = null
    private var bob: LocalP2PRoomMember? = null
    private var aliceRoom: P2PRoom? = null
    private var bobRoom: P2PRoom? = null

    private lateinit var aliceLocalVideoStream: LocalVideoStream
    private lateinit var bobLocalVideoStream: LocalVideoStream

    @Before
    fun setup() = runBlocking{
        TestUtil.setupSkyway()

        aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()
        bobLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        aliceRoom = P2PRoom.create()
        val aliceMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        alice = aliceRoom?.join(aliceMemberInit)

        bobRoom = P2PRoom.find(id = aliceRoom?.id)
        val bobMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        bob = bobRoom?.join(bobMemberInit)

    }

    @After
    fun tearDown() {
        Log.d(TAG, "SkyWayContext.dispose()")
        aliceRoom?.dispose()
        bobRoom?.dispose()
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

//    @Test  //TODO impl on core getPreferredEncodingId
//    fun changePreferredEncoding() = runBlocking {
//        val encoding = Encoding("ENCODING_ID_1",200_000, 4.0)
//        val options = RoomPublication.Options("metadata", null, listOf(encoding), false)
//        val publication = alice?.publish(aliceLocalVideoStream, options)
//        Assert.assertNotNull(publication)
//        Assert.assertEquals(publication?.metadata, options.metadata)
//        val pubEncoding = publication?.encodings?.get(0)
//        Assert.assertNotNull(pubEncoding)
//        Assert.assertEquals(encoding.id, pubEncoding?.id)
//
//        val subscription = publication?.id?.let { bob?.subscribe(it) }
//        Assert.assertNotNull(subscription?.id)
//    }

    @Test
    fun cancel() = runBlocking {
        val options = RoomPublication.Options(metadata = "metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)

        val subscription = publication?.id?.let { bob?.subscribe(it) }
        Assert.assertNotNull(subscription?.id)
        subscription?.cancel()
        Assert.assertEquals(subscription!!.state, Subscription.State.CANCELED)
    }


}
