package com.ntt.skyway.plugin.sfuBot.util

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.ntt.skyway.authtoken.AuthTokenBuilder
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.member.LocalPerson

object TestUtil {
    private val tag = this.javaClass.simpleName

    suspend fun setupSkyway() {
        val logLevel = Logger.LogLevel.VERBOSE
        val authToken = AuthTokenBuilder.CreateToken(
            com.ntt.skyway.plugin.sfuBot.BuildConfig.APP_ID,
            com.ntt.skyway.plugin.sfuBot.BuildConfig.SECRET_KEY
        )

        val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.TURN_ONLY)
        val option = SkyWayContext.Options(authToken, logLevel, rtcConfig = rtcConfig)

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val result = SkyWayContext.setup(appContext, option) {
            Log.e(tag, "Context ${it.message}")
        }
        if (result) {
            Log.d(tag, "Setup succeed")
        }
    }

    fun waitForFindSubscriptions(member: LocalPerson, publication: Publication): Boolean {
        repeat(10) {
            if (member.subscriptions.find { it.id == publication.id } != null) {
                return true
            }
            Thread.sleep(100)
        }
        return false
    }
}
