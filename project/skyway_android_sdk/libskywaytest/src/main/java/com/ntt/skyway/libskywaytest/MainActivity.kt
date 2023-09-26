package com.ntt.skyway.libskywaytest

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.ntt.skyway.core.network.HttpClient
import com.ntt.skyway.core.network.WebSocketClientFactory
import com.ntt.skyway.core.util.Logger
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
            .setInjectableLogger({ _, _, _ -> }, Logging.Severity.LS_NONE)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        Logger.logLevel = Logger.LogLevel.VERBOSE

        startTest(applicationContext,
            HttpClient,
            WebSocketClientFactory,
            Logger
        )
        finishAndRemoveTask()
    }

    private external fun startTest(context: Context,
                                   httpClient: HttpClient,
                                   webSocketClient: WebSocketClientFactory,
                                   logger: Logger
    ): Int

    companion object {
        init {
            System.loadLibrary("nativetest")
        }
    }
}
