package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.RoomSubscription
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.*
import java.util.*

class SFURoomAudioVideoManualTask(listener: Listener, params: Params):
    SFURoomTaskBase(listener,params) {

    override val TAG = this.javaClass.simpleName

    var subscriptionCountMap:MutableMap<String,Int> = mutableMapOf()
    val SUCCESS_SUBSCRIPTION_COUNT = 2

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
                checkSubscriber(it.publisher!!)
            }
            sfuRoom?.publications?.forEach {
                subscribe(it)
                checkSubscriber(it.publisher!!)
            }

            val publicationLocalAudioStream = localSFURoomMember?.publish(getAudioStream())
            if (publicationLocalAudioStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId,"publish audio failed")
                return@launch
            }

            val publicationLocalVideoStream = localSFURoomMember?.publish(getCameraStream())
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId,"publish video failed")
                return@launch
            }
        }
    }

    private fun checkSubscriber(member: RoomMember){
        if(!subscriptionCountMap.contains(member.id)){
            subscriptionCountMap[member.id] = 0
        }
        subscriptionCountMap[member.id]=subscriptionCountMap[member.id]!!+1
        if(subscriptionCountMap[member.id] == SUCCESS_SUBSCRIPTION_COUNT){
            listener.onTaskSuccessHandler?.invoke(member.id,params.requestId,params.taskId)
        }
    }

    fun onPublicationSubscribedHandler(it: RoomSubscription) {
        if (it.contentType == Stream.ContentType.VIDEO) {
            listener.onSubscribeHandler?.invoke(it)
        }
    }
}
