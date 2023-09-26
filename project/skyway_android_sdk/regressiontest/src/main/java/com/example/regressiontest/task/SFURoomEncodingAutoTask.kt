package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*
import java.util.*

class SFURoomEncodingAutoTask(listener: Listener, params: Params):
    SFURoomTaskBase(listener,params) {

    override val TAG = this.javaClass.simpleName

    @OptIn(SkyWayOptIn::class)
    override fun run() {
        GlobalScope.launch(Dispatchers.Default) {
            initTimeout()

            val rtcConfig = SkyWayContext.RtcConfig(policy = SkyWayContext.TurnPolicy.ENABLE)
            SkyWayContext._updateRtcConfig(rtcConfig)

            joinTask()

            if (localSFURoomMember == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId,"join failed")
                return@launch
            }

            localSFURoomMember?.onPublicationSubscribedHandler = {
                onPublicationSubscribedHandler(it)
            }
            sfuRoom?.onStreamPublishedHandler = {
                subscribe(it)
            }
            sfuRoom?.publications?.forEach {
                subscribe(it)
            }

            val options = RoomPublication.Options(encodings = mutableListOf(Encoding(null,50_000, null)))
            var publicationLocalVideoStream = localSFURoomMember?.publish(getCameraStream(), options)
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId,"publish failed")
                return@launch
            }
            listener.onPhaseUpdateHandler = {
                if(phase == HIGHRATE_PHASE_NUMBER){
                    publicationLocalVideoStream.updateEncodings(mutableListOf(Encoding(null, 300_000, null)))
                }
            }
        }
    }

    fun onPublicationSubscribedHandler(it: RoomSubscription) {
        if (it.contentType == Stream.ContentType.VIDEO) {
            checkBitrate(100_000,150_000, it)
            listener.onSubscribeHandler?.invoke(it)
        }
    }
}
