package com.ntt.skyway.examples.sfuroom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ntt.skyway.examples.common.RoomDetailsActivity
import com.ntt.skyway.examples.common.manager.RoomManager
import com.ntt.skyway.examples.common.manager.SampleManager
import com.ntt.skyway.examples.databinding.ActivityRoomCommonBinding
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class SFURoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoomCommonBinding

    private val scope = CoroutineScope(Dispatchers.IO)
    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityRoomCommonBinding.inflate(layoutInflater)
        setContentView(this.binding.root)
        initUI()
    }

    private fun initUI() {
        binding.apply {
            supportActionBar?.title = SampleManager.type?.displayName

            btnJoinChannel.setOnClickListener {
                val memberInit: RoomMember.Init
                if (binding.memberName.text.toString().isEmpty()) {
                    memberInit = RoomMember.Init(UUID.randomUUID().toString())
                } else {
                    memberInit = RoomMember.Init(binding.memberName.text.toString())
                }


                scope.launch(Dispatchers.Main) {
                    RoomManager.room = SFURoom.findOrCreate(roomName.text.toString())
                    if (RoomManager.room == null) {
                        Toast.makeText(
                            this@SFURoomActivity,
                            "Room findOrCreate failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    Log.d(tag, "findOrCreate Room id: " + RoomManager.room?.id)
                    Toast.makeText(this@SFURoomActivity, "Room findOrCreate OK", Toast.LENGTH_SHORT)
                        .show()

                    RoomManager.localPerson = RoomManager.room!!.join(memberInit)
                    if (RoomManager.localPerson != null) {
                        Log.d(tag, "localPerson: " + RoomManager.localPerson?.id)
                        RoomManager.type = "SFU"
                        startActivity(Intent(this@SFURoomActivity, RoomDetailsActivity::class.java))
                    } else {
                        Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        }
    }

}