package com.ntt.skyway.core.network

/**
 *  @suppress
 */
object WebSocketClientFactory {
    @JvmStatic
    fun create(): WebSocketClient {
        return WebSocketClient()
    }
}
