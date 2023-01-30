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

    /**
     *  音声デバイスからデータを取得した際に発火するハンドラ。
     *  このハンドラ内での処理後、bufferが対向に送信されます。
     */
    @SkyWayOptIn
    var onAudioBufferHandler: ((buffer: ByteBuffer) -> Unit)? = null

    /**
     *  音声入力デバイス。
     *  新たに[AudioRecord]を作成する際に、このプロパティの設定を参照をすることができます。
     */
    val audioRecord
        get() = WebRTCManager.audioRecord


    @OptIn(SkyWayOptIn::class)
    internal val onAudioBufferListener =
        WebRtcAudioRecord.OnAudioBufferListener { buffer ->
            onAudioBufferHandler?.invoke(buffer)
        }

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

    /**
     *  音声入力デバイスを切り替えます。切り替え前のデバイスからの取得は停止します。
     *
     *  @param audioRecord 切り替え先のオーディオデバイス。
     */
    @SkyWayOptIn
    @JvmStatic
    fun changeRecord(audioRecord: AudioRecord) {
        WebRTCManager.audioRecord = audioRecord
    }

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
