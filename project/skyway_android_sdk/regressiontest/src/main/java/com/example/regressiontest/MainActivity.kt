package com.example.regressiontest

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.regressiontest.databinding.ActivityMainBinding
import com.example.regressiontest.manager.RoomManager
import com.example.regressiontest.util.Util
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.ntt.skyway.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName

    private lateinit var binding: ActivityMainBinding

    private val roomName = ""
    private val memberName = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        checkPermission()

        binding.roomName.setText(roomName)
        binding.memberName.setText(memberName)

        binding.btnJoinRoom.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val activityIntent = Intent(this@MainActivity, RegressionTestDetailsActivity::class.java)
                activityIntent.putExtra("RoomName",binding.roomName.text.toString())
                activityIntent.putExtra("MemberName",binding.memberName.text.toString())
                startActivity(activityIntent)
            }
        }

        val qrcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result ->
            if (result.contents == null) {
                Util.showToast(applicationContext, "QRScan Canceled")
                return@registerForActivityResult
            }
            val sessionName = result.contents.replace(Regex(".*sessionName="),"")
            binding.roomName.setText(sessionName)
            Util.showToast(applicationContext, "QRScan Success")
        }
        binding.btnScanQRCode.setOnClickListener {
            val options = ScanOptions()
                .setOrientationLocked(false)
                .setBeepEnabled(false)
            qrcodeLauncher.launch(options)
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
}
