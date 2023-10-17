package com.ntt.skyway

import android.app.Application
import android.content.Context
import android.util.Log
import com.ntt.skyway.authtoken.AuthTokenBuilder
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.manager.ChannelManager
import com.ntt.skyway.manager.Manager
import com.ntt.skyway.manager.RoomManager
import com.ntt.skyway.manager.SFURoomManager
import com.ntt.skyway.plugin.sfuBot.SFUBotPlugin
import kotlinx.coroutines.runBlocking

class App : Application() {
    companion object {
        lateinit var appContext: Context
        private val logLevel = Logger.LogLevel.VERBOSE
        private val authToken = AuthTokenBuilder.CreateToken(
            BuildConfig.APP_ID,
            BuildConfig.SECRET_KEY
        )
        val option = SkyWayContext.Options(
            authToken,
            logLevel,
            rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.ENABLE),
//            webRTCLog = true
        )
        val channelManager = ChannelManager()
        val roomManager = RoomManager()
        val sfuManager = SFURoomManager()
        var currentManager: Manager = channelManager

        fun showMessage(message:String){
//            Toast.makeText(appContext, message, Toast.LENGTH_SHORT)
//                .show()
            Log.d("ReferenceApp",message)
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        runBlocking {
            SkyWayContext.registerPlugin(SFUBotPlugin())
            SkyWayContext.onReconnectStartHandler = {
                showMessage("onReconnectStartHandler")
            }
            SkyWayContext.onReconnectSuccessHandler = {
                showMessage("onReconnectSuccessHandler")
            }
            SkyWayContext.onErrorHandler = {
                showMessage("onErrorHandler : ${it.message}")
            }
            Logger.onLogHandler = { logLevel: Logger.LogLevel, s: String ->
//                Log.e("onLogHandler", "$logLevel $s")
            }
            val result = SkyWayContext.setup(applicationContext, option)
            if (result) {
                showMessage("setup success")
            }
        }
    }
}
