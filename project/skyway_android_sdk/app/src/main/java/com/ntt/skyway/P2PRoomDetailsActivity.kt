package com.ntt.skyway

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.adapter.RecyclerViewAdapterRoomPublication
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.databinding.ActivityDetailsBinding
import com.ntt.skyway.listener.RoomPublicationAdapterListener
import com.ntt.skyway.manager.RoomManager
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.*


class P2PRoomDetailsActivity : DetailsBaseActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private var manager = App.currentManager as RoomManager
    private var currentSelectableStream: SelectableStream? = null

    private var recyclerViewAdapterMember: RecyclerViewAdapterRoomMember? = null
    private var recyclerViewAdapterPublication: RecyclerViewAdapterRoomPublication? = null
    private var membersLiveData = MutableLiveData<MutableList<RoomMember>>()
    private var publicationsLiveData = MutableLiveData<MutableList<RoomPublication>>()

    private var currentPublication: RoomPublication? = null
    private var localVideoStream: LocalVideoStream? = null
    private var localDataStream: LocalDataStream? = null

    private fun initUI() {
        // channel / local person
        binding.channelName.text = manager.room?.name
        binding.memberName.text = manager.localPerson?.name

        // selector
        binding.streamSelector.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, streamList)
        binding.streamSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentSelectableStream = (parent?.selectedItem as SelectableStream)
                    currentSelectableStream?.select()
                }
            }

        // renderer
        binding.localRenderer.setup()
        binding.remoteRenderer.setup()

        // stream / members
        recyclerViewAdapterMember = RecyclerViewAdapterRoomMember()

        membersLiveData.observe(this) {
            recyclerViewAdapterMember?.setData(it)
        }
        publicationsLiveData.observe(this) {
            if (manager.localPerson == null) {
                return@observe
            }
            recyclerViewAdapterPublication?.setData(it, manager.localPerson!!)
        }

        recyclerViewAdapterPublication = RecyclerViewAdapterRoomPublication(this, object :
            RoomPublicationAdapterListener {
            override suspend fun onUnPublishClick(publicationId: String): Boolean {
                val publication =
                    manager.localPerson?.publications?.find { it.id == publicationId }
                if (publication == null) {
                    App.showMessage("unpublish publication not found : $publicationId")
                    return false
                }
                val stream = publication.stream
                if (stream?.contentType == Stream.ContentType.VIDEO) {
                    (stream as LocalVideoStream).removeAllRenderer()
                }
                if (!manager.localPerson?.unpublish(publication)!!) {
                    App.showMessage("unpublish failed : $publicationId")
                    return false
                }
                App.showMessage("unpublish success : $publicationId")
                return true
            }

            override suspend fun onUpdateEncodingClick(publicationId: String): Boolean {
                val publication =
                    manager.localPerson?.publications?.find { it.id == publicationId }
                if (publication == null) {
                    App.showMessage("update encoding publication not found : $publicationId")
                    return false
                }

                if (publication.encodings.isEmpty()) {
                    App.showMessage("update encodings : encoding size is 0")
                    return false
                }

                if (publication.encodings[0].maxBitrate != null && publication.encodings[0].maxBitrate!! < 100_000) {
                    publication.updateEncodings(
                        mutableListOf(
                            Encoding("high", maxBitrate = 500_000, scaleResolutionDownBy = 2.0),
                            Encoding("low", maxBitrate = 100_000, scaleResolutionDownBy = 1.0)
                        )
                    )
                    App.showMessage("update encodings (rate unlimited): $publicationId")
                } else {
                    publication.updateEncodings(
                        mutableListOf(
                            Encoding("high", maxBitrate = 50_000, scaleResolutionDownBy = 2.0),
                            Encoding("low", maxBitrate = 10_000, scaleResolutionDownBy = 1.0)
                        )
                    )
                    App.showMessage("update encodings (rate limited): $publicationId")
                }
                return true
            }

            override suspend fun onChangeEncodingClick(publicationId: String): Boolean {
                val subscription =
                    manager.localPerson?.subscriptions?.find { it.publication.id == publicationId }
                if (subscription == null) {
                    App.showMessage("changePreferredEncoding subscription not found : $publicationId")
                    return false
                }

                val subscriptionEncodings = subscription.publication.encodings
                if (subscriptionEncodings.isEmpty()) {
                    App.showMessage("change preferred encoding : encoding size is 0")
                    return false
                }

                for (encoding in subscriptionEncodings) {
                    if (encoding.id != null && subscription.preferredEncodingId != encoding.id) {
                        subscription.changePreferredEncoding(encoding.id!!)
                        App.showMessage("change preferred encoding (ID:${encoding.id!!}) : $publicationId")
                        return true
                    }
                }
                App.showMessage("change preferred encoding : changeable encoding not found")
                return false
            }

            override suspend fun onSendDataClick(publicationId: String): Boolean {
                val publication =
                    manager.localPerson?.publications?.find { it.id == publicationId }
                if (publication == null) {
                    App.showMessage("send data publication not found : $publicationId")
                    return true
                }
                val message = binding.textData.text.toString()
                (publication.stream!! as LocalDataStream).write(message)
                App.showMessage("send data : $message")
                return true
            }

            override suspend fun onReplaceStreamClick(publicationId: String): Boolean {
                val stream = currentSelectableStream?.getStream()
                if (stream == null) {
                    App.showMessage("replace stream canceled : stream is null")
                    return false
                }
                if (stream !is LocalVideoStream) {
                    App.showMessage("replace stream canceled : selected stream is not LocalVideoStream")
                    return false
                }
                val publication =
                    manager.localPerson?.publications?.find { it.id == publicationId }
                if (publication == null) {
                    App.showMessage("replace stream publication not found : $publicationId")
                    return false
                }
                localVideoStream!!.removeAllRenderer()
                stream.addRenderer(binding.localRenderer)
                publication.replaceStream(stream)
                localVideoStream = stream
                App.showMessage("replace stream : $publicationId")
                return true
            }

            override suspend fun onSubscribeClick(publicationId: String): Boolean {
                val options = RoomSubscription.Options()
                val subscription = manager.localPerson?.subscribe(publicationId, options)
                if (subscription == null) {
                    App.showMessage("subscribe failed : $publicationId")
                    return false
                }

                subscription.onConnectionStateChangedHandler = {
                    App.showMessage("subscription.onConnectionStateChangedHandler: $it")
                }

                when (subscription.contentType) {
                    Stream.ContentType.VIDEO -> {
                        (subscription.stream as RemoteVideoStream).addRenderer(binding.remoteRenderer)
                        App.showMessage("subscribe success(Video) : ${subscription.id}")
                    }
                    Stream.ContentType.AUDIO -> {
                        (subscription.stream as RemoteAudioStream)
                        App.showMessage("subscribe success(Audio) : ${subscription.id}")
                    }
                    Stream.ContentType.DATA -> {
                        (subscription.stream as RemoteDataStream).onDataHandler = {
                            App.showMessage("data received: $it")
                        }
                        (subscription.stream as RemoteDataStream).onDataBufferHandler = {
                            App.showMessage(
                                "data received byte: ${it.contentToString()} string: ${String(it)}"
                            )
                        }
                        App.showMessage("subscribe success(Data) : ${subscription.id}")
                    }
                    else -> {}
                }
                return true
            }

            override suspend fun onUnSubscribeClick(publicationId: String): Boolean {
                val subscription =
                    manager.localPerson?.subscriptions?.find { it.publication.id == publicationId }
                if (subscription == null) {
                    App.showMessage("unsubscribe subscription not found : $publicationId")
                    return false
                }
                if (!manager.localPerson?.unsubscribe(subscription.id)!!) {
                    App.showMessage("unsubscribe failed : ${subscription.id}")
                    return false
                }
                App.showMessage("unsubscribe success : ${subscription.id}")
                return true
            }

            override suspend fun onEnableClick(publicationId: String): Boolean {
                val publication =
                    manager.localPerson?.publications?.find { it.id == publicationId }
                if (publication == null) {
                    App.showMessage("enable publication not found : $publicationId")
                    return false
                }
                if (!publication.enable()) {
                    App.showMessage("enable failed : $publicationId")
                    return false
                }
                App.showMessage("enable success : $publicationId")
                return true
            }

            override suspend fun onDisableClick(publicationId: String): Boolean {
                val publication =
                    manager.localPerson?.publications?.find { it.id == publicationId }
                if (publication == null) {
                    App.showMessage("disable publication not found : $publicationId")
                    return false
                }
                if (!publication.disable()) {
                    App.showMessage("disable failed : $publicationId")
                    return false
                }
                App.showMessage("disable success : $publicationId")
                return true
            }
        })

        binding.memberList.layoutManager = LinearLayoutManager(this)
        binding.memberList.adapter = recyclerViewAdapterMember
        binding.rvPublicationList.layoutManager = LinearLayoutManager(this)
        binding.rvPublicationList.adapter = recyclerViewAdapterPublication

        manager.room?.onMemberListChangedHandler = {
            runOnUiThread {
                runBlocking { delay(200) }
                membersLiveData.value = manager.room?.members?.toMutableList()
            }
        }

        manager.room?.onPublicationListChangedHandler = {
            runOnUiThread {
                runBlocking { delay(200) }
                publicationsLiveData.value = manager.room?.publications?.toMutableList()
            }
        }

        membersLiveData.value = manager.room?.members?.toMutableList()
        publicationsLiveData.value = manager.room?.publications?.toMutableList()
    }

    @DelicateCoroutinesApi
    fun initButton() {
        binding.btnLeave.setOnClickListener {
            GlobalScope.launch {
                if (!manager.localPerson?.leave()!!) {
                    App.showMessage("leave failed : ${manager.localPerson?.id}")
                    return@launch
                }
                App.showMessage("leave success")
                finish()
            }
        }
        binding.btnPublish.setOnClickListener {
            val stream = currentSelectableStream?.getStream()
            if (stream == null) {
                App.showMessage("publish canceled : stream is null")
                return@setOnClickListener
            }
            GlobalScope.launch {
                var options: RoomPublication.Options? = null
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    if (localVideoStream != null) {
                        localVideoStream!!.removeAllRenderer()
                    }
                    localVideoStream = (stream as LocalVideoStream)
                    localVideoStream!!.addRenderer(binding.localRenderer)
                    options = RoomPublication.Options(
                        encodings = mutableListOf(
                            Encoding("high", maxBitrate = 500_000, scaleResolutionDownBy = 2.0),
                            Encoding("low", maxBitrate = 100_000, scaleResolutionDownBy = 1.0)
                        )
                    )
                } else if (stream.contentType == Stream.ContentType.DATA) {
                    localDataStream = (stream as LocalDataStream)
                }

                val publication = manager.localPerson?.publish(stream, options)
                if (publication == null) {
                    App.showMessage("publish failed : ${stream.contentType}")
                    return@launch
                }

                publication.onConnectionStateChangedHandler = {
                    App.showMessage("publication.onConnectionStateChangedHandler: $it")
                }

                currentPublication = publication
                App.showMessage("publish success : ${stream.contentType}")
            }
        }
        binding.btnCreateSFUBot.visibility = View.GONE
        binding.btnCreateSFUBot.setOnClickListener {}
    }

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()
        initButton()
    }
}
