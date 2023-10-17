/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local.source

import android.media.AudioRecord
import com.ntt.skyway.core.content.Factory
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.WebRTCManager
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.audio.WebRtcAudioRecord
import java.nio.ByteBuffer

/**
 *  音声入力に関する操作を行うクラス。
 */
object AudioSource {
    val isStarted
        get() = _isStarted

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    var onAudioBufferHandler: ((buffer: ByteBuffer) -> Unit)? = null

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    val audioRecord
        get() = null

    private var source: AudioSource? = null
    private var _isStarted = true

    /**
     *  音声入力を開始します。マイクのインジケータが点灯します。
     */
    @JvmStatic
    fun start() {
        WebRTCManager.startRecording()
        _isStarted = true
    }

    /**
     *  音声入力を終了します。
     *  マイクのインジケータが消灯します。
     */
    @JvmStatic
    fun stop() {
        WebRTCManager.stopRecording()
        _isStarted = false
    }

    @Deprecated("This API is deprecated.", ReplaceWith("", ""))
    fun changeRecord(audioRecord: AudioRecord) { }

    /**
     *  Publish可能な[com.ntt.skyway.core.content.Stream]を生成します。
     */
    @JvmStatic
    fun createStream(): LocalAudioStream {
        if (source == null) {
            source = WebRTCManager.createRTCAudioSource()
        }
        val track = WebRTCManager.createRTCAudioTrack(source!!)
        val streamJson = nativeCreateAudioStream(track)
        return Factory.createLocalAudioStream(streamJson, this, track)
    }

    private external fun nativeCreateAudioStream(track: AudioTrack): String
}
