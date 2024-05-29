package com.ntt.skyway.core

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceInfo
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.VideoSource
import com.ntt.skyway.core.util.Logger
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Loggable
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SoftwareVideoDecoderFactory
import org.webrtc.SoftwareVideoEncoderFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.UUID

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

    val nativePCFactory: Long?
        get() = pcFactory?.nativePeerConnectionFactory
    val eglBaseContext: EglBase.Context?
        get() = egl?.eglBaseContext

    var isSetup = false

    internal val videoSourceList: MutableList<VideoSource> = mutableListOf()
    private var egl: EglBase? = null
    private var audioDeviceModule: JavaAudioDeviceModule? = null
    private var pcFactory: PeerConnectionFactory? = null

    fun setup(context: Context, enableHardwareCodec: Boolean, audioSource: Int?) {
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

        val audioRecordStateCallback = object : JavaAudioDeviceModule.AudioRecordStateCallback {
            override fun onWebRtcAudioRecordStart() {
                if (!AudioSource.isStarted) {
                    AudioSource.stop()
                }
            }

            override fun onWebRtcAudioRecordStop() {
            }
        }

        val audioDeviceModuleBuilder = JavaAudioDeviceModule.builder(context)
        audioDeviceModule = audioDeviceModuleBuilder.apply {
            setAudioRecordStateCallback(audioRecordStateCallback)
            audioSource?.let { setAudioSource(audioSource) }
        }.createAudioDeviceModule()

        audioDeviceModule?.audioInput?.onAudioBufferListener = AudioSource.onAudioBufferListener

        pcFactory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()

        isSetup = true
    }

    fun createRTCVideoSource(): org.webrtc.VideoSource? {
        return pcFactory?.createVideoSource(false)
    }

    fun createRTCVideoTrack(source: org.webrtc.VideoSource): VideoTrack? {
        return pcFactory?.createVideoTrack(UUID.randomUUID().toString(), source)
    }

    fun createRTCAudioSource(): org.webrtc.AudioSource? {
        return pcFactory?.createAudioSource(MediaConstraints())
    }

    fun createRTCAudioTrack(source: org.webrtc.AudioSource): AudioTrack? {
        return pcFactory?.createAudioTrack(UUID.randomUUID().toString(), source)
    }

    fun createSurfaceTextureHelper(): SurfaceTextureHelper {
        return SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
    }

    fun startRecording() {
        check(isSetup) { "Please setup first" }
        audioDeviceModule?.audioInput?.audioRecord?.startRecording()
    }

    fun stopRecording() {
        check(isSetup) { "Please setup first" }
        audioDeviceModule?.audioInput?.audioRecord?.stop()
    }

    fun setPreferredInputDevice(audioDeviceInfo: AudioDeviceInfo) {
        audioDeviceModule?.setPreferredInputDevice(audioDeviceInfo)
    }

    fun dispose() {
        videoSourceList.forEach {
            it.dispose()
        }
        videoSourceList.clear()

        pcFactory?.dispose()
        egl?.release()
    }
}
