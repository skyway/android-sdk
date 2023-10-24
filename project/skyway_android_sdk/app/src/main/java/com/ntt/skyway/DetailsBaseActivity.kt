package com.ntt.skyway

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore.Audio
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.CustomVideoFrameSource
import com.ntt.skyway.core.content.local.source.DataSource


open class DetailsBaseActivity : AppCompatActivity() {
    private var mService: ScreenShareService? = null
    private var mIsBound = false

    abstract class SelectableStream {
        abstract fun select()
        abstract fun getStream() : LocalStream?
    }

    class CustomVideoFrameStream : SelectableStream() {
        override fun select(){}

        override fun getStream() : LocalStream {
            val videoSource = CustomVideoFrameSource(800, 800)

            val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val handler = Handler(Looper.getMainLooper())
            val gray = Paint()
            gray.color = android.graphics.Color.GRAY
            val cyan = Paint()
            cyan.color = android.graphics.Color.CYAN
            var count = 0
            handler.post(object : Runnable {
                override fun run() {
                    canvas.drawRect(0F, 0F, 800F, 800F, gray)
                    canvas.drawRect(count * 100F, 200F, count * 100F + 100F, 300F, cyan)
                    count++
                    if (count > 8) count = 0
                    videoSource.updateFrame(bitmap, 0)
                    handler.postDelayed(this, 100)
                }
            })
            return videoSource.createStream()
        }
        override fun toString(): String {
            return "CustomVideoFrame"
        }
    }
    class CameraStream : SelectableStream() {
        override fun select(){}
        override fun getStream() : LocalStream {
            val device = CameraSource.getFrontCameras(App.appContext).first()
            CameraSource.startCapturing(
                App.appContext,
                device,
                CameraSource.CapturingOptions(800, 800)
            )
            return CameraSource.createStream()
        }
        override fun toString(): String {
            return "Camera"
        }
    }
    class AudioStream : SelectableStream() {
        override fun select(){}
        @OptIn(SkyWayOptIn::class)
        override fun getStream() : LocalStream {
            AudioSource.start()
            AudioSource.onAudioBufferHandler = {
//                Log.d("onAudioBufferHandler", it.get().toString())
            }
            return AudioSource.createStream()
        }
        override fun toString(): String {
            return "Audio"
        }
    }
    class DataStream : SelectableStream() {
        override fun select(){}
        override fun getStream(): LocalStream {
            val localDataSource = DataSource()
            return localDataSource.createStream()
        }
        override fun toString(): String {
            return "Data"
        }
    }
    class ScreenShareStream(private val activity: DetailsBaseActivity) : SelectableStream() {
        var stream: LocalVideoStream? = null
        override fun select(){
            if(stream == null){
                activity.startScreenCapture()
            }
        }
        override fun getStream(): LocalStream? {
            return stream
        }

        override fun toString(): String {
            return "ScreenShare"
        }
    }

    private val startActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent = result.data ?: return@registerForActivityResult
        val stream = mService?.getScreenShareStream(data)!!
        screenShareStream.stream = stream
    }

    private val cameraStream = CameraStream()
    private val audioStream = AudioStream()
    private val dataStream = DataStream()
    private val customVideoFrameStream = CustomVideoFrameStream()
    private val screenShareStream = ScreenShareStream(this)

    var streamList = mutableListOf(
        cameraStream,
        audioStream,
        dataStream,
        customVideoFrameStream,
        screenShareStream,
    )

    override fun onStop() {
        super.onStop()
        stopScreenShareService()
    }

    private fun startScreenCapture() {
        startScreenShareService()
        val mediaProjectionManager = application.getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        startActivity.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            val binder = iBinder as ScreenShareService.ScreenShareServiceMyBinder
            mService = binder.service
            mIsBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mIsBound = false
        }
    }

    private fun startScreenShareService() {
        val myServiceIntent = Intent(this, ScreenShareService::class.java)
        ContextCompat.startForegroundService(this, myServiceIntent)
        bindService()
    }

    private fun stopScreenShareService() {
        val serviceIntent = Intent(this, ScreenShareService::class.java)
        stopService(serviceIntent)
    }

    private fun bindService() {
        Intent(this, ScreenShareService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        Intent(this, ScreenShareService::class.java).also {
            unbindService(serviceConnection)
        }
    }
}
