package com.example.regressiontest.task

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*
import java.util.*

class P2PRoomReplaceStreamAutoTask(listener: Listener, params: Params) :
    P2PRoomTaskBase(listener, params) {

    override val TAG = this.javaClass.simpleName

    @OptIn(SkyWayOptIn::class)
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

            localP2PRoomMember?.onPublicationSubscribedHandler = {
                onPublicationSubscribedHandler(it)
            }
            p2PRoom?.onStreamPublishedHandler = {
                subscribe(it)
            }
            p2PRoom?.publications?.forEach {
                subscribe(it)
            }

            val publicationLocalVideoStream = localP2PRoomMember?.publish(getLowRateVideoStream())
            if (publicationLocalVideoStream == null) {
                listener.onTaskFailedHandler?.invoke(params.requestId, "publish failed")
                return@launch
            }

            listener.onPhaseUpdateHandler = {
                if (phase == HIGHRATE_PHASE_NUMBER) {
                    publicationLocalVideoStream.replaceStream(getCameraStream(1200, 1200))
                }
            }
        }
    }

    private fun getLowRateVideoStream(): LocalVideoStream {
        val videoSource = CustomVideoFrameSource(800, 800)

        val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val handler = Handler(Looper.getMainLooper())
        val gray = Paint()
        gray.color = android.graphics.Color.GRAY
        handler.post(object : java.lang.Runnable {
            override fun run() {
                canvas.drawRect(0F, 0F, 800F, 800F, gray)
                videoSource.updateFrame(bitmap, 0)

                // Wait 60ms to adjust bitrate
                handler.postDelayed(this, 60)
            }
        })
        return videoSource.createStream()
    }

    fun onPublicationSubscribedHandler(it: RoomSubscription) {
        if (it.contentType == Stream.ContentType.VIDEO) {
            checkBitrate(100_000, 150_000, it)
            listener.onSubscribeHandler?.invoke(it)
        }
    }
}
