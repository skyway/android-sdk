package com.ntt.skyway.core

import android.annotation.SuppressLint
import android.content.Context
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.ScreenSource
import com.ntt.skyway.core.util.Logger
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.*

@SuppressLint("StaticFieldLeak")
internal object WebRTCManager {
    class WebRTCLog : Loggable {
        companion object {
            fun webRTCSeverityToLogLevel(severity: Logging.Severity): Logger.LogLevel {
                return when (severity) {
                    Logging.Severity.LS_VERBOSE -> Logger.LogLevel.VERBOSE
                    Logging.Severity.LS_INFO -> Logger.LogLevel.INFO
                    Logging.Severity.LS_WARNING -> Logger.LogLevel.WARN
                    Logging.Severity.LS_ERROR -> Logger.LogLevel.ERROR
                    Logging.Severity.LS_NONE -> Logger.LogLevel.NONE
                }
            }
        }

        override fun onLogMessage(message: String, severity: Logging.Severity, tag: String) {
            if (!Logger.webRTCLog) return
            Logger.log(webRTCSeverityToLogLevel(severity), message, tag)
        }
    }

    val nativePCFactory
        get() = pcFactory.nativePeerConnectionFactory
    val eglBaseContext: EglBase.Context
        get() = egl.eglBaseContext

    var isSetup = false

    internal val videoSourceList: MutableList<com.ntt.skyway.core.content.local.source.VideoSource> = mutableListOf()
    private lateinit var egl: EglBase
    private lateinit var audioDeviceModule: JavaAudioDeviceModule
    private lateinit var pcFactory: PeerConnectionFactory

    fun setup(context: Context, enableHardwareCodec: Boolean) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setInjectableLogger(WebRTCLog(), Logging.Severity.LS_VERBOSE)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        egl = EglBase.create()
        val videoEncoderFactory = if (enableHardwareCodec) DefaultVideoEncoderFactory(
            eglBaseContext,
            true,
            true
        ) else SoftwareVideoEncoderFactory()
        val videoDecoderFactory =
            if (enableHardwareCodec) DefaultVideoDecoderFactory(eglBaseContext) else SoftwareVideoDecoderFactory()

        val audioDeviceModuleBuilder = JavaAudioDeviceModule.builder(context)
        audioDeviceModule = audioDeviceModuleBuilder.createAudioDeviceModule()

        pcFactory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()

        CameraSource.initialize()
        ScreenSource.initialize()

        isSetup = true
    }

    fun createRTCVideoSource(): VideoSource {
        return pcFactory.createVideoSource(false)
    }

    fun createRTCVideoTrack(source: VideoSource): VideoTrack {
        return pcFactory.createVideoTrack(UUID.randomUUID().toString(), source)
    }

    fun createRTCAudioSource(): org.webrtc.AudioSource {
        return pcFactory.createAudioSource(MediaConstraints())
    }

    fun createRTCAudioTrack(source: org.webrtc.AudioSource): AudioTrack {
        return pcFactory.createAudioTrack(UUID.randomUUID().toString(), source)
    }

    fun createSurfaceTextureHelper(): SurfaceTextureHelper {
        return SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
    }

    fun startRecording() {
        check(isSetup) { "Please setup first" }
        audioDeviceModule.audioInput.audioRecord?.startRecording()
    }

    fun stopRecording() {
        check(isSetup) { "Please setup first" }
        audioDeviceModule.audioInput.audioRecord?.stop()
    }

    fun dispose() {
        videoSourceList.forEach {
            it.dispose()
        }
        videoSourceList.clear()

        pcFactory.dispose()
        egl.release()
    }
}
