package com.ntt.skyway.core.network

object WebSocketClientFactory {
    @JvmStatic
    fun create(): WebSocketClient {
        return WebSocketClient()
    }
}
