package com.ntt.skyway.example.sfuroom

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = this.javaClass.simpleName
    private val applicationContext = getApplication<Application>().applicationContext
    private var isSkyWayContextSetupDone = false
    private val option = SkyWayContext.Options(
        authToken = TODO("replace your auth token here"),
        logLevel = Logger.LogLevel.VERBOSE
    )

    private var room: Room? = null
    var localPerson: LocalRoomMember? = null

    var joinedRoom by mutableStateOf(false)
        private set

    var localMemberName by mutableStateOf("")
        private set

    var localVideoStream by mutableStateOf<LocalVideoStream?>(null)
        private set

    var remoteVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    var localDataStream by mutableStateOf<LocalDataStream?>(null)
        private set

    var roomMembers by mutableStateOf(emptyList<RoomMember>())
        private set

    var roomPublications by mutableStateOf(emptyList<RoomPublication>())
        private set

    var roomSubscriptions by mutableStateOf(emptyList<RoomSubscription>())
        private set

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null

    fun joinRoom(roomName: String, memberName: String, roomType: Room.Type) {
        viewModelScope.launch {
            setupSkyWayContextIfNecessary()
            if (roomType == Room.Type.P2P) {
                room = P2PRoom.findOrCreate(roomName)
            } else if (roomType == Room.Type.SFU) {
                room = SFURoom.findOrCreate(roomName)
            }
            if(room == null) {
                Toast.makeText(applicationContext,"Room findOrCreate failed", Toast.LENGTH_SHORT).show()
                return@launch
            }
            Log.d(tag, "findOrCreate Room id: " + room?.id)
            Log.d(tag, "Room type: " + room?.type)
            Toast.makeText(applicationContext,"Room findOrCreate OK", Toast.LENGTH_SHORT).show()
            val memberInit: RoomMember.Init = if(memberName.isEmpty()){
                RoomMember.Init(UUID.randomUUID().toString())
            } else {
                RoomMember.Init(memberName)
            }

            localPerson = room!!.join(memberInit)
            if (localPerson != null) {
                Log.d(tag, "localPerson: " + localPerson?.id)

                joinedRoom = true
                localMemberName = localPerson!!.name!!
                setupLocalPersonCallback()
                setupRoomFunctions()
                startCameraCapture()
                autoPublishAudioVideo()
                autoSubscribe()
            } else {
                Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupLocalPersonCallback() {
        localPerson?.apply {
            onPublicationListChangedHandler = {
                Log.d(tag, "localPerson onPublicationListChangedHandler")
            }
            onSubscriptionListChangedHandler = {
                Log.d(tag, "localPerson onSubscriptionListChangedHandler")
            }
        }
    }

    private fun setupRoomFunctions() {
        room?.apply {
            members.toList().let {
                roomMembers = it
                Log.d(tag, "members: $it")
            }
            publications.toList().let {
                roomPublications = it
                Log.d(tag, "publications: $it")
            }
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                //TODO main thread
                roomMembers = room!!.members.toList()
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                //TODO main thread
                roomPublications = room!!.publications.toList()
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished: ${it.id}")
            }
        }
    }



    private fun startCameraCapture() {
        val device = CameraSource.getFrontCameras(applicationContext).first()
        CameraSource.startCapturing(
            applicationContext,
            device,
            CameraSource.CapturingOptions(800, 800)
        )
        localVideoStream = CameraSource.createStream()
    }

    private fun autoPublishAudioVideo() {
        publishCameraVideoStream()
        publishAudioStream()
    }

    private fun publishCameraVideoStream() {
        viewModelScope.launch {
            publication = localVideoStream?.let { localPerson?.publish(it) }
            Log.d(tag, "publication state: ${publication?.state}")

            publication?.onEnabledHandler = {
                Log.d(tag, "onEnabledHandler ${publication?.state}")
            }

            publication?.onDisabledHandler = {
                Log.d(tag, "onDisabledHandler ${publication?.state}")
            }
        }
    }

    private fun publishAudioStream() {
        Log.d(tag, "publishAudioStream()")
        AudioSource.start()
        val localAudioStream = AudioSource.createStream()
        val options = RoomPublication.Options()
        viewModelScope.launch {
            publication = localPerson?.publish(localAudioStream, options)
        }
    }

    private fun autoSubscribe() {
        room?.publications?.forEach {
            subscribe(it)
        }
    }


    fun subscribe(publication: RoomPublication) {
        viewModelScope.launch {
            subscription = localPerson?.subscribe(publication.id)
            when (subscription?.contentType) {
                Stream.ContentType.VIDEO -> {
                    roomSubscriptions = roomSubscriptions + subscription!!
                }
                Stream.ContentType.AUDIO -> {
                    (subscription?.stream as RemoteAudioStream)
                }
                Stream.ContentType.DATA -> {
                    (subscription?.stream as RemoteDataStream).onDataHandler = {
                        Log.d(tag, "data received: $it")
                    }

                    (subscription?.stream as RemoteDataStream).onDataBufferHandler = {
                        Log.d(tag, "data received byte: ${it.contentToString()}")
                        Log.d(tag, "data received string: ${String(it)}")
                    }
                }
                null -> {

                }
            }
        }
    }

    fun unsubscribe() {
        viewModelScope.launch {
            localPerson?.unsubscribe(subscription?.id!!)
        }
    }

    fun unPublish(publication: RoomPublication) {
        viewModelScope.launch {
            localPerson?.unpublish(publication)
        }
    }

    private suspend fun setupSkyWayContextIfNecessary() {
        if (isSkyWayContextSetupDone) {
            return
        }
        val result = SkyWayContext.setup(applicationContext, option)
        if (result) {
            Log.d("App", "SkyWay Setup succeed")
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            if (localPerson != null) {
                room?.leave(localPerson!!)
                localPerson = null
                joinedRoom = false
            }
            roomSubscriptions = emptyList()

        }
    }
}