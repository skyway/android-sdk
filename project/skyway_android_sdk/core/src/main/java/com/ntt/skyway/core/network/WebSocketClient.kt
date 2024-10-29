package com.ntt.skyway.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.*

/**
 *  @suppress
 */
class WebSocketClient {
    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private var nativePointer: Long = 0L
    private val jobManager = JobManager(CoroutineScope(Dispatchers.IO + SupervisorJob()))

    private val webSocketListener: WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            jobManager.launchJob {
                nativeOnConnect(nativePointer)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            jobManager.launchJob {
                nativeOnMessage(nativePointer, text)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            jobManager.launchJob {
                nativeOnClose(nativePointer, code)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            jobManager.launchJob {
                nativeOnError(nativePointer, response?.code ?: 0)
            }
        }
    }

    private fun connect(url: String, subProtocols: Array<String>, headers: Array<WebSocketHeader>, nativePointer: Long) {
        this.nativePointer = nativePointer
        val request = Request.Builder().apply {
            this.addHeader("Sec-WebSocket-Protocol", subProtocols.joinToString())
            headers.forEach {
                this.addHeader(it.key, it.value)
            }
        }.url(url).build()
        ws = client.newWebSocket(request, webSocketListener)
    }

    private fun send(text: String) {
        ws?.send(text)
    }

    private fun close(code: Int, reason: String): Boolean {
        return runBlocking {
            if(reason == "destroy") {
                jobManager.terminateAllJobs()
            }
            return@runBlocking ws?.close(code, reason) ?: false
        }
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
