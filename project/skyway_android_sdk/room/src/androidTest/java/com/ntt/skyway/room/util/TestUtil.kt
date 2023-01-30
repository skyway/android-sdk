package com.ntt.skyway.room.util

import android.Manifest
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import org.junit.Rule

object TestUtil {
    val TAG = this.javaClass.simpleName

    suspend fun setupSkyway() {
        val logLevel = Logger.LogLevel.VERBOSE
        val authToken = "YOUR TOKEN"
        val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.TURN_ONLY)
        val option = SkyWayContext.Options(authToken, logLevel, rtcConfig = rtcConfig)

        var appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val result = SkyWayContext.setup(appContext, option) {
            Log.e(TAG, "Context ${it.message}")
        }
        if (result) {
            Log.d(TAG, "Setup succeed")
        }
    }
}
