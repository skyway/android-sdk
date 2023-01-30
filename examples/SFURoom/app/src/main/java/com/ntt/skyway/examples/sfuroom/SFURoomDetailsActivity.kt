package com.ntt.skyway.examples.sfuroom

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.core.content.Codec
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.examples.sfuroom.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.examples.sfuroom.adapter.RecyclerViewAdapterRoomPublication
import com.ntt.skyway.examples.sfuroom.databinding.ActivitySfuRoomDetailsBinding
import com.ntt.skyway.examples.sfuroom.listener.RoomPublicationAdapterListener
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomDetailsActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivitySfuRoomDetailsBinding

    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterRoomPublication: RecyclerViewAdapterRoomPublication? = null

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null

    private var toggleEncodingId = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySfuRoomDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()

        scope.launch(Dispatchers.Main) {
            initSurfaceViews()
        }
    }

    private fun initUI() {
        binding.memberName.text = SFURoomManager.localPerson?.name

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

        SFURoomManager.localPerson?.onPublicationListChangedHandler = {
            Log.d(tag, "localPerson onPublicationListChangedHandler")
        }

        SFURoomManager.localPerson?.onSubscriptionListChangedHandler = {
            Log.d(tag, "localPerson onSubscriptionListChangedHandler")
        }

        recyclerViewAdapterRoomPublication =
            RecyclerViewAdapterRoomPublication(roomPublicationAdapterListener)

        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterRoomPublication


        SFURoomManager.sfuRoom?.members?.toMutableList()
            ?.let { recyclerViewAdapterRoomMember?.setData(it) }
        SFURoomManager.sfuRoom?.publications?.toMutableList()
            ?.let { recyclerViewAdapterRoomPublication?.setData(it) }

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
                finish()
            }
        }

        binding.btnPublish.setOnClickListener {
            publishCameraVideoStream()
        }

        binding.btnAudio.setOnClickListener {
            publishAudioStream()
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

    }

    private fun initRoomFunctions() {
        SFURoomManager.sfuRoom?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                runOnUiThread {
                    SFURoomManager.sfuRoom?.members?.toMutableList()
                        ?.let { recyclerViewAdapterRoomMember?.setData(it) }
                }
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                runOnUiThread {
                    SFURoomManager.sfuRoom?.publications?.toMutableList()
                        ?.let { recyclerViewAdapterRoomPublication?.setData(it) }
                }
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished: ${it.id}")
            }
        }
    }

    private fun publishCameraVideoStream() {
        Log.d(tag, "publishCameraVideoStream()")
        scope.launch(Dispatchers.Main) {
            val encodingLow = Encoding("low", 10_000, 10.0)
            val encodingHigh = Encoding("high", 200_000, 1.0)
            val options = RoomPublication.Options(encodings =  mutableListOf(encodingLow, encodingHigh))
            publication = localVideoStream?.let { SFURoomManager.localPerson?.publish(it, options) }
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
        scope.launch(Dispatchers.Main) {
            publication = SFURoomManager.localPerson?.publish(localAudioStream, options)
        }
    }

    private fun publishDataStream() {
        val localDataSource = DataSource()
        localDataStream = localDataSource.createStream()
        val options = RoomPublication.Options()
        scope.launch(Dispatchers.Main) {
            publication = SFURoomManager.localPerson?.publish(localDataStream!!, options)
        }
    }
    

    private var roomPublicationAdapterListener: RoomPublicationAdapterListener = object: RoomPublicationAdapterListener{
        override fun onUnPublishClick(publication: RoomPublication) {
            scope.launch(Dispatchers.Default) {
                SFURoomManager.localPerson?.unpublish(publication)
            }
        }

        override fun onSubscribeClick(publicationId: String) {
            scope.launch(Dispatchers.Main) {
                subscription = SFURoomManager.localPerson?.subscribe(publicationId, RoomSubscription.Options(preferredEncodingId = "low"))
                when (subscription?.contentType) {
                    Stream.ContentType.VIDEO -> {
                        (subscription?.stream as RemoteVideoStream).addRenderer(binding.remoteRenderer)
                    }
                    Stream.ContentType.AUDIO -> {
                        (subscription?.stream as RemoteAudioStream)
                    }
                    null -> {}
                    else -> {}
                }
            }
        }

        override fun onUnSubscribeClick() {
            scope.launch(Dispatchers.Main) {
                SFURoomManager.localPerson?.unsubscribe(subscription?.id!!)
            }
        }

    }

}
