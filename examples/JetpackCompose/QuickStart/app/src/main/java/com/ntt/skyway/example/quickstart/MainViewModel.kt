package com.ntt.skyway.example.quickstart

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(): ViewModel() {
    private val option = SkyWayContext.Options(
        logLevel = Logger.LogLevel.VERBOSE
    )
    var applicationContext: Context? = null

    private val appId = TODO("replace your app id here")
    private val secretKey = TODO("replace your secret key here")
    private var localRoomMember: LocalRoomMember? = null
    private var room: Room? = null

    var localVideoStream by mutableStateOf<LocalVideoStream?>(null)
        private set
    var localAudioStream by mutableStateOf<LocalAudioStream?>(null)
        private set
    var remoteVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    fun joinAndPublish(roomName: String) {

        viewModelScope.launch() {
            val result = SkyWayContext.setupForDev(applicationContext!!, appId, secretKey, option)
            if (result) {
                Log.d("App", "Setup succeed")
            }

            // cameraリソースの取得
            val device = CameraSource.getFrontCameras(applicationContext!!).first()

            // camera映像のキャプチャを開始します
            val cameraOption = CameraSource.CapturingOptions(800, 800)
            CameraSource.startCapturing(applicationContext!!, device, cameraOption)

            // 描画やpublishが可能なStreamを作成します
            // Stateを更新され、MainScreenのSurfaceViewRendererより描画します
            withContext(Dispatchers.Main) {
                localVideoStream = CameraSource.createStream()
            }

            // AudioSourceがオブジェクトであることに注意
            AudioSource.start()

            // publishが可能なStreamを作成します
            localAudioStream = AudioSource.createStream()

            room = Room.findOrCreate(name = roomName)

            val memberInit = RoomMember.Init(name = "member_" + UUID.randomUUID())
            localRoomMember = room?.join(memberInit)

            val resultMessage = if (localRoomMember == null) "Join failed" else "Joined room"
            Toast.makeText(applicationContext, resultMessage, Toast.LENGTH_SHORT)
                .show()

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
        // Publicationをsubscribeします
        viewModelScope.launch {
            val subscription = localRoomMember?.subscribe(publication)
            subscription?.stream?.let { stream ->
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    withContext(Dispatchers.Main) {
                        remoteVideoStream = subscription.stream as RemoteVideoStream
                    }
                }
            }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            localRoomMember?.leave()
            room?.dispose()
            room = null
            localRoomMember = null
            localVideoStream = null
            localAudioStream = null
            remoteVideoStream = null
            SkyWayContext.dispose()
        }
    }
}