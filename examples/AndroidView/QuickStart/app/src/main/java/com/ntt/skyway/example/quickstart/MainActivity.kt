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
import android.widget.TextView
import android.widget.Toast
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream

class MainActivity : AppCompatActivity() {
    private val option = SkyWayContext.Options(
        logLevel = Logger.LogLevel.VERBOSE
    )
    private val appId = TODO("replace your app id here")
    private val secretKey = TODO("replace your secret key here")
    private val scope = CoroutineScope(Dispatchers.IO)
    private var localRoomMember     : LocalRoomMember?  = null
    private var room                : P2PRoom?          = null
    private var localVideoStream    : LocalVideoStream? = null
    private var localAudioStream    : LocalAudioStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UIの初期化
        initUI()

        // 権限の要求
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

        val btnJoinRoom = findViewById<Button>(R.id.joinButton)
        btnJoinRoom.setOnClickListener {
            joinAndPublish()
        }
    }

    private fun initUI() {
        val roomName = findViewById<TextView>(R.id.roomName)
        roomName.text = UUID.randomUUID().toString()
    }
    private fun joinAndPublish() {

        scope.launch() {
            val result = SkyWayContext.setupForDev(applicationContext, appId, secretKey, option)
            if (result) {
                Log.d("App", "Setup succeed")
            }

            // cameraリソースの取得
            val device = CameraSource.getFrontCameras(applicationContext).first()

            // camera映像のキャプチャを開始します
            val cameraOption = CameraSource.CapturingOptions(800, 800)
            CameraSource.startCapturing(applicationContext, device, cameraOption)

            // 描画やpublishが可能なStreamを作成します
            localVideoStream = CameraSource.createStream()

            // SurfaceViewRenderer を取得して描画します。
            runOnUiThread {
                val localVideoRenderer = findViewById<SurfaceViewRenderer>(R.id.local_renderer)
                localVideoRenderer.setup()
                localVideoStream!!.addRenderer(localVideoRenderer)
            }

            // AudioSourceがオブジェクトであることに注意
            AudioSource.start()

            // publishが可能なStreamを作成します
            localAudioStream = AudioSource.createStream()
            room = P2PRoom.findOrCreate(name = findViewById<EditText>(R.id.roomName).text.toString())
            val memberInit = RoomMember.Init(name = "member_" + UUID.randomUUID())
            localRoomMember = room?.join(memberInit)

            val resultMessage = if (localRoomMember == null) "Join failed" else "Joined room"
            runOnUiThread {
                Toast.makeText(applicationContext, resultMessage, Toast.LENGTH_SHORT)
                    .show()
            }

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

            localRoomMember?.publish(localVideoStream!!)
            localRoomMember?.publish(localAudioStream!!)
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
                    else -> {}
                }
            }
        }
    }
}