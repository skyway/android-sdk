package com.ntt.skyway.compose.examples.pip

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.compose.examples.SkyWayCredentials
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * PiP サンプル専用の ViewModel。
 *
 * Roomへの入室、カメラ映像の Publish、相手が初回 Publish した映像の自動 Subscribe を行います。
 * PiP API や Activity API は扱わず、[PipStateHolder.isCallActive] を介して通話状態のみを連携します。
 */
class PipViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = this.javaClass.simpleName
    private val appContext = getApplication<Application>().applicationContext
    private var isSkyWayContextSetupDone = false
    private val appId = SkyWayCredentials.APP_ID
    private val secretKey = SkyWayCredentials.SECRET_KEY

    private val _joinedRoom = mutableStateOf(false)
    val joinedRoom: Boolean by _joinedRoom

    private val _localMemberName = mutableStateOf("")
    val localMemberName: String by _localMemberName

    private val _localVideoStream = mutableStateOf<LocalVideoStream?>(null)
    val localVideoStream: LocalVideoStream? by _localVideoStream

    private val _remoteVideoStream = mutableStateOf<RemoteVideoStream?>(null)
    val remoteVideoStream: RemoteVideoStream? by _remoteVideoStream

    private var room: Room? = null
    private var localMember: LocalRoomMember? = null
    private var videoPublication: RoomPublication? = null

    /** SkyWay セットアップ → カメラ起動・Publish → ルーム入室 */
    fun joinRoom(roomName: String, memberName: String) {
        viewModelScope.launch {
            setupSkyWayContextIfNecessary()
            if (!isSkyWayContextSetupDone) {
                Toast.makeText(appContext, "SkyWay setup failed", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val r = Room.findOrCreate(roomName) ?: run {
                Toast.makeText(appContext, "Room findOrCreate failed", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val init = RoomMember.Init(memberName.ifBlank { UUID.randomUUID().toString() })
            val member = r.join(init) ?: run {
                Toast.makeText(appContext, "Join failed", Toast.LENGTH_SHORT).show()
                r.dispose()
                return@launch
            }

            room = r
            localMember = member

            startCameraAndPublish()
            setupRoomHandlers(r)
            r.publications.forEach { subscribeIfRemoteVideo(it) }

            _localMemberName.value = member.name ?: member.id
            _joinedRoom.value = true

            // 通話中フラグ ON → MainActivity が BasicPictureInPicture.setEnabled(true) を呼ぶ
            PipStateHolder.isCallActive.value = true
            Log.d(tag, "Joined room: ${r.id}")
        }
    }

    /** ルームから退出します。 */
    fun leaveRoom() {
        viewModelScope.launch {
            // 離脱処理より先に無効化し、処理中の意図しない PiP 突入を防ぐ
            PipStateHolder.isCallActive.value = false

            localMember?.let { room?.leave(it) }
            room?.dispose()
            room = null
            localMember = null
            videoPublication = null
            _joinedRoom.value = false
            _remoteVideoStream.value = null
            CameraSource.stopCapturing()
            _localVideoStream.value = null
        }
    }

    private fun startCameraAndPublish() {
        val cameras = CameraSource.getFrontCameras(appContext)
        if (cameras.isEmpty()) {
            Log.w(tag, "Front camera not found.")
            return
        }
        CameraSource.startCapturing(
            appContext,
            cameras.first(),
            CameraSource.CapturingOptions(800, 800)
        )
        val stream = CameraSource.createStream()
        _localVideoStream.value = stream
        viewModelScope.launch {
            videoPublication = stream?.let { localMember?.publish(it) }
        }
    }

    private fun setupRoomHandlers(r: Room) {
        r.onStreamPublishedHandler = { publication ->
            Log.d(tag, "onStreamPublished: ${publication.id}")
            subscribeIfRemoteVideo(publication)
        }
    }

    /** 自分以外が Publish した最初の映像 Publication だけを自動で Subscribe します。 */
    private fun subscribeIfRemoteVideo(publication: RoomPublication) {
        if (publication.publisher?.id == localMember?.id) return
        if (publication.contentType != Stream.ContentType.VIDEO) return
        if (_remoteVideoStream.value != null) return

        viewModelScope.launch {
            val sub = localMember?.subscribe(publication.id) ?: return@launch
            _remoteVideoStream.value = sub.stream as? RemoteVideoStream
            Log.d(tag, "Subscribed: ${sub.id}")
        }
    }

    private suspend fun setupSkyWayContextIfNecessary() {
        if (isSkyWayContextSetupDone) return
        val result = SkyWayContext.setupForDev(
            context = appContext,
            appId = appId,
            secretKey = secretKey,
            option = SkyWayContext.Options(logLevel = Logger.LogLevel.VERBOSE)
        )
        if (result) {
            isSkyWayContextSetupDone = true
            Log.d(tag, "SkyWay Setup succeed")
        }
    }

    override fun onCleared() {
        // viewModelScope は破棄済みのため、ここでは suspend 関数を呼ばずに同期的に後始末する
        PipStateHolder.isCallActive.value = false
        CameraSource.stopCapturing()
        room?.dispose()
        super.onCleared()
    }
}
