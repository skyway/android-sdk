package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Test fails because simulcast is not supported
class SFURoomSimulcastAutoTask(listener: Listener, params: Params) :
    SFURoomTaskBase(listener, params) {

    override val TAG: String = this.javaClass.simpleName

    @OptIn(SkyWayOptIn::class, DelicateCoroutinesApi::class)
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

            sfuRoom?.apply {
                onStreamPublishedHandler = { pub ->
                    val subscription = subscribe(pub, RoomSubscription.Options("low"))
                    subscription?.let {
                        if (it.contentType == Stream.ContentType.VIDEO) {
                            checkBitrate(100_000, 150_000, it)
                            listener.onSubscribeHandler?.invoke(it)
                        }
                    }
                }
                publications.forEach { pub ->
                    val subscription = subscribe(pub, RoomSubscription.Options("low"))
                    subscription?.let {
                        if (it.contentType == Stream.ContentType.VIDEO) {
                            checkBitrate(100_000, 150_000, it)
                            listener.onSubscribeHandler?.invoke(it)
                        }
                    }
                }
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
}
