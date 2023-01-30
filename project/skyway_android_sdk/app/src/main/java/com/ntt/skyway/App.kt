package com.ntt.skyway

import android.app.Application
import android.util.Log
import android.content.Context
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.plugin.sfuBot.SFUBotPlugin
import kotlinx.coroutines.*

class App : Application() {
    companion object {
        lateinit var appContext: Context
        private val logLevel = Logger.LogLevel.VERBOSE
        private const val authToken = "YOUR TOKEN"
        internal val scope = CoroutineScope(Dispatchers.IO)
        internal var setupJob: Job? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.TURN_ONLY)
        val option = SkyWayContext.Options(authToken, logLevel, rtcConfig = rtcConfig)
        SkyWayContext.registerPlugin(SFUBotPlugin())

        setupJob = scope.launch {
            Log.e("App", "setup")
            val result = SkyWayContext.setup(applicationContext, option) {
                Log.e("App", "Context ${it.message}")
            }
            if (result) {
                Log.d("App", "Setup succeed")
            }
        }
    }
}
