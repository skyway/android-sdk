//
//  data_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/stream/remote/data_stream.hpp>

#ifndef SKYWAY_ANDROID_CONTENT_REMOTE_DATA_STREAM_BRIDGE_HPP
#define SKYWAY_ANDROID_CONTENT_REMOTE_DATA_STREAM_BRIDGE_HPP

namespace skyway_android {
namespace content {
namespace remote {

using RemoteDataStream = skyway::core::stream::remote::RemoteDataStream;

class RemoteDataStreamBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jlong AddListener(JNIEnv* env, jobject j_this, jlong remote_data_stream);
    static void Dispose(JNIEnv* env, jobject j_this, jlong j_remote_data_stream_global_ref);

private:
    static jobject j_remote_data_stream_global;
};

}  // namespace remote
}  // namespace content
}  // namespace channel

#endif /* SKYWAY_ANDROID_CONTENT_REMOTE_DATA_STREAM_BRIDGE_HPP */
