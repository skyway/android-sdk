package com.ntt.skyway.core

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMember
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.util.TestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.util.*


class AuthTokenTest {
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
    private var remoteBob: RemoteMember? = null
    private var bob: LocalPerson? = null
    private var aliceChannel: Channel? = null
    private var bobChannel: Channel? = null

    private lateinit var aliceLocalVideoStream: LocalVideoStream
    private lateinit var bobLocalVideoStream: LocalVideoStream

    @Before
    fun setup() = runBlocking{
        TestUtil.setupSkyway(SkyWayContext.Token(tokenReminderTimeSec = 8))

        aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()
        bobLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        aliceChannel = Channel.create(null, null)

        bobChannel = Channel.find(null, id = aliceChannel?.id)
        val bobMemberInit = Member.Init(name = UUID.randomUUID().toString(), metadata = "")
        bob = bobChannel?.join(bobMemberInit)


        val aliceMemberInit = Member.Init(name = UUID.randomUUID().toString(), metadata = "")
        alice = aliceChannel?.join(aliceMemberInit)

        remoteBob = aliceChannel?.members?.first { it.id == bob!!.id } as RemoteMember
    }

    @After
    fun tearDown() {
        Log.d(TAG, "SkyWayContext.dispose()")
        SkyWayContext.dispose()
    }


//    -----------------TEST-----------------

    @Test
    fun updateToken() = runBlocking {
        val isUpdated = SkyWayContext.updateAuthToken(TestUtil.authToken)
        Assert.assertTrue(isUpdated)
    }
}
