package com.ntt.skyway.examples.autosubscribe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ntt.skyway.examples.common.manager.RoomManager
import com.ntt.skyway.examples.common.manager.SampleManager
import com.ntt.skyway.examples.databinding.ActivityAutoSubscribeBinding
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AutoSubscribeActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName

    private lateinit var binding: ActivityAutoSubscribeBinding

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityAutoSubscribeBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        initUI()
    }

    private fun initUI() {
        binding.apply {
            supportActionBar?.title = SampleManager.type?.displayName

            val memberInit: RoomMember.Init
            if(binding.memberName.text.toString().isEmpty()){
                memberInit = RoomMember.Init(UUID.randomUUID().toString())
            } else {
                memberInit = RoomMember.Init(binding.memberName.text.toString())
            }


            btnJoinP2PRoom.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    RoomManager.room = P2PRoom.find(id = roomName.text.toString())
                    if(RoomManager.room == null) {
                        RoomManager.room = P2PRoom.findOrCreate(roomName.text.toString())
                        if(RoomManager.room == null) {
                            Toast.makeText(this@AutoSubscribeActivity, "Room find or findOrCreate failed", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }
                    Log.d(tag, "Room id: " + RoomManager.room?.id)
                    Toast.makeText(this@AutoSubscribeActivity,"Room OK", Toast.LENGTH_SHORT).show()

                    RoomManager.localPerson = RoomManager.room!!.join(memberInit)
                    if (RoomManager.localPerson != null) {
                        Log.d(tag, "localPerson: " + RoomManager.localPerson?.id)
                        var intent = Intent(this@AutoSubscribeActivity, AutoSubscribeRoomDetailsActivity::class.java)
                        RoomManager.type = "P2P"
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            btnJoinSFURoom.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    RoomManager.room = SFURoom.find(id = roomName.text.toString())
                    if(RoomManager.room == null) {
                        RoomManager.room = SFURoom.findOrCreate(roomName.text.toString())
                        if(RoomManager.room == null) {
                            Toast.makeText(this@AutoSubscribeActivity, "Room find or findOrCreate failed", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }
                    Log.d(tag, "Room id: " + RoomManager.room?.id)
                    Toast.makeText(this@AutoSubscribeActivity,"Room OK", Toast.LENGTH_SHORT).show()

                    RoomManager.localPerson = RoomManager.room!!.join(memberInit)
                    if (RoomManager.localPerson != null) {
                        Log.d(tag, "localPerson: " + RoomManager.localPerson?.id)
                        val intent = Intent(this@AutoSubscribeActivity, AutoSubscribeRoomDetailsActivity::class.java)
                        RoomManager.type = "SFU"
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        }

    }

}