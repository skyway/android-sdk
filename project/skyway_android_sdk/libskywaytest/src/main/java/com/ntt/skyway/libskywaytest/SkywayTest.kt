package com.ntt.skyway.libskywaytest

import android.content.Context
import com.ntt.skyway.core.network.HttpClient
import com.ntt.skyway.core.network.WebSocketClientFactory
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

object SkywayTest {

    init {
        try {
            System.loadLibrary("nativetest")
        } catch (e: UnsatisfiedLinkError) {
            Logger.logE(e.message.toString())
        }
    }

     suspend fun startTest(applicationContext: Context): Int =
        withContext(Dispatchers.Default) {
            val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setInjectableLogger({ _, _, _ -> }, Logging.Severity.LS_NONE)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            return@withContext startTestNative(
                applicationContext,
                HttpClient,
                WebSocketClientFactory,
                Logger)
        }

    private external fun startTestNative(
        context: Context,
        httpClient: HttpClient,
        webSocketClient: WebSocketClientFactory,
        logger: Logger
    ): Int

}
