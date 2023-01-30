package com.ntt.skyway.example.quickstart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var localRoomMember: LocalRoomMember? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scope.launch {
            val option = SkyWayContext.Options(
                authToken = "YOUR_TOKEN",
                logLevel = Logger.LogLevel.VERBOSE
            )

            val result = SkyWayContext.setup(applicationContext, option)

            if (result) {
                Log.d("App", "Setup succeed")
            }

            val device = CameraSource.getFrontCameras(applicationContext).first()

            // camera映像のキャプチャを開始します
            val cameraOption = CameraSource.CapturingOptions(800, 800)
            CameraSource.startCapturing(applicationContext, device, cameraOption)

            // 描画やpublishが可能なStreamを作成します
            val localVideoStream = CameraSource.createStream()

            // SurfaceViewRenderer を取得して描画します。
            runOnUiThread {
                val localVideoRenderer = findViewById<SurfaceViewRenderer>(R.id.local_renderer)
                localVideoRenderer.setup()
                localVideoStream.addRenderer(localVideoRenderer)
            }

            AudioSource.start()

            // publishが可能なStreamを作成します
            val localAudioStream = AudioSource.createStream()

            val room = P2PRoom.findOrCreate(name = "room")
            val memberInit = RoomMember.Init(name = "member" + UUID.randomUUID())
            localRoomMember = room?.join(memberInit)

            room?.publications?.forEach {
                if(it.publisher?.id == localRoomMember?.id) return@forEach
                subscribe(it)
            }

            room?.onStreamPublishedHandler = Any@{
                Log.d("room", "onStreamPublished: ${it.id}")
                if (it.publisher?.id == localRoomMember?.id) {
                    return@Any
                }
                subscribe(it)
            }

            localRoomMember?.publish(localVideoStream)
        }
    }

    private fun subscribe(publication: RoomPublication) {
        scope.launch {
            // Publicationをsubscribeします
            val subscription = localRoomMember?.subscribe(publication)
            runOnUiThread {
                val remoteVideoRenderer =
                    findViewById<SurfaceViewRenderer>(R.id.remote_renderer)
                remoteVideoRenderer.setup()
                val remoteStream = subscription?.stream
                when(remoteStream?.contentType) {
                    // コンポーネントへの描画
                    Stream.ContentType.VIDEO -> (remoteStream as RemoteVideoStream).addRenderer(remoteVideoRenderer)
                    else -> {}
                }
            }
        }
    }
}