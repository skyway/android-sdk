package com.example.regressiontest.task

import com.example.regressiontest.App
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*
import org.json.JSONObject

abstract class TaskBase(protected val listener: Listener, val params: Params) {
    class Listener {
        var onTaskSuccessHandler: ((memberId: String, requestId: String, taskId: String) -> Unit)? =
            null
        var onTaskFailedHandler: ((requestId: String, message: String) -> Unit)? = null
        var onSubscribeHandler: ((subscription: RoomSubscription) -> Unit)? = null
        var onUpdateBitrateHandler: ((subscription: RoomSubscription, bitrate: Int) -> Unit)? = null
        var onPhaseUpdateHandler: ((phase: Int) -> Unit)? = null
    }

    class Params(
        var requestId: String,
        val taskId: String,
        val taskRoom: String,
        val clients: Int
    ) {}

    open val TAG = this.javaClass.simpleName
    private var timeout: Long = 20 * 1000

    private val LOWRATE_PHASE_NUMBER = 0
    protected val HIGHRATE_PHASE_NUMBER = 1

    protected var phase = 0

    protected var job: Job? = null
    protected var isClose = false

    abstract fun run()

    fun initTimeout() {
        GlobalScope.launch(Dispatchers.Default) {
            delay(timeout)
            listener.onTaskFailedHandler?.invoke(params.requestId, "timeout")
        }
    }

    fun newPhase(requestId: String) {
        phase++
        params.requestId = requestId
        listener.onPhaseUpdateHandler?.invoke(phase)
        initTimeout()
    }

    @OptIn(SkyWayOptIn::class)
    fun getByteReceived(subscription: RoomSubscription): Int {
        val getStats = subscription.getStats()
        val report =
            getStats?.reports?.filter { it.id.contains("InboundRTPVideo") || it.type == "inbound-rtp" }
        if (report?.size == 0) {
            return 0
        }
        val getStat = report?.get(0)
        return getStat?.params?.get("bytesReceived")?.toString()?.toInt() ?: 0
    }

    @OptIn(SkyWayOptIn::class)
    fun getCodec(subscription: RoomSubscription): String? {
        val getStats = subscription.getStats()
        val report = getStats?.reports?.filter { it.id.contains("RTCCodec") || it.id.contains("CIT01") }
        if (report?.size == 0) {
            return ""
        }
        val getStat = report?.get(0)
        return getStat?.params?.get("mimeType")?.toString()?.lowercase()?.replace("\"", "")
    }

    fun checkCodec(subscription: RoomSubscription, checkCodec: String) {
        job = GlobalScope.launch {
            if (isClose) return@launch
            var codec: String? = null
            var retryCount = 0
            while (codec.isNullOrBlank() && retryCount < 5) {
                codec = getCodec(subscription)
                if (codec.isNullOrBlank()) {
                    delay(500)
                    retryCount++
                }
            }

            if (!codec.isNullOrBlank()) {
                if (codec == checkCodec) {
                    listener.onTaskSuccessHandler?.invoke(
                        subscription.publication.publisher!!.id,
                        params.requestId,
                        params.taskId
                    )
                } else {
                    listener.onTaskFailedHandler?.invoke(
                        params.requestId,
                        "codec miss match : $codec(client) vs $checkCodec(expect)"
                    )
                }

                // finish this task
                return@launch
            }
        }
    }

    fun getCameraStream(width: Int = 800, height: Int = 800): LocalVideoStream {
        val cameraSource = CameraSource
        val device = CameraSource.getFrontCameras(App.appContext).first()
        CameraSource.startCapturing(
            App.appContext,
            device,
            CameraSource.CapturingOptions(width, height)
        )
        return cameraSource.createStream()
    }

    fun getAudioStream(): LocalAudioStream {
        return AudioSource.createStream()
    }

    fun checkBitrate(passingLowBit: Int, passingHighBit: Int, subscription: RoomSubscription) {
        job = GlobalScope.launch {
            var preBytesReceived = 0
            while (true) {
                if (isClose) return@launch
                val bytesReceived = getByteReceived(subscription)
                val bitrate = (bytesReceived - preBytesReceived) * 8
                preBytesReceived = bytesReceived

                listener.onUpdateBitrateHandler?.invoke(subscription, bitrate)

                if (phase == LOWRATE_PHASE_NUMBER) {
                    if (bitrate in 1..passingLowBit) {
                        listener.onTaskSuccessHandler?.invoke(
                            subscription.publication.publisher!!.id,
                            params.requestId,
                            params.taskId
                        )
                    }
                } else if (phase == HIGHRATE_PHASE_NUMBER) {
                    if (bitrate >= passingHighBit) {
                        listener.onTaskSuccessHandler?.invoke(
                            subscription.publication.publisher!!.id,
                            params.requestId,
                            params.taskId
                        )

                        // finish this task
                        return@launch
                    }
                }
                // Wait 1 second to measure bit rate
                delay(1000)
            }
        }
    }

    fun checkMessage(subscription: RoomSubscription) {
        subscription.stream?.let {
            (it as RemoteDataStream).onDataHandler = {
                val message = JSONObject(it)
                if (message["message"] == "message" && message.length() == 1) {
                    listener.onTaskSuccessHandler?.invoke(
                        subscription.publication.publisher!!.id,
                        params.requestId,
                        params.taskId
                    )
                }
            }
        }
    }

    open fun closeTask() {
        isClose = true
        job?.cancel()
    }
}
