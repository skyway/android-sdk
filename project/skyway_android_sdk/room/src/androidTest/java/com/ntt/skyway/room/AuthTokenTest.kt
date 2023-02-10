package com.ntt.skyway.room

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.room.member.RemoteRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.util.TestUtil
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

    private var alice: LocalP2PRoomMember? = null
    private var remoteBob: RemoteRoomMember? = null
    private var bob: LocalP2PRoomMember? = null
    private var aliceRoom: P2PRoom? = null
    private var bobRoom: P2PRoom? = null

    private lateinit var aliceLocalVideoStream: LocalVideoStream
    private lateinit var bobLocalVideoStream: LocalVideoStream

    @Before
    fun setup() = runBlocking{
        TestUtil.setupSkyway(SkyWayContext.Token(tokenReminderTimeSec = 8))

        aliceLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()
        bobLocalVideoStream = CustomVideoFrameSource(800, 800).createStream()

        aliceRoom = P2PRoom.create(null, null)

        bobRoom = P2PRoom.find(null, id = aliceRoom?.id)
        val bobMemberInit = RoomMember.Init(name = UUID.randomUUID().toString(), metadata = "")
        bob = bobRoom?.join(bobMemberInit)


        val aliceMemberInit = RoomMember.Init(name = UUID.randomUUID().toString(), metadata = "")
        alice = aliceRoom?.join(aliceMemberInit)

        remoteBob = aliceRoom?.members?.first { it.id == bob!!.id } as RemoteRoomMember
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
