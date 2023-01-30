//
//  data_source_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "data_source_bridge.hpp"

#include <skyway/core/stream/local/data_stream.hpp>

#include "core/content/content_util.hpp"
#include "core/util/register_methods_helper.cpp"

namespace skyway_android {
namespace content {
namespace local {
namespace source {

using LocalDataStream = skyway::core::stream::local::LocalDataStream;

bool DataSource::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeCreateDataStream",
            "()Ljava/lang/String;",
            (void*) DataSource::CreateDataStream
        }
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/local/source/DataSource",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring DataSource::CreateDataStream(JNIEnv* env, jobject j_this) {
    auto local_data_stream = new LocalDataStream();
    auto local_data_stream_json = util::getStreamDataJson(local_data_stream);
    return env->NewStringUTF(local_data_stream_json.dump().c_str());
}

}  // namespace source
}  // namespace local
}  // namespace content
}  // namespace skyway_android
