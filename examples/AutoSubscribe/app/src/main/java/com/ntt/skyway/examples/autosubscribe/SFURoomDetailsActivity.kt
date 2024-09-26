package com.ntt.skyway.examples.autosubscribe

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.examples.autosubscribe.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.examples.autosubscribe.adapter.RecyclerViewAdapterRoomPublication
import com.ntt.skyway.examples.autosubscribe.adapter.RecyclerViewAdapterSubscription
import com.ntt.skyway.examples.autosubscribe.databinding.ActivitySfuRoomDetailsBinding
import com.ntt.skyway.examples.autosubscribe.listener.RoomPublicationAdapterListener
import com.ntt.skyway.examples.autosubscribe.manager.SFURoomManager
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SFURoomDetailsActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivitySfuRoomDetailsBinding

    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterRoomPublication: RecyclerViewAdapterRoomPublication? = null
    private var recyclerViewAdapterSubscription: RecyclerViewAdapterSubscription? = null

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null

    private var toggleEncodingId = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySfuRoomDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)
        supportActionBar?.title = "SFU"

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

        recyclerViewAdapterSubscription = RecyclerViewAdapterSubscription()
        binding.rvSubscriptionList.layoutManager = GridLayoutManager(this, 5, GridLayoutManager.HORIZONTAL, false)
        binding.rvSubscriptionList.adapter = recyclerViewAdapterSubscription

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

        val device = CameraSource.getFrontCameras(applicationContext).first()
        CameraSource.startCapturing(
            applicationContext,
            device,
            CameraSource.CapturingOptions(800, 800)
        )
        localVideoStream = CameraSource.createStream()
        localVideoStream?.addRenderer(binding.localRenderer)

        subscribeToCurrentPublication()
        publishAudioVideo()
    }

    private fun publishAudioVideo() {
        publishAudioStream()
        publishCameraVideoStream()
    }

    private fun subscribeToCurrentPublication() {
        scope.launch {
            SFURoomManager.sfuRoom?.publications?.forEach {
                subscribeToPublication(it)
            }
        }

    }

    private fun initButtons() {
        binding.btnLeaveRoom.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                SFURoomManager.sfuRoom!!.leave(SFURoomManager.localPerson!!)
                finish()
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
                scope.launch {
                    subscribeToPublication(it)
                }

            }

            onSubscriptionListChangedHandler = {
                var videoSubs =  SFURoomManager.localPerson?.subscriptions?.filter { it.contentType ==  Stream.ContentType.VIDEO}
                Log.d(tag, "Room onSubscriptionListChangedHandler sub size: ${SFURoomManager.localPerson?.subscriptions?.size} videoSubs size: ${videoSubs?.size}")
                runOnUiThread {
                    recyclerViewAdapterSubscription?.setData(videoSubs as MutableList<RoomSubscription>)
                }

            }

        }
    }

    private suspend fun subscribeToPublication(publication: RoomPublication) {
        if(publication.publisher?.id == SFURoomManager.localPerson?.id) return
        subscription = SFURoomManager.localPerson?.subscribe(publication.id, RoomSubscription.Options(preferredEncodingId = "low"))
        when (subscription?.contentType) {
            Stream.ContentType.VIDEO -> {

            }
            Stream.ContentType.AUDIO -> {
                (subscription?.stream as RemoteAudioStream)
            }
            null -> {}
            else -> {}
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
    

    private var roomPublicationAdapterListener: RoomPublicationAdapterListener = object: RoomPublicationAdapterListener{
        override fun onUnPublishClick(publication: RoomPublication) {
            scope.launch(Dispatchers.Default) {
                SFURoomManager.localPerson?.unpublish(publication)
            }
        }

        override fun onUnSubscribeClick() {
            scope.launch(Dispatchers.Main) {
                SFURoomManager.localPerson?.unsubscribe(subscription?.id!!)
            }
        }

        override fun onSFUChangeEncodingClick(subscription: RoomSubscription) {
            //only for SFU
            scope.launch(Dispatchers.Main) {
                if (toggleEncodingId) {
                    subscription?.changePreferredEncoding("high")
                    Log.d(tag, "$tag changePreferredEncoding to high")
                } else {
                    subscription?.changePreferredEncoding("low")
                    Log.d(tag, "$tag changePreferredEncoding to low")
                }
            }
            toggleEncodingId = !toggleEncodingId

        }

    }

}
