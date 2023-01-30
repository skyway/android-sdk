package com.ntt.skyway.room

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.ScreenShareService
import com.ntt.skyway.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.adapter.RecyclerViewAdapterRoomPublication
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.databinding.ActivityRoomDetailsBinding
import com.ntt.skyway.listener.RoomPublicationAdapterListener
import com.ntt.skyway.manager.RoomManager
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomDetailsActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityRoomDetailsBinding

    companion object {
        const val CAPTURE_PERMISSION_REQUEST_CODE = 1002
    }

    var mService: ScreenShareService? = null

    // Boolean to check if our activity is bound to service or not
    var mIsBound: Boolean = false

    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterRoomPublication: RecyclerViewAdapterRoomPublication? = null

    private var membersLiveData = MutableLiveData<MutableList<RoomMember>>()
    private var members = arrayListOf<RoomMember>()

    private var publicationsLivaData = MutableLiveData<MutableList<RoomPublication>>()
    private var publications = arrayListOf<RoomPublication>()

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()

        scope.launch(Dispatchers.Main) {
            initSurfaceViews()
        }
    }

    private fun initUI() {
        binding.roomName.text = RoomManager.room?.name
        binding.memberName.text = RoomManager.localPerson?.name

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

        membersLiveData.observe(this) {
            recyclerViewAdapterRoomMember?.setData(it)
        }

        RoomManager.room?.members?.forEach { members.add(it) }
        membersLiveData.value = members

        RoomManager.localPerson?.onPublicationListChangedHandler = {
            Log.d(tag, "localPerson onPublicationListChangedHandler")
        }

        RoomManager.localPerson?.onSubscriptionListChangedHandler = {
            Log.d(tag, "localPerson onSubscriptionListChangedHandler")
        }

        recyclerViewAdapterRoomPublication =
            RecyclerViewAdapterRoomPublication(object : RoomPublicationAdapterListener {
                override fun onSubscribeClick(publicationId: String) {
                    scope.launch(Dispatchers.Main) {
                        subscription = RoomManager.localPerson?.subscribe(publicationId)
                        when (subscription?.contentType) {
                            Stream.ContentType.VIDEO -> {
                                (subscription?.stream as RemoteVideoStream).addRenderer(binding.remoteRenderer)
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

                override fun onUnSubscribeClick() {
                    scope.launch(Dispatchers.Main) {
                        RoomManager.localPerson?.unsubscribe(subscription?.id!!)
                    }
                }

                override fun onEnableClick(publicationId: String) {
                    scope.launch(Dispatchers.Main) {
                        val publication =
                            RoomManager.room?.publications?.find { it.id == publicationId }
                        publication?.enable()
                    }
                }

                override fun onDisableClick(publicationId: String) {
                    scope.launch(Dispatchers.Main) {
                        val publication =
                            RoomManager.room?.publications?.find { it.id == publicationId }
                        publication?.disable()
                    }
                }
            })

        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterRoomPublication

        publicationsLivaData.observe(this) {
            recyclerViewAdapterRoomPublication?.setData(it)
        }

        RoomManager.room?.publications?.forEach { publications.add(it) }
        publicationsLivaData.value = publications

        initButtons()
        initRoomFunctions()
    }


    private fun initSurfaceViews() {
        binding.localRenderer.setup()
        binding.remoteRenderer.setup()

        val device = CameraSource.getFrontCameras(applicationContext).first()
        CameraSource.startCapturing(
            applicationContext,
            device,
            CameraSource.CapturingOptions(800, 800)
        )
        localVideoStream = CameraSource.createStream()
        localVideoStream?.addRenderer(binding.localRenderer)
    }

    private fun initButtons() {
        binding.btnLeaveRoom.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                RoomManager.room!!.leave(RoomManager.localPerson!!)
                onBackPressed()
            }

        }

        binding.btnPublish.setOnClickListener {
            publishCameraVideoStream()
        }

        binding.btnUnPublish.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                publication?.let {
                    RoomManager.localPerson?.unpublish(it)
                }
            }
        }

        binding.btnScreenShare.setOnClickListener {
            startScreenCapture()
        }

        binding.btnAudio.setOnClickListener {
            publishAudioStream()
        }

        binding.btnPublishData.setOnClickListener {
            publishDataStream()
        }

        binding.btnSendData.setOnClickListener {
            val text = binding.textData.text.toString()
            localDataStream?.write(text)
//                localDataStream?.write(text.toByteArray()) // to send ByteArray()
        }
    }

    private fun initRoomFunctions() {
        RoomManager.room?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                runOnUiThread {
                    this@RoomDetailsActivity.members.clear()
                    membersLiveData.value = RoomManager.room?.members?.toMutableList()
                }
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                runOnUiThread {
                    this@RoomDetailsActivity.publications.clear()
                    publicationsLivaData.value = RoomManager.room?.publications?.toMutableList()
                }
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished: ${it.id}")
            }
        }
    }

    private fun publishCameraVideoStream() {
        Log.d(tag, "publishCameraVideoStream()")
//        val encoding = Encoding("low", 200_000, 4.0)
        // val codec = Codec(Codec.MimeType.AV1)
        val options = RoomPublication.Options(
            // codecCapabilities = mutableListOf(codec)
        )
        scope.launch(Dispatchers.Main) {
            publication = RoomManager.localPerson?.publish(localVideoStream!!, options)
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
        val options = RoomPublication.Options(
            // codecCapabilities = mutableListOf(Codec(Codec.MimeType.RED))
        )
        scope.launch(Dispatchers.Main) {
            publication = RoomManager.localPerson?.publish(localAudioStream, options)
        }
    }

    private fun publishDataStream() {
        val localDataSource = DataSource()
        localDataStream = localDataSource.createStream()
        val options = RoomPublication.Options()
        scope.launch(Dispatchers.Main) {
            publication = RoomManager.localPerson?.publish(localDataStream!!, options)
        }
    }

    private fun startScreenCapture() {
        startScreenShareService()
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        mMediaProjectionPermissionResultData: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, mMediaProjectionPermissionResultData)
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) return

        val screenShareVideoStream =
            mService?.getScreenShareStream(mMediaProjectionPermissionResultData!!)
        localVideoStream?.removeRenderer(binding.localRenderer)
        screenShareVideoStream?.addRenderer(binding.localRenderer)

        scope.launch(Dispatchers.Main) {
            publication = RoomManager.localPerson?.publish(screenShareVideoStream!!)
            publication?.let {
                addPublicationHandler(it)
            }
        }
    }


    private fun startScreenShareService() {
        val myServiceIntent = Intent(this, ScreenShareService::class.java)
        ContextCompat.startForegroundService(this, myServiceIntent)
        bindService()
    }

    private fun stopScreenShareService() {
        val serviceIntent = Intent(this, ScreenShareService::class.java)
        stopService(serviceIntent)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d(tag, "ServiceConnection: connected to service.")
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            val binder = iBinder as ScreenShareService.ScreenShareServiceMyBinder
            mService = binder.service
            mIsBound = true
            // getRandomNumberFromService() // return a random number from the service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(tag, "ServiceConnection: disconnected from service.")
            mIsBound = false
        }
    }

    private fun bindService() {
        Intent(this, ScreenShareService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenShareService()
    }

    private fun addPublicationHandler(publication: RoomPublication) {
        publication.apply {
            val tag = publication.javaClass.simpleName

            onMetadataUpdatedHandler = {
                Log.d(tag, "onMetadataUpdated")
            }

            onUnpublishedHandler = {
                Log.d(tag, "onUnpublished")
            }

            onSubscribedHandler = {
                Log.d(tag, "onSubscribed")
            }

            onUnsubscribedHandler = {
                Log.d(tag, "onUnsubscribed")
            }

            onSubscriptionListChangedHandler = {
                Log.d(tag, "onSubscriptionListChanged")
            }
        }
    }

}
