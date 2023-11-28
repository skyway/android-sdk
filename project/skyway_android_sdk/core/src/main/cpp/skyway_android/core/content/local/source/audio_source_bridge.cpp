//
//  audio_source_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "audio_source_bridge.hpp"

// libskyway
#include <skyway/core/stream/local/audio_stream.hpp>

#include "core/content/content_util.hpp"
#include "core/util/register_methods_helper.cpp"
#include "core/util/jstring_to_string.hpp"

namespace skyway_android {
namespace content {
namespace local {
namespace source {

using LocalAudioStream = skyway::core::stream::local::LocalAudioStream;

bool AudioSource::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeCreateAudioStream",
            "(Lorg/webrtc/AudioTrack;)Ljava/lang/String;",
            (void*) AudioSource::CreateAudioStream
        }
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/local/source/AudioSource",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring
AudioSource::CreateAudioStream(JNIEnv* env, jobject j_this, jobject j_track) {
    auto audio_track_ptr = AudioSource::GetAudioTrackPointer(env, j_track);
    auto local_audio_stream = new LocalAudioStream(audio_track_ptr);
    auto local_audio_stream_json = util::getStreamDataJson(local_audio_stream);
    return env->NewStringUTF(local_audio_stream_json.dump().c_str());
}

rtc::scoped_refptr <AudioTrack> AudioSource::GetAudioTrackPointer(JNIEnv* env, jobject j_track) {
    auto j_class = env->GetObjectClass(j_track);
    auto method_id = env->GetMethodID(j_class, "getNativeAudioTrack", "()J");
    auto audio_track_ptr = reinterpret_cast<AudioTrack*>(env->CallLongMethod(j_track, method_id));
    env->DeleteLocalRef(j_class);
    return rtc::scoped_refptr<AudioTrack>(audio_track_ptr);
}

}  // namespace source
}  // namespace local
}  // namespace content
}  // namespace skyway_android
