package com.ntt.skyway.core.network

import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    @OptIn(DelicateCoroutinesApi::class)
    private val coroutineContext = newSingleThreadContext("skyway-websocket")
    private val scope = CoroutineScope(coroutineContext)
    private val destroyMutex = Mutex()
    private val jobsScope = CoroutineScope(Dispatchers.IO)
    private val jobs = mutableListOf<Job>()

    private val webSocketListener: WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            jobsScope.launch {
                destroyMutex.withLock {
                    if (isDestroyed) {
                        return@launch
                    }
                    Logger.logD("Connected: ${this.hashCode()}")
                    updateState(ConnectionState.OPEN)
                    val job = scope.launch {
                        nativeOnConnect(nativePointer)
                    }
                    addJob(job)
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            jobsScope.launch {
                destroyMutex.withLock {
                    if (isDestroyed) {
                        return@launch
                    }
                    val job = scope.launch {
                        nativeOnMessage(nativePointer, text)
                    }
                    addJob(job)
                }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            jobsScope.launch {
                destroyMutex.withLock {
                    if (isDestroyed) {
                        return@launch
                    }
                    Logger.logD("Closed: $code $reason")
                    updateState(ConnectionState.CLOSED)
                    val job = scope.launch {
                        nativeOnClose(nativePointer, code)
                    }
                    addJob(job)
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            jobsScope.launch {
                destroyMutex.withLock {
                    if (isDestroyed) {
                        return@launch
                    }
                    Logger.logE("Error: $connectionState, ${this@WebSocketClient.hashCode()}, ${t.message}, $response")
                    updateState(ConnectionState.CLOSED)
                    val job = scope.launch {
                        nativeOnError(nativePointer, response?.code ?: 0)
                    }
                    addJob(job)
                }
            }
        }
    }

    private fun addJob(job: Job) {
        jobs.add(job)
    }

    private fun connect(url: String, subProtocols: Array<String>, headers: Array<WebSocketHeader>, nativePointer: Long) {
        Logger.logD("Connect start: ${this.hashCode()}")
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
        ws?.send(text)
    }

    private fun close(code: Int, reason: String): Boolean {
        Logger.logD("Close start: ${this.hashCode()}, $code, $reason")
        return runBlocking {
            if(reason == "destroy") {
                destroyMutex.withLock {
                    isDestroyed = true
                    jobs.forEach {
                        it.join()
                    }
                }
            }
            return@runBlocking ws?.close(code, reason) ?: false
        }
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
