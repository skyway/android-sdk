//
//  audio_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "audio_stream_bridge.hpp"

#include "core/util/register_methods_helper.hpp"

namespace skyway_android {
namespace content {
namespace remote {

bool RemoteAudioStreamBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeGetTrack",
            "(J)J",
            (void*) RemoteAudioStreamBridge::GetTrack
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/remote/RemoteAudioStream",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

long RemoteAudioStreamBridge::GetTrack(JNIEnv* env, jobject j_this, jlong remote_audio_stream) {
    return (long) (((RemoteAudioStream*)remote_audio_stream)->GetTrack().get());
}

}  // namespace remote
}  // namespace content
}  // namespace channel
