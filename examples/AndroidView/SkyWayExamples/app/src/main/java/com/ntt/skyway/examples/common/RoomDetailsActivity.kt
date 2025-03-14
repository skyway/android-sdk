package com.ntt.skyway.examples.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.examples.common.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.examples.common.adapter.RecyclerViewAdapterRoomPublication
import com.ntt.skyway.examples.common.listener.RoomPublicationAdapterListener
import com.ntt.skyway.examples.common.manager.RoomManager
import com.ntt.skyway.examples.common.manager.SampleManager
import com.ntt.skyway.examples.databinding.ActivityRoomDetailsCommonBinding
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RoomDetailsActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityRoomDetailsCommonBinding

    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterRoomPublication: RecyclerViewAdapterRoomPublication? = null

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null

    //only use by SFURoom sample
    private var sfuToggleEncodingId = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityRoomDetailsCommonBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()

        scope.launch(Dispatchers.Main) {
            initSurfaceViews()
        }
    }

    private fun initUI() {
        binding.memberName.text = RoomManager.localPerson?.name

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

        RoomManager.localPerson?.onPublicationListChangedHandler = {
            Log.d(tag, "localPerson onPublicationListChangedHandler")
        }

        RoomManager.localPerson?.onSubscriptionListChangedHandler = {
            Log.d(tag, "localPerson onSubscriptionListChangedHandler")
        }

        recyclerViewAdapterRoomPublication =
            RecyclerViewAdapterRoomPublication(roomPublicationAdapterListener)

        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterRoomPublication


        RoomManager.room?.members?.toMutableList()
            ?.let { recyclerViewAdapterRoomMember?.setData(it) }
        RoomManager.room?.publications?.toMutableList()
            ?.let { recyclerViewAdapterRoomPublication?.setData(it) }

        initButtons()
        initRoomFunctions()
        supportActionBar?.title = SampleManager.type?.displayName
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
                finish()
            }
        }

        binding.btnPublish.setOnClickListener {
            publishCameraVideoStream()
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
        }

        //only use by SFURoom sample
        binding.btnChangeEncoding.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                val sub = RoomManager.room?.subscriptions?.find { it.id == subscription?.id }

                if (sfuToggleEncodingId) {
                    sub?.changePreferredEncoding("high")
                    Log.d(tag, "$tag changePreferredEncoding to high")
                } else {
                    sub?.changePreferredEncoding("low")
                    Log.d(tag, "$tag changePreferredEncoding to low")
                }
                sfuToggleEncodingId = !sfuToggleEncodingId
            }
        }

        if (RoomManager.type == "P2P") {
            binding.btnChangeEncoding.visibility = View.GONE
        }
        if (RoomManager.type == "SFU") {
            binding.textData.visibility = View.GONE
            binding.btnSendData.visibility = View.GONE
            binding.btnPublishData.visibility = View.GONE
        }
    }

    private fun initRoomFunctions() {
        RoomManager.room?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                runOnUiThread {
                    RoomManager.room?.members?.toMutableList()
                        ?.let { recyclerViewAdapterRoomMember?.setData(it) }
                }
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                runOnUiThread {
                    RoomManager.room?.publications?.toMutableList()
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
            publication = localVideoStream?.let { RoomManager.localPerson?.publish(it) }
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


    private var roomPublicationAdapterListener: RoomPublicationAdapterListener = object: RoomPublicationAdapterListener{
        override fun onUnPublishClick(publication: RoomPublication) {
            scope.launch(Dispatchers.Default) {
                RoomManager.localPerson?.unpublish(publication)
            }
        }

        // used by p2proom & sfuroom sample
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

        override fun onSFUChangeEncodingClick(subscription: RoomSubscription) {
            scope.launch(Dispatchers.Main) {
                if (sfuToggleEncodingId) {
                    subscription?.changePreferredEncoding("high")
                    Log.d(tag, "$tag changePreferredEncoding to high")
                } else {
                    subscription?.changePreferredEncoding("low")
                    Log.d(tag, "$tag changePreferredEncoding to low")
                }
            }
            sfuToggleEncodingId = !sfuToggleEncodingId
        }

    }

}