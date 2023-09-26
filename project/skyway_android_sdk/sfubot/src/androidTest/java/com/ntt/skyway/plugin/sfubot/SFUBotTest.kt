package com.ntt.skyway.plugin.sfuBot

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.plugin.sfuBot.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.util.*

class SFUBotTest {
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
    private lateinit var aliceChannel: Channel
    private lateinit var localStream: LocalVideoStream

    @Before
    fun setup() = runBlocking {
        TestUtil.setupSkyway()
        SkyWayContext.registerPlugin(SFUBotPlugin())
        localStream = CustomVideoFrameSource(800, 800).createStream()
        aliceChannel = Channel.create()!!
        val init = Member.Init(UUID.randomUUID().toString())
        alice = aliceChannel.join(init)!!
    }

    @After
    fun tearDown() {
        Log.d(TAG, "SkyWayContext.dispose()")
        SkyWayContext.dispose()
    }

    @Test
    fun createBot() = runBlocking {
        val bot = SFUBot.createBot(aliceChannel)
        assertNotNull(bot)
    }

    @Test
    fun startForwarding() = runBlocking {
        val bot = SFUBot.createBot(aliceChannel)
        val publication = alice?.publish(localStream)
        val forwarding = bot?.startForwarding(publication!!)
        assertNotNull(forwarding)
    }

    @Test
    fun stopForwarding() = runBlocking {
        val bot = SFUBot.createBot(aliceChannel)!!
        val publication = alice?.publish(localStream)
        val forwarding = bot.startForwarding(publication!!)!!
        val result = bot.stopForwarding(forwarding)
        assertTrue(result)
    }

    @Test
    fun getStats_WithMediaStream() = runBlocking {
        val bot = SFUBot.createBot(aliceChannel)
        val publication = alice?.publish(localStream)
        val forwarding = bot?.startForwarding(publication!!)
        assertNotNull(forwarding)

        TestUtil.waitForFindSubscriptions(alice!!, publication!!)

        val subscription = forwarding?.id?.let { alice?.subscribe(it) }
        assertNotNull(subscription?.id)

        val pub_stats = publication.getStats(bot!!.id)
        assertNotNull(pub_stats)
        assertTrue(pub_stats!!.reports.size > 0)

        val sub_stats = subscription!!.getStats()
        assertNotNull(sub_stats)
        assertTrue(sub_stats!!.reports.size > 0)
    }
}
