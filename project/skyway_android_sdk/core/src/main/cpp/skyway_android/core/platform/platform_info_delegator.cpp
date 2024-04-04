//
//  platform_info_delegator.cpp
//  skyway_android
//
//  Copyright © 2024 NTT Communications. All rights reserved.
//

#include "core/platform/platform_info_delegator.hpp"
#include "core/context/context_bridge.hpp"
#include "core/util/jstring_to_string.hpp"


namespace skyway_android {
namespace core {
namespace platform {

std::string PlatformInfoDelegator::GetPlatform() const {
    return "android";
}

std::string PlatformInfoDelegator::GetOsInfo() const {
    auto env = ContextBridge::AttachCurrentThread();
    jclass build_class = env->FindClass("android/os/Build$VERSION");
    jfieldID release_id = env->GetStaticFieldID(build_class, "RELEASE", "Ljava/lang/String;");
    jstring release_str  = (jstring)env->GetStaticObjectField(build_class, release_id);
    auto build_version = JStringToStdString(env, release_str);
    env->DeleteLocalRef(release_str);
    ContextBridge::DetachCurrentThread();
    return "Android " + build_version;
}

std::string PlatformInfoDelegator::GetModelName() const {
    auto env = ContextBridge::AttachCurrentThread();
    jclass build_class = env->FindClass("android/os/Build");
    jfieldID model_id = env->GetStaticFieldID(build_class, "MODEL", "Ljava/lang/String;");
    jstring model_str  = (jstring)env->GetStaticObjectField(build_class, model_id);
    auto device_name = JStringToStdString(env, model_str);
    env->DeleteLocalRef(model_str);
    ContextBridge::DetachCurrentThread();
    return device_name;
}

}  // namespace platform
}  // namespace core
}  // namespace skyway_android
