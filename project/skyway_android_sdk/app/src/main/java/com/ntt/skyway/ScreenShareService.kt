package com.ntt.skyway

import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.ScreenSource

@RequiresApi(Build.VERSION_CODES.S)

class ScreenShareService : Service() {
    // Binder given to clients (notice class declaration below)
    private val mBinder: IBinder = ScreenShareServiceMyBinder()

    /**
     * Class used for the client Binder. The Binder object is responsible for returning an instance
     * of "MyService" to the client.
     */
    inner class ScreenShareServiceMyBinder : Binder() {
        // Return this instance of MyService so clients can call public methods
        val service: ScreenShareService
            get() =// Return this instance of MyService so clients can call public methods
                this@ScreenShareService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()

        startForeground(1, notification)
        return START_NOT_STICKY
    }
    @Suppress("DEPRECATION")
    private fun createNotification(): Notification {
        val notificationChannelId = "SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Service channel"
                it.enableLights(true)
                it.enableVibration(true)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, FLAG_MUTABLE)
            }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("Service")
            .setContentText("service working")
            .setContentIntent(pendingIntent)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    fun getScreenShareStream(data: Intent): LocalVideoStream {
        ScreenSource.setup(applicationContext, data)
        ScreenSource.stopCapturing()
        ScreenSource.startCapturing(800, 800, 30)
        return ScreenSource.createStream()
    }

    fun changeCapturingSize(width: Int, height: Int) {
        ScreenSource.changeCapturingSize(width, height)
    }
}
