package com.ntt.skyway.room.util

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.ntt.skyway.authtoken.AuthTokenBuilder
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

object TestUtil {
    val TAG = this.javaClass.simpleName
    val authToken = AuthTokenBuilder.CreateToken(
        com.ntt.skyway.room.BuildConfig.APP_ID,
        com.ntt.skyway.room.BuildConfig.SECRET_KEY
    )

    suspend fun setupSkyway(token: SkyWayContext.Token? = null) {
        val logLevel = Logger.LogLevel.VERBOSE
        val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.TURN_ONLY)
        val option =
            SkyWayContext.Options(authToken, logLevel, rtcConfig = rtcConfig, token = token)

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val result = SkyWayContext.setup(appContext, option) {
            Log.e(TAG, "Context ${it.message}")
        }
        if (result) {
            Log.d(TAG, "Setup succeed")
        }
    }

    suspend fun waitForFindSubscription(
        member: LocalRoomMember,
        publication: RoomPublication
    ): Boolean {
        repeat(10) {
            if (member.subscriptions.find { it.id == publication.id } != null) {
                return true
            }
            Thread.sleep(100)
        }
        return false
    }
}
