/*
* Copyright © 2022 NTT Communications. All rights reserved.
*/

package com.ntt.skyway.core.content.local.source

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import com.ntt.skyway.core.util.Logger
import org.webrtc.ScreenCapturerAndroid

/**
 *  画面共有による映像入力に関する操作を行うクラス。
 */
object ScreenSource : VideoSource() {
    private lateinit var capturer: ScreenCapturerAndroid

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Logger.logV("mediaProjection: onStop")
        }
    }

    /**
     *  初期化します。
     *  画面共有はユーザが確認可能な通知のサービスが立ち上がっている必要があります。
     */
    @JvmStatic
    fun setup(context: Context, mediaProjectionPermissionResultData: Intent) {
        if (!this.isInitialized) {
            this.initialize()
        }
        capturer = ScreenCapturerAndroid(
            mediaProjectionPermissionResultData, mediaProjectionCallback
        )
        capturer.initialize(textureHelper, context, source.capturerObserver)
    }

    /**
     *  画面のキャプチャを開始します。
     *  先に[ScreenSource.setup]を行ってください。
     */
    @JvmStatic
    fun startCapturing(width: Int, height: Int, frameRate: Int) {
        capturer.startCapture(width, height, frameRate)
    }

    /**
     *  画面のキャプチャを停止します。
     *  先に[ScreenSource.setup]を行ってください。
     */
    @JvmStatic
    fun stopCapturing() {
        capturer.stopCapture()
    }
}
