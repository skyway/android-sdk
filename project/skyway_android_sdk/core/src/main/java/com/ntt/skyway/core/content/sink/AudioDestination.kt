/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.sink

import android.media.AudioTrack
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.WebRTCManager
import org.webrtc.audio.WebRtcAudioTrack
import java.nio.ByteBuffer

object AudioDestination {
    /**
     *  音声デバイスからデータを取得した際に発火するハンドラ。
     *  このハンドラ内での処理後、bufferが対向に送信されます。
     */
    @SkyWayOptIn
    var onAudioBufferHandler: ((buffer: ByteBuffer) -> Unit)? = null

    /**
     *  音声出力デバイス。
     *  新たに[AudioTrack]を作成する際に、このプロパティの設定を参照をすることができます。
     */
    val audioTrack
        get() = WebRTCManager.audioTrack

    @OptIn(SkyWayOptIn::class)
    internal val onAudioBufferListener =
        WebRtcAudioTrack.OnAudioBufferListener { buffer ->
            onAudioBufferHandler?.invoke(buffer)
        }

    /**
     *  音声出力デバイスを切り替えます。切り替え前のデバイスへの出力は停止します。
     *
     *  @param audioTrack 切り替え先のオーディオデバイス。
     */
    @SkyWayOptIn
    @JvmStatic
    fun changeTrack(audioTrack: AudioTrack) {
        WebRTCManager.stopPlayout()
        WebRTCManager.audioTrack = audioTrack
        WebRTCManager.startPlayout()
    }
}
