//
//  data_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/stream/local/data_stream.hpp>

#ifndef SKYWAY_ANDROID_CONTENT_LOCAL_DATA_STREAM_BRIDGE_HPP
#define SKYWAY_ANDROID_CONTENT_LOCAL_DATA_STREAM_BRIDGE_HPP

namespace skyway_android {
namespace content {
namespace local {

using LocalDataStream = skyway::core::stream::local::LocalDataStream;

class LocalDataStreamBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static void Write(JNIEnv* env, jobject j_this, jstring j_data, jlong local_data_stream);
    static void WriteByteArray(JNIEnv* env, jobject j_this, jbyteArray j_array, jlong local_data_stream);

};

}  // namespace local
}  // namespace content
}  // namespace channel

#endif /* SKYWAY_ANDROID_CONTENT_LOCAL_DATA_STREAM_BRIDGE_HPP */
