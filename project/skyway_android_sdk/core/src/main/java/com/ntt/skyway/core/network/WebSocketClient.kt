package com.ntt.skyway.core.network

import com.ntt.skyway.core.util.Logger
import okhttp3.*


class WebSocketClient {
    enum class ConnectionState(val string: String) {
        ESTABLISHING("ESTABLISHING"),
        CONNECTING("CONNECTING"),
        OPEN("OPEN"),
        CLOSED("CLOSED")
    }

    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private var connectionState: ConnectionState = ConnectionState.ESTABLISHING
    private var nativePointer: Long = 0L
    private var isDestroyed = false

    private val webSocketListener: WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (isDestroyed) {
                return
            }
            Logger.logD("Connect")
            updateState(ConnectionState.OPEN)
            nativeOnConnect(nativePointer)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (isDestroyed) {
                return
            }
            Logger.logD("Receive Message: $text")
            nativeOnMessage(nativePointer, text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (isDestroyed) {
                return
            }
            Logger.logD("Closed: $code $reason")
            updateState(ConnectionState.CLOSED)
            nativeOnClose(nativePointer, code)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (isDestroyed) {
                return
            }
            Logger.logE("Error: $connectionState, ${this@WebSocketClient.hashCode()}, ${t.message}, $response")
            updateState(ConnectionState.CLOSED)
            nativeOnError(nativePointer, response?.code ?: 0)
        }
    }

    private fun connect(url: String, subProtocols: Array<String>, headers: Array<WebSocketHeader>, nativePointer: Long) {
        Logger.logD("Connect start")
        this.nativePointer = nativePointer
        updateState(ConnectionState.CONNECTING)
        val request = Request.Builder().apply {
            this.addHeader("Sec-WebSocket-Protocol", subProtocols.joinToString())
            headers.forEach {
                this.addHeader(it.key, it.value)
            }
        }.url(url).build()
        ws = client.newWebSocket(request, webSocketListener)
    }

    private fun send(text: String) {
        if (connectionState != ConnectionState.OPEN) {
            Logger.logW("Failed to send message. WebSocket is not opened")
            return
        }
        Logger.logD("Sending Message: $text")
        ws?.send(text)
    }

    private fun close(code: Int, reason: String): Boolean {
        Logger.logD("Close start: ${this.hashCode()}, $code, $reason")
        if(reason == "destroy") {
            isDestroyed = true
        }
        return ws?.close(code, reason) ?: false
    }

    private fun updateState(state: ConnectionState) {
        Logger.logD("Update status: $state, ${this.hashCode()}")
        connectionState = state
    }

    private fun createHeader(key: String, value: String): WebSocketHeader {
        return WebSocketHeader(key, value)
    }

    private fun createHeaderArray(length: Int): Array<WebSocketHeader> {
        return Array(length) { WebSocketHeader("", "") }
    }

    private external fun nativeOnConnect(nativePointer: Long)
    private external fun nativeOnMessage(nativePointer: Long, message: String)
    private external fun nativeOnClose(nativePointer: Long, code: Int)
    private external fun nativeOnError(nativePointer: Long, code: Int)
}
