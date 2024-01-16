/*
* Copyright © 2022 NTT Communications. All rights reserved.
*/

package com.ntt.skyway.core.content.local.source

import android.content.Context
import com.ntt.skyway.core.util.Logger
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer

/**
 *  カメラデバイスによる映像入力に関する操作を行うクラス。
 */
object CameraSource : VideoSource() {
    data class CapturingOptions(val width: Int, val height: Int, val frameRate: Int = 30)

    /**
     *  カメラが変更が完了したときに発火するハンドラ。
     */
    var onCameraChangedHandler: ((success: Boolean) -> Unit)? = null

    /**
     *  カメラが変更に失敗したときに発火するハンドラ。
     */
    var onCameraChangeErrorHandler: ((reason: String?) -> Unit)? = null

    private var capturer: CameraVideoCapturer? = null

    private val cameraEventsHandler = object : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(p0: String?) {
            Logger.logE("onCameraError: $p0")
        }

        override fun onCameraDisconnected() {
            Logger.logV("onCameraDisconnected")
        }

        override fun onCameraFreezed(p0: String?) {
            Logger.logV("onCameraFreezed: $p0")
        }

        override fun onCameraOpening(p0: String?) {
            Logger.logV("onCameraOpening: $p0")
        }

        override fun onFirstFrameAvailable() {
            Logger.logV("onFirstFrameAvailable")
        }

        override fun onCameraClosed() {
            Logger.logV("onCameraClosed")
        }
    }

    private val cameraSwitchHandler = object : CameraVideoCapturer.CameraSwitchHandler {
        override fun onCameraSwitchDone(p0: Boolean) {
            onCameraChangedHandler?.invoke(p0)
        }

        override fun onCameraSwitchError(p0: String?) {
            onCameraChangeErrorHandler?.invoke(p0)
        }
    }

    /**
     *  サポートしているカメラデバイスの一覧を取得します。
     */
    @JvmStatic
    fun getCameras(context: Context): Set<String> {
        val enumerator = createCameraEnumerator(context)
        return enumerator.deviceNames.toSet()
    }

    /**
     *  サポートしているフロントカメラデバイスの一覧を取得します。
     */
    @JvmStatic
    fun getFrontCameras(context: Context): Set<String> {
        val enumerator = createCameraEnumerator(context)
        return enumerator.deviceNames.filter { enumerator.isFrontFacing(it) }.toSet()
    }

    /**
     *  サポートしているバックカメラデバイスの一覧を取得します。
     */
    @JvmStatic
    fun getBackCameras(context: Context): Set<String> {
        val enumerator = createCameraEnumerator(context)
        return enumerator.deviceNames.filter { enumerator.isBackFacing(it) }.toSet()
    }

    /**
     *  カメラデバイスを変更します。
     *  キャプチャ中にカメラデバイスを変更したい場合に利用してください。
     *  デバイス名は[getCameras]、[getFrontCameras]、[getBackCameras]から取得してください。
     *
     *  @param deviceName cameraのデバイス名
     */
    @JvmStatic
    fun changeCamera(deviceName: String) {
        capturer?.switchCamera(cameraSwitchHandler, deviceName)
    }

    /**
     *  カメラデバイスからキャプチャを開始します。
     *  カメラのインジケータが点灯します。
     */
    @JvmStatic
    fun startCapturing(context: Context, deviceName: String, options: CapturingOptions) {
        if (!this.isInitialized) {
            this.initialize()
        }
        capturer?.apply {
            stopCapture()
            dispose()
        }
        val enumerator = createCameraEnumerator(context)
        capturer = enumerator.createCapturer(deviceName, cameraEventsHandler)
        capturer?.initialize(textureHelper, context, source.capturerObserver)
        capturer?.startCapture(options.width, options.height, options.frameRate)
    }

    /**
     *  カメラデバイスからキャプチャを停止します。
     *  カメラのインジケータが消灯します。
     */
    @JvmStatic
    fun stopCapturing() {
        capturer?.stopCapture()
    }

    private fun createCameraEnumerator(context: Context): CameraEnumerator {
        return if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }
    }
}
