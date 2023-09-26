package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*
import java.util.*

// Test fails because simulcast is not supported
class SFURoomSimulcastAutoTask(listener: Listener, params: Params) :
    SFURoomTaskBase(listener, params) {

    override val TAG = this.javaClass.simpleName

    @OptIn(SkyWayOptIn::class)
    override fun run() {
        GlobalScope.launch(Dispatchers.Default) {
            initTimeout()

            val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.ENABLE)
            SkyWayContext._updateRtcConfig(rtcConfig)

            joinTask()
            if (localSFURoomMember == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "join failed")
                return@launch
            }

            localSFURoomMember?.onPublicationSubscribedHandler = {
                onPublicationSubscribedHandler(it)
            }
            sfuRoom?.onStreamPublishedHandler = {
                subscribe(it, RoomSubscription.Options("low"))
            }
            sfuRoom?.publications?.forEach {
                subscribe(it, RoomSubscription.Options("low"))
            }

            val options = RoomPublication.Options(
                encodings = mutableListOf(
                    Encoding(
                        null,
                        10_000,
                        null
                    ), // TODO: Remove this section when Simulcast supports it.
                    Encoding("low", 10_000, null),
                    Encoding("high", 300_000, null),
                )
            )
            val publicationLocalVideoStream =
                localSFURoomMember?.publish(getCameraStream(), options)
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish failed")
                return@launch
            }
            listener.onPhaseUpdateHandler = {
                if (phase == HIGHRATE_PHASE_NUMBER) {
                    runBlocking {
                        for (subscription in localSFURoomMember!!.subscriptions) {
                            subscription.changePreferredEncoding("high")
                        }
                    }
                }
            }
        }
    }

    fun onPublicationSubscribedHandler(it: RoomSubscription) {
        if (it.contentType == Stream.ContentType.VIDEO) {
            checkBitrate(100_000, 150_000, it)
            listener.onSubscribeHandler?.invoke(it)
        }
    }
}
