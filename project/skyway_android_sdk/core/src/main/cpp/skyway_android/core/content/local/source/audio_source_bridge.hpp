//
//  audio_source_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>
#include <api/media_stream_interface.h>

#ifndef SKYWAY_ANDROID_CONTENT_LOCAL_SOURCE_AUDIO_SOURCE_BRIDGE_HPP
#define SKYWAY_ANDROID_CONTENT_LOCAL_SOURCE_AUDIO_SOURCE_BRIDGE_HPP

namespace skyway_android {
namespace content {
namespace local {
namespace source {

using AudioTrack = webrtc::AudioTrackInterface;

class AudioSource {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jstring CreateAudioStream(JNIEnv* env, jobject j_this, jobject j_track);

private:
    static rtc::scoped_refptr <AudioTrack> GetAudioTrackPointer(JNIEnv* env, jobject j_track);
};

}  // namespace source
}  // namespace local
}  // namespace content
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CONTENT_LOCAL_SOURCE_AUDIO_SOURCE_BRIDGE_HPP */
