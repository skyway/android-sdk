package com.ntt.skyway.room.util

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

object TestUtil {
    val TAG = this.javaClass.simpleName
    val authToken = "YOUR_TOKEN"

    suspend fun setupSkyway(token: SkyWayContext.Token? = null) {
        val logLevel = Logger.LogLevel.VERBOSE
        val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.TURN_ONLY)
        val option = SkyWayContext.Options(authToken, logLevel, rtcConfig = rtcConfig, token = token)

        var appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val result = SkyWayContext.setup(appContext, option) {
            Log.e(TAG, "Context ${it.message}")
        }
        if (result) {
            Log.d(TAG, "Setup succeed")
        }
    }

    suspend fun waitForBobChannelOnStreamPublishedHandler(bobRoom: Room): Boolean? {
        return withTimeoutOrNull(8000L) {
            suspendCancellableCoroutine { continuation ->
                bobRoom.onStreamPublishedHandler = {
                    continuation.resume(true, null)
                }
                // in case the coroutine gets cancelled - because of timeout or something else
                continuation.invokeOnCancellation {
                    Logger.logE("error:" + it?.message)
                    continuation.cancel(it)
                }
            }
        }
    }
}
