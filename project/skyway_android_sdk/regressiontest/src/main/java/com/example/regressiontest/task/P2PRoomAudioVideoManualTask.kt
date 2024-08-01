package com.example.regressiontest.task

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class P2PRoomAudioVideoManualTask(listener: Listener, params: Params) :
    P2PRoomTaskBase(listener, params) {

    override val TAG: String = this.javaClass.simpleName
    val SUCCESS_SUBSCRIPTION_COUNT = 2

    var subscriptionCountMap: MutableMap<String, Int> = mutableMapOf()

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
                            listener.onSubscribeHandler?.invoke(it)
                        }
                    }
                    checkSubscriber(pub.publisher!!)
                }
                publications.forEach { pub ->
                    val subscription = subscribe(pub)
                    subscription?.let {
                        if (it.contentType == Stream.ContentType.VIDEO) {
                            listener.onSubscribeHandler?.invoke(it)
                        }
                    }
                    checkSubscriber(pub.publisher!!)
                }
            }

            val publicationLocalAudioStream = localP2PRoomMember?.publish(getAudioStream())
            if (publicationLocalAudioStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish audio failed")
                return@launch
            }

            val publicationLocalVideoStream = localP2PRoomMember?.publish(getCameraStream())
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish video failed")
                return@launch
            }
        }
    }

    private fun checkSubscriber(member: RoomMember) {
        if (!subscriptionCountMap.contains(member.id)) {
            subscriptionCountMap[member.id] = 0
        }
        subscriptionCountMap[member.id] = subscriptionCountMap[member.id]!! + 1
        if (subscriptionCountMap[member.id] == SUCCESS_SUBSCRIPTION_COUNT) {
            listener.onTaskSuccessHandler?.invoke(member.id, params.requestId, params.taskId)
        }
    }
}
