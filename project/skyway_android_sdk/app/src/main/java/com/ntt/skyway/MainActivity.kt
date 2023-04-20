package com.ntt.skyway

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntt.skyway.App.Companion.scope
import com.ntt.skyway.App.Companion.setupJob
import com.ntt.skyway.adapter.RecyclerViewAdapterMember
import com.ntt.skyway.channel.ChannelDetailsActivity
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.Channel
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.databinding.ActivityMainBinding
import com.ntt.skyway.manager.ChannelManager
import com.ntt.skyway.room.RoomActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName

    private val channelName = "channel_" + (Math.random() * 100).toInt().toString()
    private val memberName = "member_" + (Math.random() * 100).toInt().toString()
    private val recyclerViewAdapterMember: RecyclerViewAdapterMember = RecyclerViewAdapterMember()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        checkPermission()

        val findOrCreateJob = scope.launch {
            setupJob?.join()
            Log.d(tag, "setup: ${SkyWayContext.isSetup}")
            val channel = Channel.findOrCreate(channelName) ?: run {
                showMessage("findOrCreate Failed")
                return@launch
            }
            Log.d(tag, "channelId: ${channel.id}")
            ChannelManager.channel = channel
            initChannelFunctions()
        }

        ChannelManager.membersLiveData.observe(this) {
            recyclerViewAdapterMember.setData(it)
        }

        binding.apply {
            channelName.text = this@MainActivity.channelName
            memberName.setText(this@MainActivity.memberName)

            btnJoinChannel.setOnClickListener {
                val memberName = binding.memberName.text.toString()
                val memberInit = Member.Init(memberName)
                scope.launch {
                    findOrCreateJob.join()
                    ChannelManager.localPerson = ChannelManager.channel?.join(memberInit)
                    if (ChannelManager.localPerson != null) {
                        startActivity(Intent(this@MainActivity, ChannelDetailsActivity::class.java))
                    } else {
                        showMessage("Joined Failed")
                    }
                }
            }

            btnDisposeChannel.setOnClickListener {
                ChannelManager.channel?.dispose()
            }

            btnRoom.setOnClickListener {
                startActivity(Intent(this@MainActivity, RoomActivity::class.java))
            }

            rvUserList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvUserList.adapter = recyclerViewAdapterMember
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PERMISSION_GRANTED
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

    private fun initChannelFunctions() {
        ChannelManager.update()
        ChannelManager.channel?.apply {
            onMemberListChangedHandler = {
                this@MainActivity.onMemberListChanged()
            }
        }
    }

    private fun onMemberListChanged() {
        Log.d(tag, "$tag onMembershipChanged")
        runOnUiThread {
            ChannelManager.update()
        }
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        Log.d(tag, "$tag onDestroy")
        super.onDestroy()
        ChannelManager.channel?.dispose()
        SkyWayContext.dispose()
    }
}
