package com.ntt.skyway.examples.sfuroom

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.examples.sfuroom.databinding.ActivityMainBinding
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.*
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private val authToken = "YOUR_TOKEN"

    private lateinit var binding: ActivityMainBinding

    private val scope = CoroutineScope(Dispatchers.IO)
    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        checkPermission()
        initUI()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                ),
                0
            )
        } else {
            setupSkyWayContext()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty()
            && grantResults[0] == PermissionChecker.PERMISSION_GRANTED
            && grantResults[1] == PermissionChecker.PERMISSION_GRANTED){
            setupSkyWayContext()
        } else {
            Log.e("App","permission denied")
        }

    }

    private fun setupSkyWayContext(){
        scope.launch(Dispatchers.Default) {
            val option = SkyWayContext.Options(
                authToken = authToken,
                logLevel = Logger.LogLevel.VERBOSE
            )
            val result =  SkyWayContext.setup(applicationContext, option)
            if (result) {
                Log.d("App", "Setup succeed")
                SFURoomManager.isSkywayContextSetup = true
            }

        }

    }

    private fun initUI() {
        binding.apply {

            btnJoinChannel.setOnClickListener {
                val memberInit: RoomMember.Init
                if(binding.memberName.text.toString().isEmpty()){
                    memberInit = RoomMember.Init(UUID.randomUUID().toString())
                } else {
                    memberInit = RoomMember.Init(binding.memberName.text.toString())
                }


                scope.launch(Dispatchers.Main) {
                    SFURoomManager.sfuRoom = SFURoom.findOrCreate(roomName.text.toString())
                    if(SFURoomManager.sfuRoom == null) {
                        Toast.makeText(this@MainActivity,"Room findOrCreate failed",Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    Log.d(tag, "findOrCreate Room id: " + SFURoomManager.sfuRoom?.id)
                    Toast.makeText(this@MainActivity,"Room findOrCreate OK",Toast.LENGTH_SHORT).show()

                    SFURoomManager.localPerson = SFURoomManager.sfuRoom!!.join(memberInit)
                    if (SFURoomManager.localPerson != null) {
                        Log.d(tag, "localPerson: " + SFURoomManager.localPerson?.id)
                        startActivity(Intent(this@MainActivity, RoomDetailsActivity::class.java))
                    } else {
                        Toast.makeText(applicationContext, "Joined Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        }

    }


}
