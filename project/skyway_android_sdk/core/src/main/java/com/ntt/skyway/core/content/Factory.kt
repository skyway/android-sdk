package com.ntt.skyway.core.content

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.VideoSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.core.content.remote.RemoteStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import org.webrtc.AudioTrack
import org.webrtc.VideoTrack

internal object Factory {
    fun createLocalAudioStream(
        streamJson: String,
        source: AudioSource,
        track: AudioTrack
    ): LocalAudioStream {
        val streamDto = createStreamDto(streamJson)
        return LocalAudioStream(source, track, streamDto)
    }

    fun createLocalVideoStream(
        streamJson: String,
        source: VideoSource,
        track: VideoTrack
    ): LocalVideoStream {
        val streamDto = createStreamDto(streamJson)
        return LocalVideoStream(source, track, streamDto)
    }

    fun createLocalDataStream(streamJson: String): LocalDataStream {
        val streamDto = createStreamDto(streamJson)
        return LocalDataStream(streamDto)
    }

    fun createRemoteStream(contentType: Stream.ContentType, streamJson: String): RemoteStream {
        val streamDto = createStreamDto(streamJson)
        return when (contentType) {
            Stream.ContentType.AUDIO -> RemoteAudioStream(streamDto)
            Stream.ContentType.VIDEO -> RemoteVideoStream(streamDto)
            Stream.ContentType.DATA -> RemoteDataStream(streamDto)
        }
    }

    private fun createStreamDto(streamJson: String): Stream.Dto {
        val dto = Gson().fromJson(streamJson, JsonObject::class.java)
        return Stream.Dto(
            id = dto.get("id").asString,
            nativePointer = dto.get("nativePointer").asLong
        )
    }
}
