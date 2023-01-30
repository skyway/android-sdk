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
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.databinding.ActivitySfuRoomDetailsBinding
import com.ntt.skyway.listener.RoomPublicationAdapterListener
import com.ntt.skyway.manager.SFURoomManager
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.*

class SFURoomDetailsActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivitySfuRoomDetailsBinding

    companion object {
        const val CAPTURE_PERMISSION_REQUEST_CODE = 1002
    }

    var mService: ScreenShareService? = null

    // Boolean to check if our activity is bound to service or not
    private var mIsBound: Boolean = false

    private var localVideoStream: LocalVideoStream? = null

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterRoomPublication: RecyclerViewAdapterRoomPublication? = null

    private var membersLiveData = MutableLiveData<MutableList<RoomMember>>()
    private var members = arrayListOf<RoomMember>()

    private var publicationsLivaData = MutableLiveData<MutableList<RoomPublication>>()
    private var publications = arrayListOf<RoomPublication>()

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null
    private var toggleEncodingId = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySfuRoomDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()
        initSurfaceViews()
    }

    private fun initUI() {
        binding.roomName.text = SFURoomManager.sfuRoom?.name
        binding.memberName.text = SFURoomManager.localPerson?.name

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

        membersLiveData.observe(this) {
            recyclerViewAdapterRoomMember?.setData(it)
        }

        SFURoomManager.sfuRoom?.members?.forEach { members.add(it) }
        membersLiveData.value = members

        SFURoomManager.localPerson?.onPublicationListChangedHandler = {
            Log.d(tag, "localPerson onPublicationListChangedHandler")
        }

        SFURoomManager.localPerson?.onSubscriptionListChangedHandler = {
            Log.d(tag, "localPerson onSubscriptionListChangedHandler")
        }

        recyclerViewAdapterRoomPublication =
            RecyclerViewAdapterRoomPublication(object : RoomPublicationAdapterListener {
                override fun onSubscribeClick(publicationId: String) {
                    scope.launch(Dispatchers.Main) {
                        subscription = SFURoomManager.localPerson?.subscribe(
                            publicationId,
                            RoomSubscription.Options("low")
                        )
                        (subscription?.stream as RemoteVideoStream).addRenderer(binding.remoteRenderer)
                    }

                }

                override fun onUnSubscribeClick() {
                    scope.launch(Dispatchers.Main) {
                        SFURoomManager.localPerson?.unsubscribe(subscription?.id!!)
                    }
                }

                override fun onEnableClick(publicationId: String) {
                    scope.launch(Dispatchers.Main) {
                        val publication =
                            SFURoomManager.sfuRoom?.publications?.find { it.id == publicationId }
                        publication?.enable()
                    }
                }

                override fun onDisableClick(publicationId: String) {
                    scope.launch(Dispatchers.Main) {
                        val publication =
                            SFURoomManager.sfuRoom?.publications?.find { it.id == publicationId }
                        publication?.disable()
                    }
                }

            })
        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterRoomPublication

        publicationsLivaData.observe(this) {
            recyclerViewAdapterRoomPublication?.setData(it)
        }

        SFURoomManager.sfuRoom?.publications?.forEach { publications.add(it) }
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
                SFURoomManager.sfuRoom!!.leave(SFURoomManager.localPerson!!)
                onBackPressed()
            }
        }

        binding.btnPublish.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                val options = RoomPublication.Options()
                publication = SFURoomManager.localPerson?.publish(localVideoStream!!, options)
                publication?.let { setRoomPublicationEventHandler(it) }
            }
        }

        binding.btnUnPublish.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                publication?.let {
                    SFURoomManager.localPerson?.unpublish(it)
                }
            }
        }

        binding.btnChangeEncoding.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                val sub = SFURoomManager.sfuRoom?.subscriptions?.find { it.id == subscription?.id }

                if (toggleEncodingId) {
                    sub?.changePreferredEncoding("high")
                    Log.d(tag, "$tag changePreferredEncoding to high")
                } else {
                    sub?.changePreferredEncoding("low")
                    Log.d(tag, "$tag changePreferredEncoding to low")
                }
                toggleEncodingId = !toggleEncodingId
            }
        }

        binding.btnScreenShare.setOnClickListener {
            startScreenCapture()
        }
    }

    private fun initRoomFunctions() {
        SFURoomManager.sfuRoom?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                runOnUiThread {
                    this@SFURoomDetailsActivity.members.clear()
                    membersLiveData.value = SFURoomManager.sfuRoom?.members?.toMutableList()
                }
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                runOnUiThread {
                    this@SFURoomDetailsActivity.publications.clear()
                    publicationsLivaData.value =
                        SFURoomManager.sfuRoom?.publications?.toMutableList()
                }
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished")
            }
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

        GlobalScope.launch(Dispatchers.Main) {
            publication = SFURoomManager.localPerson?.publish(screenShareVideoStream!!)
            publication?.let { setRoomPublicationEventHandler(it) }
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

    private fun setRoomPublicationEventHandler(roomPublication: RoomPublication) {
        roomPublication.apply {
            val tag = roomPublication.javaClass.simpleName

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
