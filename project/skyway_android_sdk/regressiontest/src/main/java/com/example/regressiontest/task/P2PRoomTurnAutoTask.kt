package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*

class P2PRoomTurnAutoTask(listener: Listener, params: Params) :
    P2PRoomTaskBase(listener, params) {

    override val TAG = this.javaClass.simpleName

    @OptIn(SkyWayOptIn::class)
    override fun run() {
        GlobalScope.launch(Dispatchers.Default) {
            initTimeout()

            val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.TURN_ONLY)
            SkyWayContext._updateRtcConfig(rtcConfig)

            joinTask()
            if (localP2PRoomMember == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "join failed")
                return@launch
            }

            localP2PRoomMember?.onPublicationSubscribedHandler = {
                onPublicationSubscribedHandler(it)
            }
            p2PRoom?.onStreamPublishedHandler = {
                subscribe(it)
            }
            p2PRoom?.publications?.forEach {
                subscribe(it)
            }

            val publicationLocalDataStream = localP2PRoomMember?.publish(getDataTaskStream())
            if (publicationLocalDataStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish failed")
                return@launch
            }
        }
    }

    fun onPublicationSubscribedHandler(subscription: RoomSubscription) {
        if (subscription.contentType == Stream.ContentType.DATA) {
            checkMessage(subscription)
        }
    }
}
