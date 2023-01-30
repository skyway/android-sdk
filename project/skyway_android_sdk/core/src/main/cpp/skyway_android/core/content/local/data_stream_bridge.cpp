//
//  data_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "data_stream_bridge.hpp"
#include <skyway/global/interface/logger.hpp>
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"

namespace skyway_android {
namespace content {
namespace local {

bool LocalDataStreamBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeWrite",
            "(Ljava/lang/String;J)V",
            (void*) LocalDataStreamBridge::Write
        },
        {
            "nativeWriteByteArray",
            "([BJ)V",
            (void*) LocalDataStreamBridge::WriteByteArray
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/local/LocalDataStream",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void LocalDataStreamBridge::Write(JNIEnv* env, jobject j_this, jstring j_data, jlong local_data_stream) {
    auto data = JStringToStdString(env, j_data);
    ((LocalDataStream*)local_data_stream)->Write(data);
}

void LocalDataStreamBridge::WriteByteArray(JNIEnv* env, jobject j_this, jbyteArray j_array, jlong local_data_stream) {
    jsize count = env->GetArrayLength(j_array);
    if (count == 0) {
        SKW_ERROR("array length is zero");
        return;
    }

    jbyte* elements = env->GetByteArrayElements(j_array, nullptr);
    if (!elements) {
        SKW_ERROR("array elements is nullptr");
        return;
    }

    ((LocalDataStream*)local_data_stream)->Write((const uint8_t*)elements, (size_t)count);
    env->ReleaseByteArrayElements(j_array, elements, 0);

}

}  // namespace local
}  // namespace content
}  // namespace channel
