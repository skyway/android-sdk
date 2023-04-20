package com.ntt.skyway.room.p2p

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.util.TestUtil
import kotlinx.coroutines.*
import org.junit.*
import java.util.*


class LocalP2PRoomMemberTest {
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

    @Test
    fun publish() = runBlocking {
        val options = RoomPublication.Options(isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
    }

    @Test
    fun publishWithMetadata() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
    }

    @Test
    fun publishTwice() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication1 = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication1)
        Assert.assertEquals(publication1?.metadata, options.metadata)

        val publication2 = alice?.publish(bobLocalVideoStream, options)
        Assert.assertNotNull(publication2)
        Assert.assertEquals(publication2?.metadata, options.metadata)
    }

    @Test
    fun publish_WithEncodingId() = runBlocking {
        val encoding = Encoding("TEST_ENCODING_ID",200_000, 4.0)
        val options = RoomPublication.Options("metadata", encodings = listOf(encoding), isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        val pubEncoding = publication?.encodings?.get(0)
        Assert.assertNotNull(pubEncoding)
        Assert.assertEquals(encoding.id, pubEncoding?.id)
    }

    @Test
    fun subscribe() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!,publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        Assert.assertNotNull(subscription?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithAlreadySubscribed() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!,publication!!)
        val subscription1 = publication.id.let { bob?.subscribe(it) }
        Assert.assertNotNull(subscription1?.id)
        val subscription2 = publication.id.let { bob?.subscribe(it) }
        Assert.assertNull(subscription2?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithNotExistPublication() = runBlocking {
        val subscription =  bob?.subscribe("notexist!!")
        Assert.assertNull(subscription?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithSubscribedByPublisher() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!,publication!!)
        val subscription = alice?.subscribe(publication.id)
        Assert.assertNull(subscription)
    }

//    @Test  //TODO impl on core getPreferredEncodingId
//    fun subscribe_WithPreferredEncodingId() = runBlocking {
//        val encoding = Encoding("TEST_ENCODING_ID",200_000, 4.0)
//        val options = RoomPublication.Options("metadata", null, listOf(encoding), false)
//        val publication = alice?.publish(aliceLocalVideoStream, options)
//        Assert.assertNotNull(publication)
//        Assert.assertEquals(publication?.metadata, options.metadata)
//        val subOption = RoomSubscription.Options(true, encoding.id)
//        val subscription = publication?.id?.let { bob?.subscribe(it, subOption) }
//        Assert.assertNotNull(subscription?.id)
////        Assert.assertEquals(subscription.get)
//    }

    @Test
    fun unpublish() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        Assert.assertTrue(alice!!.unpublish(publication!!))
    }

    @Test
    fun unpublish_ShouldReturnFalseWithAlreadyUnpublished() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        Assert.assertTrue(alice!!.unpublish(publication!!))
        Assert.assertFalse(alice!!.unpublish(publication))
    }

    @Test
    fun unsubscribe() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!,publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        Assert.assertNotNull(subscription?.id)
        Assert.assertTrue(bob!!.unsubscribe(subscription!!.id))
    }

    @Test
    fun unsubscribe_ShouldReturnFalseWithAlreadyUnsubscribed() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        Assert.assertNotNull(publication)
        Assert.assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!,publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        Assert.assertNotNull(subscription?.id)
        Assert.assertTrue(bob!!.unsubscribe(subscription!!.id))
        Assert.assertFalse(bob!!.unsubscribe(subscription.id))
    }

    @Test
    fun updateMetadata() = runBlocking {
        Assert.assertTrue(alice?.updateMetadata("updateMetadata")!!)
    }

    @Test
    fun leave() = runBlocking {
        Assert.assertTrue(alice?.leave()!!)
    }

    @Test
    fun leave_ShouldReturnFalseWithAlreadyLeft() = runBlocking {
        Assert.assertTrue(alice?.leave()!!)
        Assert.assertFalse(alice?.leave()!!)
    }
}
