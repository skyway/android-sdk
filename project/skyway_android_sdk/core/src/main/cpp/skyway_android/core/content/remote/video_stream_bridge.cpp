//
//  video_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "video_stream_bridge.hpp"

#include "core/util/register_methods_helper.hpp"
#include "core/util/native_to_jlong.hpp"

namespace skyway_android {
namespace content {
namespace remote {

bool RemoteVideoStreamBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeGetTrack",
            "(J)J",
            (void*) RemoteVideoStreamBridge::GetTrack
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/remote/RemoteVideoStream",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

long RemoteVideoStreamBridge::GetTrack(JNIEnv* env, jobject j_this, jlong remote_video_stream) {
    auto remote_video_stream_ptr = reinterpret_cast<RemoteVideoStream*>(remote_video_stream);
    return NativeToJlong((remote_video_stream_ptr)->GetTrack().get());
}

}  // namespace remote
}  // namespace content
}  // namespace channel
