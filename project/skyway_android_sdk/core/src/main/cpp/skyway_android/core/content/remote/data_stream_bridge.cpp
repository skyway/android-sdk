//
//  data_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "data_stream_bridge.hpp"

#include "date_stream_listener.hpp"
#include "core/util/register_methods_helper.hpp"

namespace skyway_android {
namespace content {
namespace remote {

jobject RemoteDataStreamBridge::j_remote_data_stream_global;

bool RemoteDataStreamBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeAddListener",
            "(J)J",
            (void*) RemoteDataStreamBridge::AddListener
        },
        {
            "nativeDispose",
            "(J)V",
            (void*) RemoteDataStreamBridge::Dispose
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/remote/RemoteDataStream",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jlong RemoteDataStreamBridge::AddListener(JNIEnv* env, jobject j_this, jlong remote_data_stream) {
    j_remote_data_stream_global = env->NewGlobalRef(j_this);
    auto listener = new RemoteDataStreamListener(j_remote_data_stream_global);
    ((RemoteDataStream*)remote_data_stream)->AddListener(listener);
    return (jlong) j_remote_data_stream_global;
}

void RemoteDataStreamBridge::Dispose(JNIEnv* env, jobject j_this, jlong j_remote_data_stream_global_ref) {
    jobject j_remote_data_stream = (jobject) j_remote_data_stream_global_ref;
    env->DeleteGlobalRef(j_remote_data_stream);
}

}  // namespace remote
}  // namespace content
}  // namespace channel
