/*
* Copyright © 2022 NTT Communications. All rights reserved.
*/

package com.ntt.skyway.core.content.local.source

import android.content.Context
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.local.source.ScreenSource.setup
import org.webrtc.FileVideoCapturer

/**
 *  映像ファイルによる映像入力に関する操作を行うクラス。
 *
 *  @param context コンテキスト。
 *  @param fileName 映像ファイルの名前。[java.io.RandomAccessFile]によってアクセスされます。
 */
@SkyWayOptIn
class VideoFileSource(context: Context, fileName: String): VideoSource() {
    private val capturer: FileVideoCapturer

    init {
        capturer = FileVideoCapturer(fileName)
        capturer.initialize(textureHelper, context, source.capturerObserver)
    }

    /**
     *  映像ファイルのキャプチャを開始します。
     */
    fun startCapturing(width: Int, height: Int, frameRate: Int) {
        capturer.startCapture(width, height, frameRate)
    }

    /**
     *  映像ファイルのキャプチャを停止します。
     */
    fun stopCapturing() {
        capturer.stopCapture()
    }
}
