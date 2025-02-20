package com.ntt.skyway.examples

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.examples.autosubscribe.AutoSubscribeActivity
import com.ntt.skyway.examples.common.manager.SampleManager
import com.ntt.skyway.examples.databinding.ActivityMainBinding
import com.ntt.skyway.examples.p2proom.P2PRoomActivity
import com.ntt.skyway.examples.sfuroom.SFURoomActivity
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private val authToken = TODO("replace your auth token here")

    private lateinit var binding: ActivityMainBinding

    private val scope = CoroutineScope(Dispatchers.IO)
    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
        permissions: Array<String>,
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
            }

        }
    }

    private fun initUI() {
        binding.apply {
            btnP2PRoom.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    SampleManager.type = SampleManager.Type.P2P_ROOM
                    startActivity(Intent(this@MainActivity, P2PRoomActivity::class.java))
                }
            }
            btnSFURoom.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    SampleManager.type = SampleManager.Type.SFU_ROOM
                    startActivity(Intent(this@MainActivity, SFURoomActivity::class.java))
                }
            }
            btnAutoSubscribe.setOnClickListener {
                scope.launch(Dispatchers.Main) {
                    SampleManager.type = SampleManager.Type.AUTO_SUBSCRIBE
                    startActivity(Intent(this@MainActivity, AutoSubscribeActivity::class.java))
                }
            }
        }
    }

}
