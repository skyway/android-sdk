package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*
import java.util.*

class P2PRoomCodecH264ManualTask(listener: Listener, params: Params) :
    P2PRoomTaskBase(listener, params) {

    override val TAG: String = this.javaClass.simpleName
    val SUCCESS_SUBSCRIPTION_CODEC = "video/h264"

    @OptIn(SkyWayOptIn::class, DelicateCoroutinesApi::class)
    override fun run() {
        GlobalScope.launch(Dispatchers.Default) {
            initTimeout()

            val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.ENABLE)
            SkyWayContext._updateRtcConfig(rtcConfig)

            joinTask()
            if (localP2PRoomMember == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "join failed")
                return@launch
            }

            p2PRoom?.apply {
                onStreamPublishedHandler = { pub ->
                    val subscription = subscribe(pub)
                    subscription?.let {
                        if (it.contentType == Stream.ContentType.VIDEO) {
                            checkCodec(it, SUCCESS_SUBSCRIPTION_CODEC)
                            listener.onSubscribeHandler?.invoke(it)
                        }
                    }
                }
                publications.forEach { pub ->
                    val subscription = subscribe(pub)
                    subscription?.let {
                        if (it.contentType == Stream.ContentType.VIDEO) {
                            checkCodec(it, SUCCESS_SUBSCRIPTION_CODEC)
                            listener.onSubscribeHandler?.invoke(it)
                        }
                    }
                }
            }

            val options =
                RoomPublication.Options(codecCapabilities = mutableListOf(Codec(Codec.MimeType.H264)))
            val publicationLocalVideoStream =
                localP2PRoomMember?.publish(getCameraStream(), options)
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish video failed")
                return@launch
            }
        }
    }
}
