//
//  data_source_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#ifndef SKYWAY_ANDROID_CONTENT_LOCAL_SOURCE_DATA_SOURCE_BRIDGE_HPP
#define SKYWAY_ANDROID_CONTENT_LOCAL_SOURCE_DATA_SOURCE_BRIDGE_HPP

namespace skyway_android {
namespace content {
namespace local {
namespace source {

class DataSource {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jstring CreateDataStream(JNIEnv* env, jobject j_this);

};

}  // namespace source
}  // namespace local
}  // namespace content
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CONTENT_LOCAL_SOURCE_DATA_SOURCE_BRIDGE_HPP */
