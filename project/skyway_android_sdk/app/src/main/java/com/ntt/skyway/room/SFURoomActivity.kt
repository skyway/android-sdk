package com.ntt.skyway.room

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.databinding.ActivitySfuroomBinding
import com.ntt.skyway.manager.SFURoomManager
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SFURoomActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivitySfuroomBinding

    private val roomName = "sfu_room_" + (Math.random() * 100).toInt().toString()
    private val memberName = "member_" + (Math.random() * 100).toInt().toString()

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySfuroomBinding.inflate(layoutInflater)
        setContentView(this.binding.root)


        binding.roomName.text = roomName
        binding.memberName.setText(memberName)

        scope.launch(Dispatchers.Main) {
            SFURoomManager.sfuRoom = SFURoom.findOrCreate(roomName)
            SFURoomManager.sfuRoom?.let {
                setRoomEventHandler(it)
            }
            initRoomFunctions()
        }

        binding.btnJoinRoom.setOnClickListener {
            val memberInit = RoomMember.Init(binding.memberName.text.toString())
            scope.launch(Dispatchers.Main) {
                SFURoomManager.localPerson = SFURoomManager.sfuRoom?.join(memberInit)
                SFURoomManager.localPerson?.let {
                    setLocalRoomPersonEventHandler(it)
                }
                if (SFURoomManager.localPerson != null) {
                    startActivity(Intent(this@SFURoomActivity, SFURoomDetailsActivity::class.java))
                } else {
                    Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.btnDisposeRoom.setOnClickListener {
            SFURoomManager.sfuRoom?.dispose()
        }

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

        SFURoomManager.membersLiveData.observe(this) {
            recyclerViewAdapterRoomMember?.setData(it)
        }
    }

    private fun initRoomFunctions() {
        SFURoomManager.updateMembersList()

        SFURoomManager.sfuRoom?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMembershipListChanged")
                runOnUiThread {
                    SFURoomManager.updateMembersList()
                }
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "$tag onPublicationListChanged")
            }

            onStreamPublishedHandler = {
                Log.d(tag, "$tag onStreamPublished")
            }
        }
    }

    private fun setRoomEventHandler(sfuRoom: SFURoom) {
        val tag = sfuRoom.javaClass.simpleName
        sfuRoom.apply {
            onClosedHandler = {
                Log.d(tag, "onMetadataUpdated")
            }

            onMetadataUpdatedHandler = {
                Log.d(tag, "onMetadataUpdated")
            }

            onMemberListChangedHandler = {
                Log.d(tag, "onMemberListChanged")
            }

            onMemberJoinedHandler = {
                Log.d(tag, "onMemberJoined")
            }

            onMemberLeftHandler = {
                Log.d(tag, "onMemberLeft")
            }

            onMemberMetadataUpdatedHandler = { _, _ ->
                Log.d(tag, "onMemberMetadataUpdated")
            }

            onPublicationMetadataUpdatedHandler = { _, _ ->
                Log.d(tag, "onPublicationMetadataUpdated")
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "onPublicationListChanged")
            }

            onStreamPublishedHandler = {
                Log.d(tag, "onStreamPublished")
            }

            onStreamUnpublishedHandler = {
                Log.d(tag, "onStreamUnpublished")
            }

            onSubscriptionListChangedHandler = {
                Log.d(tag, "onSubscriptionListChanged")
            }

            onPublicationSubscribedHandler = {
                Log.d(tag, "onPublicationSubscribed")
            }

            onPublicationUnsubscribedHandler = {
                Log.d(tag, "onPublicationUnsubscribed")
            }
        }
    }

    private fun setLocalRoomPersonEventHandler(localRoomPerson: LocalRoomMember) {
        val tag = localRoomPerson.javaClass.simpleName
        localRoomPerson.apply {
            onStreamPublishedHandler = {
                Log.d(tag, "onStreamPublished")
            }

            onStreamUnpublishedHandler = {
                Log.d(tag, "onStreamUnpublished")
            }

            onPublicationListChangedHandler = {
                Log.d(tag, "onPublicationChanged")
            }

            onPublicationSubscribedHandler = {
                Log.d(tag, "onPublicationSubscribed")
            }

            onPublicationUnsubscribedHandler = {
                Log.d(tag, "onPublicationUnsubscribed")
            }

            onSubscriptionListChangedHandler = {
                Log.d(tag, "onSubscriptionChanged")
            }

            onLeftHandler = {
                Log.d(tag, "onLeft")
            }

            onMetadataUpdatedHandler = {
                Log.d(tag, "onMetadataUpdated")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SFURoomManager.sfuRoom?.dispose()
    }

    override fun onResume() {
        super.onResume()
        initRoomFunctions()
    }
}
