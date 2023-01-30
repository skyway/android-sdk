package com.ntt.skyway.core.network

internal object WebSocketClientFactory {
    @JvmStatic
    fun create(): WebSocketClient {
        return WebSocketClient()
    }
}
