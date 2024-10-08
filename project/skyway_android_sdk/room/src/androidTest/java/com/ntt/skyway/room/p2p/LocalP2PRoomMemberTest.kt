package com.ntt.skyway.room.p2p

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.util.TestUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID


class LocalP2PRoomMemberTest {
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

    private var alice: LocalP2PRoomMember? = null
    private var bob: LocalP2PRoomMember? = null
    private var aliceRoom: P2PRoom? = null
    private var bobRoom: P2PRoom? = null

    private lateinit var aliceLocalVideoStream: LocalVideoStream
    private lateinit var aliceLocalAudioStream: LocalAudioStream
    private lateinit var bobLocalVideoStream: LocalVideoStream


    @Before
    fun setup() = runBlocking {
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
        Log.d(tag, "SkyWayContext.dispose()")
        aliceRoom?.dispose()
        bobRoom?.dispose()
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

    @Test
    fun publish() = runBlocking {
        val options = RoomPublication.Options(isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
    }

    @Test
    fun publishWithMetadata() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
    }

    @Test
    fun publishTwice() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication1 = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication1)
        assertEquals(publication1?.metadata, options.metadata)

        val publication2 = alice?.publish(bobLocalVideoStream, options)
        assertNotNull(publication2)
        assertEquals(publication2?.metadata, options.metadata)
    }

    @Test
    fun publish_WithEncodingId() = runBlocking {
        val encoding = Encoding("TEST_ENCODING_ID", 200_000, 4.0)
        val options =
            RoomPublication.Options("metadata", encodings = listOf(encoding), isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        val pubEncoding = publication?.encodings?.get(0)
        assertNotNull(pubEncoding)
        assertEquals(encoding.id, pubEncoding?.id)
    }

    @Test
    fun publish_WithDtxOption() = runBlocking {
        val codec = Codec(Codec.MimeType.OPUS, Codec.Parameters(useDtx = true))
        val options =
            RoomPublication.Options(codecCapabilities = mutableListOf(codec))
        aliceLocalAudioStream = AudioSource.createStream()
        val publication = alice?.publish(aliceLocalAudioStream, options)
        assertNotNull(publication)
        assertEquals(publication!!.codecCapabilities[0].parameters.useDtx, true)
    }

    @Test
    fun publish_WithDtxOptionFalse() = runBlocking {
        val codec = Codec(Codec.MimeType.OPUS, Codec.Parameters(useDtx = false))
        val options =
            RoomPublication.Options(codecCapabilities = mutableListOf(codec))
        aliceLocalAudioStream = AudioSource.createStream()
        val publication = alice?.publish(aliceLocalAudioStream, options)
        assertNotNull(publication)
        assertEquals(publication!!.codecCapabilities[0].parameters.useDtx, false)
    }

    @Test
    fun subscribe() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        assertNotNull(subscription?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithAlreadySubscribed() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription1 = publication.id.let { bob?.subscribe(it) }
        assertNotNull(subscription1?.id)
        val subscription2 = publication.id.let { bob?.subscribe(it) }
        assertNull(subscription2?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithNotExistPublication() = runBlocking {
        val subscription = bob?.subscribe("notexist!!")
        assertNull(subscription?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithSubscribedByPublisher() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription = alice?.subscribe(publication.id)
        assertNull(subscription)
    }

    @Test
    fun subscribe_AnotherPublicationAfterPublisherIsDisconnected() = runBlocking {
        val charlieRoom = P2PRoom.find(id = aliceRoom?.id)
        val charlieMemberInit = RoomMember.Init(UUID.randomUUID().toString())
        val charlie = charlieRoom?.join(charlieMemberInit)
        val alicePub = alice?.publish(aliceLocalVideoStream)
        assertNotNull(alicePub)

        val bobPub = bob?.publish(bobLocalVideoStream)
        assertNotNull(bobPub)
        aliceRoom?.dispose()
        launch {
            charlie?.subscribe(alicePub!!)
        }
        delay(100)
        val sub = charlie?.subscribe(bobPub!!)
        charlieRoom?.dispose()
        assertNotNull(sub)
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
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        assertTrue(alice!!.unpublish(publication!!))
    }

    @Test
    fun unpublish_ShouldReturnFalseWithAlreadyUnpublished() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        assertTrue(alice!!.unpublish(publication!!))
        assertFalse(alice!!.unpublish(publication))
    }

    @Test
    fun unsubscribe() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        assertNotNull(subscription?.id)
        assertTrue(bob!!.unsubscribe(subscription!!.id))
    }

    @Test
    fun unsubscribe_ShouldReturnFalseWithAlreadyUnsubscribed() = runBlocking {
        val options = RoomPublication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        assertNotNull(subscription?.id)
        assertTrue(bob!!.unsubscribe(subscription!!.id))
        assertFalse(bob!!.unsubscribe(subscription.id))
    }

    @Test
    fun updateMetadata() = runBlocking {
        assertTrue(alice?.updateMetadata("updateMetadata")!!)
    }

    @Test
    fun leave() = runBlocking {
        assertTrue(alice?.leave()!!)
    }

    @Test
    fun leave_ShouldReturnFalseWithAlreadyLeft() = runBlocking {
        assertTrue(alice?.leave()!!)
        assertFalse(alice?.leave()!!)
    }
}
