package com.ntt.skyway.room

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.adapter.RecyclerViewAdapterRoomMember
import com.ntt.skyway.databinding.ActivityRoomBinding
import com.ntt.skyway.manager.RoomManager
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class RoomActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private val roomName = UUID.randomUUID().toString()
    private val memberName = UUID.randomUUID().toString()

    private lateinit var binding: ActivityRoomBinding

    private var recyclerViewAdapterRoomMember: RecyclerViewAdapterRoomMember? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        checkPermission()

        binding.roomName.setText(roomName)
        binding.memberName.setText(memberName)

        binding.btnJoinRoom.setOnClickListener {
            Log.d(tag, "btnJoinRoom.setOnClickListener: ")
            val memberInit = RoomMember.Init(binding.memberName.text.toString())
            scope.launch(Dispatchers.Main) {
                RoomManager.room = P2PRoom.findOrCreate(binding.roomName.text.toString())
                initRoomFunctions()
                RoomManager.localPerson = RoomManager.room!!.join(memberInit)
                RoomManager.localPerson?.apply {
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
                if (RoomManager.localPerson != null) {
                    Log.d(tag, "localPerson: " + RoomManager.localPerson?.id)
                    startActivity(Intent(this@RoomActivity, RoomDetailsActivity::class.java))
                } else {
                    Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.btnDisposeRoom.setOnClickListener {
            RoomManager.room?.dispose()
        }

        binding.btnSfuRoom.setOnClickListener {
            startActivity(Intent(this@RoomActivity, SFURoomActivity::class.java))
        }

        recyclerViewAdapterRoomMember = RecyclerViewAdapterRoomMember()
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = recyclerViewAdapterRoomMember

            RoomManager.membersLiveData.observe(this) {
            recyclerViewAdapterRoomMember?.setData(it)
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }

    private fun initRoomFunctions() {
        RoomManager.updateMembersList()

        RoomManager.room?.apply {
            onMemberListChangedHandler = {
                Log.d(tag, "$tag onMemberListChanged")
                runOnUiThread {
                    RoomManager.updateMembersList()
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

    override fun onResume() {
        super.onResume()
        initRoomFunctions()
    }

}
