package com.example.regressiontest

import android.app.Application
import android.content.Context
import android.util.Log
import com.ntt.skyway.BuildConfig
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.plugin.sfuBot.SFUBotPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : Application() {
    companion object {
        lateinit var appContext: Context
        private val logLevel = Logger.LogLevel.VERBOSE
        private val authToken = AuthTokenBuilder.CreateToken(
            BuildConfig.APP_ID,
            BuildConfig.SECRET_KEY
        )
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.ENABLE)
        val option = SkyWayContext.Options(authToken, logLevel, rtcConfig = rtcConfig)
        SkyWayContext.registerPlugin(SFUBotPlugin())
        GlobalScope.launch(Dispatchers.Main) {
            Log.e("App", "setup")
            val result = SkyWayContext.setup(applicationContext, option)
            if (result) {
                Log.d("App", "Setup succeed")
            }
        }
    }
}
