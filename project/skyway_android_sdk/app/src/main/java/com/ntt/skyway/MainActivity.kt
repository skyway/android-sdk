package com.ntt.skyway

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.ntt.skyway.manager.ChannelManager
import com.ntt.skyway.manager.Manager
import com.ntt.skyway.manager.RoomManager
import com.ntt.skyway.manager.SFURoomManager
import com.ntt.skyway.plugin.sfuBot.SFUBotPlugin
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class MainActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName

    private val channelRoomName = UUID.randomUUID().toString()
    private val memberName = UUID.randomUUID().toString()

    private lateinit var binding: ActivityMainBinding

    private val managers = arrayOf(App.channelManager,App.roomManager,App.sfuManager)

    private fun initQRCoder(){
        val qrcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result ->
            if (result.contents == null) {
                App.showMessage("QRScan Canceled")
                return@registerForActivityResult
            }
            val sessionName =
                result.contents.replace(Regex(".*channel="), "").replace(Regex("&env=.*"), "")
            binding.channelRoomName.setText(sessionName)
            binding.swIDorName.isChecked = true
            App.showMessage("QRScan Success")
        }
        binding.btnScanQR.setOnClickListener {
            val options = ScanOptions()
                .setBeepEnabled(false)
            qrcodeLauncher.launch(options)
        }
    }

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission(this, App.appContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        binding.channelRoomName.setText(channelRoomName)
        binding.memberName.setText(memberName)

        initQRCoder()

        binding.btnContextSetup.isEnabled = false
        binding.btnContextDispose.isEnabled = true
        binding.btnJoin.isEnabled = false
        binding.btnClose.isEnabled = false
        binding.btnDispose.isEnabled = false
        binding.swIDorName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.btnFindOrCreate.isEnabled = false
                binding.btnCreate.isEnabled = false
            } else {
                binding.btnFindOrCreate.isEnabled = true
                binding.btnCreate.isEnabled = true
            }
        }

        binding.btnContextSetup.setOnClickListener {
            SkyWayContext.registerPlugin(SFUBotPlugin())
            SkyWayContext.onReconnectStartHandler = {}
            SkyWayContext.onReconnectSuccessHandler = {}
            SkyWayContext.onErrorHandler = {}
            runBlocking {
                val result = SkyWayContext.setup(App.appContext, App.option)
                if (result) {
                    binding.btnContextSetup.isEnabled = false
                    binding.btnContextDispose.isEnabled = true
                    App.showMessage("setup success")
                }
            }
        }

        binding.btnContextDispose.setOnClickListener {
            SkyWayContext.dispose()
            binding.btnContextSetup.isEnabled = true
            binding.btnContextDispose.isEnabled = false
            App.showMessage("dispose success")
        }

        binding.selector.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, managers)
        binding.selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerParent = parent as Spinner
                App.currentManager = spinnerParent.selectedItem as Manager
                binding.btnJoin.isEnabled = false
                binding.btnClose.isEnabled = false
                binding.btnDispose.isEnabled = false
            }
        }

        binding.btnFindOrCreate.setOnClickListener {
            GlobalScope.launch {
                if (!App.currentManager.findOrCreate(binding.channelRoomName.text.toString())) {
                    App.showMessage("findOrCreate failed : ${binding.channelRoomName.text}")
                    return@launch
                }
                App.showMessage("findOrCreate success : ${binding.channelRoomName.text}")
                runOnUiThread {
                    binding.btnJoin.isEnabled = true
                    binding.btnClose.isEnabled = true
                    binding.btnDispose.isEnabled = true
                }
            }
        }

        binding.btnFind.setOnClickListener {
            GlobalScope.launch {
                val result :Boolean
                val text = binding.channelRoomName.text.toString()
                result = if (!binding.swIDorName.isChecked) {
                    App.currentManager.find(
                        name = text,
                        id = null,
                    )
                } else {
                    App.currentManager.find(
                        name = null,
                        id = text,
                    )
                }
                if (!result) {
                    App.showMessage("find failed : ${binding.channelRoomName.text}")
                    return@launch
                }
                App.showMessage("find success : ${binding.channelRoomName.text}")
                runOnUiThread {
                    binding.btnJoin.isEnabled = true
                    binding.btnClose.isEnabled = true
                    binding.btnDispose.isEnabled = true
                }
            }
        }

        binding.btnCreate.setOnClickListener {
            GlobalScope.launch {
                if (!App.currentManager.create(binding.channelRoomName.text.toString())) {
                    App.showMessage("create failed : ${binding.channelRoomName.text}")
                    return@launch
                }
                App.showMessage("create success : ${binding.channelRoomName.text}")
                runOnUiThread {
                    binding.btnJoin.isEnabled = true
                    binding.btnClose.isEnabled = true
                    binding.btnDispose.isEnabled = true
                }
            }
        }

        binding.btnJoin.setOnClickListener {
            GlobalScope.launch {
                if (App.currentManager.join(binding.memberName.text.toString(), null)) {
                    App.showMessage("join success : ${binding.memberName.text}")
                    when (App.currentManager) {
                        is ChannelManager -> {
                            startActivity(Intent(this@MainActivity, ChannelDetailsActivity::class.java))
                        }
                        is RoomManager -> {
                            startActivity(Intent(this@MainActivity, P2PRoomDetailsActivity::class.java))
                        }
                        is SFURoomManager -> {
                            startActivity(Intent(this@MainActivity, SFURoomDetailsActivity::class.java))
                        }
                    }
                } else {
                    App.showMessage("join failed : ${binding.memberName.text}")
                }
            }
        }

        binding.btnClose.setOnClickListener {
            GlobalScope.launch {
                if (!App.currentManager.close()) {
                    App.showMessage("close failed : ${binding.channelRoomName.text}")
                    return@launch
                }
                App.showMessage("close success : ${binding.channelRoomName.text}")
                runOnUiThread {
                    binding.btnJoin.isEnabled = false
                    binding.btnClose.isEnabled = false
                    binding.btnDispose.isEnabled = false
                }
            }
        }

        binding.btnDispose.setOnClickListener {
            GlobalScope.launch {
                if (!App.currentManager.dispose()) {
                    App.showMessage("dispose failed")
                    return@launch
                }
                App.showMessage("dispose success")
                runOnUiThread {
                    binding.btnJoin.isEnabled = false
                    binding.btnClose.isEnabled = false
                    binding.btnDispose.isEnabled = false
                }
            }
        }
    }
    override fun onDestroy() {
        runBlocking {
            Log.d(tag, "$tag onDestroy")
            super.onDestroy()
            managers.forEach { it.dispose() }
            SkyWayContext.dispose()
        }
    }

    private fun checkPermission(activity: Activity, applicationContext: Context) {
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
                activity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }
}
