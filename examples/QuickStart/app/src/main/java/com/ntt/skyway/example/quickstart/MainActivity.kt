package com.ntt.skyway.example.quickstart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ntt.skyway.core.SkyWayContext
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
import android.Manifest
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteDataStream
import kotlin.math.floor

class MainActivity : AppCompatActivity() {
    private val option = SkyWayContext.Options(
        authToken = "YOUR_TOKEN",
        logLevel = Logger.LogLevel.VERBOSE
    )
    private val scope = CoroutineScope(Dispatchers.IO)
    private var localRoomMember: LocalRoomMember? = null
    private var localDataStream: LocalDataStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                ),
                0
            )
        }
    }

    private fun initUI() {
        val roomName = findViewById<EditText>(R.id.roomName)
        roomName.setText("room_" + (Math.random() * 10000).toInt().toString())

        val btnJoinRoom = findViewById<Button>(R.id.btnJoinRoom)
        btnJoinRoom.setOnClickListener {
            joinAndPublish()
        }

        val btnSendMessage = findViewById<Button>(R.id.btnSnedMessage)
        btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun joinAndPublish() {
        scope.launch {
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
            val roomName = findViewById<EditText>(R.id.roomName)
            val room = P2PRoom.findOrCreate(name = roomName.text.toString())
            val memberInit = RoomMember.Init(name = "member_" + UUID.randomUUID())
            localRoomMember = room?.join(memberInit)

            val resultMessage = if (localRoomMember == null) "Join failed" else "Joined room"
            runOnUiThread {
                Toast.makeText(applicationContext, resultMessage, Toast.LENGTH_SHORT)
                    .show()
            }

            localDataStream = DataSource().createStream()

            room?.publications?.forEach {
                if (it.publisher?.id == localRoomMember?.id) return@forEach
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
            localRoomMember?.publish(localAudioStream)
            localRoomMember?.publish(localDataStream!!)
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
                when (remoteStream?.contentType) {
                    // コンポーネントへの描画
                    Stream.ContentType.VIDEO -> (remoteStream as RemoteVideoStream).addRenderer(
                        remoteVideoRenderer
                    )
                    Stream.ContentType.DATA -> (remoteStream as RemoteDataStream).onDataHandler = {
                        Toast.makeText(applicationContext, "Received message", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun sendMessage() {
        localDataStream?.run {
            val message = findViewById<EditText>(R.id.message)
            write(message.text.toString())
            Toast.makeText(applicationContext, "Message sent", Toast.LENGTH_SHORT)
                .show()
        }
    }
}