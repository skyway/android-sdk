package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomPublication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class P2PRoomEncodingAutoTask(listener: Listener, params: Params) :
    P2PRoomTaskBase(listener, params) {

    override val TAG: String = this.javaClass.simpleName

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
                            listener.onSubscribeHandler?.invoke(subscription)
                            checkBitrate(100_000, 150_000, it)
                        }
                    }
                }
                publications.forEach { pub ->
                    val subscription = subscribe(pub)
                    subscription?.let {
                        if (it.contentType == Stream.ContentType.VIDEO) {
                            listener.onSubscribeHandler?.invoke(subscription)
                            checkBitrate(100_000, 150_000, it)
                        }
                    }
                }
            }

            val options =
                RoomPublication.Options(encodings = mutableListOf(Encoding(null, 50_000, null)))
            val publicationLocalVideoStream =
                localP2PRoomMember?.publish(getCameraStream(), options)
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish failed")
                return@launch
            }
            listener.onPhaseUpdateHandler = {
                if (phase == HIGHRATE_PHASE_NUMBER) {
                    publicationLocalVideoStream.updateEncodings(
                        mutableListOf(
                            Encoding(
                                null,
                                300_000,
                                null
                            )
                        )
                    )
                }
            }
        }
    }
}
