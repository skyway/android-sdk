package com.ntt.skyway.channel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.ScreenShareService
import com.ntt.skyway.adapter.RecyclerViewAdapterChannelPublication
import com.ntt.skyway.adapter.RecyclerViewAdapterMember
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.databinding.ActivityChanelDetailsBinding
import com.ntt.skyway.listener.ChannelPublicationAdapterListener
import com.ntt.skyway.manager.ChannelManager
import com.ntt.skyway.plugin.sfuBot.SFUBot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChannelDetailsActivity : AppCompatActivity() {
    companion object {
        const val CAPTURE_PERMISSION_REQUEST_CODE = 1002
    }

    private val tag: String = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityChanelDetailsBinding

    private var mService: ScreenShareService? = null

    // Boolean to check if our activity is bound to service or not
    private var mIsBound: Boolean = false

    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private var recyclerViewAdapterMember: RecyclerViewAdapterMember? = null
    private var recyclerViewAdapterChannelPublication: RecyclerViewAdapterChannelPublication? = null

    private var publicationsLivaData = MutableLiveData<MutableList<Publication>>()
    private var publications = arrayListOf<Publication>()

    private var publication: Publication? = null
    private var subscription: Subscription? = null

    private var isSubEncodingHigh: Boolean = true

    private val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val paint = Paint()
    private val back = Paint()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityChanelDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()
        initSurfaceViews()

        paint.color = android.graphics.Color.CYAN
        back.color = android.graphics.Color.BLACK
    }

    private fun initUI() {
        binding.channelName.text = ChannelManager.channel?.name
        binding.memberName.text = ChannelManager.localPerson?.name

        recyclerViewAdapterMember = RecyclerViewAdapterMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterMember

        ChannelManager.membersLiveData.observe(this) {
            recyclerViewAdapterMember?.setData(it)
        }

        ChannelManager.localPerson?.onPublicationListChangedHandler = {
            Log.d(tag, "localPerson onPublicationListChangedHandler")
        }

        ChannelManager.localPerson?.onSubscriptionListChangedHandler = {
            Log.d(tag, "localPerson onSubscriptionListChangedHandler")
        }

        recyclerViewAdapterChannelPublication = RecyclerViewAdapterChannelPublication(object :
            ChannelPublicationAdapterListener {
            override fun onSubscribeClick(publicationId: String) {
                scope.launch(Dispatchers.Main) {
                    subscription = ChannelManager.localPerson?.subscribe(publicationId)
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
                        else -> {}
                    }
                }
            }

            override fun onUnSubscribeClick() {
                scope.launch {
                    ChannelManager.localPerson?.unsubscribe(subscription?.id!!)
                }
            }

            override fun onStartForwardingClick(publicationId: String) {
                scope.launch(Dispatchers.Main) {
                    val publication =
                        ChannelManager.channel?.publications?.find { it.id == publicationId }
                    ChannelManager.sfuBot?.startForwarding(publication!!)
                }
            }

            override fun onEnableClick(publicationId: String) {
                scope.launch(Dispatchers.Main) {
                    val publication =
                        ChannelManager.channel?.publications?.find { it.id == publicationId }
                    CameraSource.startCapturing(
                        applicationContext,
                        CameraSource.getBackCameras(applicationContext).first(),
                        CameraSource.CapturingOptions(800, 800)
                    )
                    AudioSource.start()
                    publication?.enable()
                }
            }

            override fun onDisableClick(publicationId: String) {
                scope.launch(Dispatchers.Main) {
                    val publication =
                        ChannelManager.channel?.publications?.find { it.id == publicationId }
                    publication?.disable()
                    CameraSource.stopCapturing()
                    AudioSource.stop()
                }
            }
        })
        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterChannelPublication

        publicationsLivaData.observe(this) {
            recyclerViewAdapterChannelPublication?.setData(it)
        }

        publicationsLivaData.value = ChannelManager.channel?.publications?.toMutableList()

        initButtons()
        initChannelFunctions()
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
//        localVideoStream?.addRenderer(binding.localRenderer)
    }

    private fun initButtons() {
        binding.apply {
            btnLeaveRoom.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    ChannelManager.channel!!.leave(ChannelManager.localPerson!!)
                    onBackPressed()
                }
            }

            btnPublish.setOnClickListener {
                publishCameraVideoStream()
            }

            btnUnPublish.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    ChannelManager.localPerson?.unpublish(publication?.id!!)
                }
            }

            btnScreenShare.setOnClickListener {
                startScreenCapture()
            }

            btnAudio.setOnClickListener {
                publishAudioStream()
            }

            btnPublishData.setOnClickListener {
                publishDataStream()
            }

            btnCustomVideo.setOnClickListener {
                publishCustomVideoStream()
            }

            btnToggleSubEncoding.setOnClickListener {
                if (isSubEncodingHigh) {
                    subscription?.changePreferredEncoding("low")
                } else {
                    subscription?.changePreferredEncoding("high")
                }
                isSubEncodingHigh = !isSubEncodingHigh
            }

            btnReplaceStream.setOnClickListener {
                replaceStream()
            }

            btnSendData.setOnClickListener {
                val text = binding.textData.text.toString()
                localDataStream?.write(text)
//                localDataStream?.write(text.toByteArray()) // to send ByteArray()
            }

            btnSFUBotCreate.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    ChannelManager.sfuBot = SFUBot.createBot(ChannelManager.channel!!)
                }
            }
        }
    }

    private fun initChannelFunctions() {
        ChannelManager.channel?.apply {
            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                runOnUiThread {
                    onPublicationChanged()
                }
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished")
            }

            onPublicationSubscribedHandler = {
                runOnUiThread {
                    onPublicationSubscribed(it)
                }
            }
        }
    }

    private fun onPublicationChanged() {
        publications.clear()
        publicationsLivaData.value = ChannelManager.channel?.publications?.toMutableList()
    }

    private fun onPublicationSubscribed(subscription: Subscription) {
        if (subscription.subscriber.side == Member.Side.LOCAL) return
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


        val options = Publication.Options()
        scope.launch(Dispatchers.Main) {
            publication = ChannelManager.localPerson?.publish(screenShareVideoStream!!, options)
            publication?.apply { }
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

    private fun publishCameraVideoStream() {
        Log.d(tag, "publishCameraVideoStream()")
//        val encoding1 = Encoding("high", 200_000, 4.0)
//        val encoding2 = Encoding("low", 2_000, 6.0)
        // val encoding1 = Encoding("high", maxFramerate = 1.0)
        // val codec = Codec(Codec.MimeType.AV1)
        val options = Publication.Options(
            metadata = "",
            // codecCapabilities = mutableListOf(codec),
            // encodings = mutableListOf(encoding1)
//            isEnabled = false
        )
        scope.launch(Dispatchers.Main) {
            publication = ChannelManager.localPerson?.publish(localVideoStream!!, options)
            Log.d(tag, "publication state: ${publication?.state}")

            publication?.stream?.let {
                (it as LocalVideoStream).addRenderer(binding.localRenderer)
            }

            publication?.onMetadataUpdatedHandler = {
                Log.d(tag, "onMetadataUpdatedHandler $it")
            }

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
        // val codec = Codec(Codec.MimeType.RED)
        val options = Publication.Options(
            // codecCapabilities = mutableListOf(codec)
        )
        scope.launch(Dispatchers.Main) {
            publication = ChannelManager.localPerson?.publish(localAudioStream, options)
        }
    }

    private fun publishDataStream() {
        val localDataSource = DataSource()
        localDataStream = localDataSource.createStream()
        val options = Publication.Options()
        scope.launch(Dispatchers.Main) {
            publication = ChannelManager.localPerson?.publish(localDataStream!!, options)
        }
    }

    private fun publishCustomVideoStream() {
        val localVideoStream = createCustomVideoStream()
//        localVideoStream.addRenderer(binding.localRenderer)
        scope.launch {
            publication = ChannelManager.localPerson?.publish(localVideoStream, Publication.Options(isEnabled = true))

            publication?.stream?.let {
                (it as LocalVideoStream).addRenderer(binding.localRenderer)
            }
        }
    }

    fun updateBitmap() {
        canvas.drawRect(0F, 0F, 800F, 800F, back)
        canvas.rotate(10F, 400F, 400F)
        canvas.drawRect(200F, 200F, 600F, 600F, paint)
    }

    private fun replaceStream() {
        val localVideoStream = createCustomVideoStream()
        publication?.replaceStream(localVideoStream)
    }

    private fun createCustomVideoStream(): LocalVideoStream {
        val localVideoSource = CustomVideoFrameSource(800, 800)
        val handler = Handler()
        val r: Runnable = object : Runnable {
            override fun run() {
                updateBitmap()
                scope.launch {
                    localVideoSource.updateFrame(bitmap, 0)
                }
                handler.postDelayed(this, 16)
            }
        }
        handler.post(r)
        return localVideoSource.createStream()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d(tag, "ServiceConnection: connected to service.")
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

    private fun unbindService() {
        Intent(this, ScreenShareService::class.java).also {
            unbindService(serviceConnection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mIsBound) unbindService()
        stopScreenShareService()
        localVideoStream?.dispose()
        binding.localRenderer.dispose()
        binding.remoteRenderer.dispose()
    }
}
