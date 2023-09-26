package com.ntt.skyway.core.channel.member

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*


class LocalPersonTest {
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
    fun publish() = runBlocking {
        val options = Publication.Options(isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
    }

    @Test
    fun publishWithMetadata() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
    }

    @Test
    fun publishTwice() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
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
            Publication.Options("metadata", encodings = listOf(encoding), isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        val pubEncoding = publication?.encodings?.get(0)
        assertNotNull(pubEncoding)
        assertEquals(encoding.id, pubEncoding?.id)
    }

    @Test
    fun subscribe() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription = publication.id.let { bob?.subscribe(it) }
        assertNotNull(subscription?.id)
    }

    @Test
    fun subscribe_ShouldReturnNullWithAlreadySubscribed() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
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
        val options = Publication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        TestUtil.waitForFindSubscription(bob!!, publication!!)
        val subscription = alice?.subscribe(publication.id)
        assertNull(subscription)
    }


    @Test
    fun unpublish() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        assertTrue(alice!!.unpublish(publication!!))
    }

    @Test
    fun unpublish_ShouldReturnFalseWithAlreadyUnpublished() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
        val publication = alice?.publish(aliceLocalVideoStream, options)
        assertNotNull(publication)
        assertEquals(publication?.metadata, options.metadata)
        assertTrue(alice!!.unpublish(publication!!))
        assertFalse(alice!!.unpublish(publication))
    }

    @Test
    fun unsubscribe() = runBlocking {
        val options = Publication.Options("metadata", isEnabled = false)
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
        val options = Publication.Options("metadata", isEnabled = false)
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
