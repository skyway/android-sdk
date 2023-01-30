package com.ntt.skyway.examples.autosubscribe

import android.os.Bundle
import android.util.Log
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
import com.ntt.skyway.examples.autosubscribe.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.examples.autosubscribe.adapter.RecyclerViewAdapterRoomPublication
import com.ntt.skyway.examples.autosubscribe.adapter.RecyclerViewAdapterSubscription
import com.ntt.skyway.examples.autosubscribe.databinding.ActivityP2pRoomDetailsBinding
import com.ntt.skyway.examples.autosubscribe.listener.RoomPublicationAdapterListener
import com.ntt.skyway.examples.autosubscribe.manager.P2PRoomManager
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class P2PRoomDetailsActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityP2pRoomDetailsBinding

    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterRoomPublication: RecyclerViewAdapterRoomPublication? = null
    private var recyclerViewAdapterSubscription: RecyclerViewAdapterSubscription? = null

    private var publication: RoomPublication? = null
    private var subscription: RoomSubscription? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityP2pRoomDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)
        supportActionBar?.title = "P2P"

        initUI()

        scope.launch(Dispatchers.Main) {
            initSurfaceViews()
        }
    }

    private fun initUI() {
        binding.memberName.text = P2PRoomManager.localPerson?.name

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

        recyclerViewAdapterSubscription = RecyclerViewAdapterSubscription()
        binding.rvSubscriptionList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSubscriptionList.adapter = recyclerViewAdapterSubscription

        recyclerViewAdapterRoomPublication =
            RecyclerViewAdapterRoomPublication(roomPublicationAdapterListener)

        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterRoomPublication

        P2PRoomManager.room?.members?.toMutableList()
            ?.let { recyclerViewAdapterRoomMember?.setData(it) }
        P2PRoomManager.room?.publications?.toMutableList()
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
        P2PRoomManager.room?.publications?.forEach {
            subscribeToPublication(it)
        }
    }



    private fun initButtons() {
        binding.btnLeaveRoom.setOnClickListener {
            scope.launch(Dispatchers.Main) {
                P2PRoomManager.room!!.leave(P2PRoomManager.localPerson!!)
                finish()
            }
        }

    }

    private fun initRoomFunctions() {
        P2PRoomManager.room?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                runOnUiThread {
                    P2PRoomManager.room?.members?.toMutableList()
                        ?.let { recyclerViewAdapterRoomMember?.setData(it) }
                }
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
                runOnUiThread {
                    P2PRoomManager.room?.publications?.toMutableList()
                        ?.let { recyclerViewAdapterRoomPublication?.setData(it) }
                }
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished: ${it.id}")
                subscribeToPublication(it)
            }

            onSubscriptionListChangedHandler = {
                var videoSubs =  P2PRoomManager.localPerson?.subscriptions?.filter { it.contentType ==  Stream.ContentType.VIDEO}
                Log.d(tag, "Room onSubscriptionListChangedHandler sub size: ${P2PRoomManager.localPerson?.subscriptions?.size} videoSubs size: ${videoSubs?.size}")
                runOnUiThread {
                    recyclerViewAdapterSubscription?.setData(videoSubs as MutableList<RoomSubscription>)
                }

            }

        }

    }

    private fun subscribeToPublication(publication: RoomPublication) {
        if(publication.publisher?.id == P2PRoomManager.localPerson?.id) return

        scope.launch(Dispatchers.Main) {
            subscription = P2PRoomManager.localPerson?.subscribe(publication.id)
            when (subscription?.contentType) {
                Stream.ContentType.VIDEO -> {

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

    private fun publishCameraVideoStream() {
        Log.d(tag, "publishCameraVideoStream()")
        scope.launch(Dispatchers.Main) {
            publication = localVideoStream?.let { P2PRoomManager.localPerson?.publish(it) }
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
            publication = P2PRoomManager.localPerson?.publish(localAudioStream, options)
        }
    }

    private fun publishDataStream() {
        val localDataSource = DataSource()
        localDataStream = localDataSource.createStream()
        val options = RoomPublication.Options()
        scope.launch(Dispatchers.Main) {
            publication = P2PRoomManager.localPerson?.publish(localDataStream!!, options)
        }
    }
    

    private var roomPublicationAdapterListener: RoomPublicationAdapterListener = object: RoomPublicationAdapterListener{
        override fun onUnPublishClick(publication: RoomPublication) {
            scope.launch(Dispatchers.Default) {
                P2PRoomManager.localPerson?.unpublish(publication)
            }
        }

        override fun onUnSubscribeClick() {
            scope.launch(Dispatchers.Main) {
                P2PRoomManager.localPerson?.unsubscribe(subscription?.id!!)
            }
        }

        override fun onSFUChangeEncodingClick(subscription: RoomSubscription) {
            //only for SFU
        }

    }

}
