//
//  context_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>
#include <map>
#include <mutex>
#include <skyway/core/context.hpp>

#ifndef SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_BRIDGE_HPP

namespace skyway_android {
namespace core {

class ContextBridge {
public:
    static bool RegisterMethods(JNIEnv* env);
    static jboolean Setup(JNIEnv* env, jobject j_this, jstring j_authToken, jstring j_options,
                          jlong j_pc_factory, jobject j_http, jobject j_ws_factory, jobject j_logger);
    static jboolean UpdateAuthToken(JNIEnv* env, jobject j_this, jstring j_auth_token);
    static void SetJavaVMFromEnv(JNIEnv* env);
    static JNIEnv* GetEnv();
    static void Dispose(JNIEnv* env, jobject j_this);

private:
    static void ApplyContextOptions(skyway::core::ContextOptions& options, nlohmann::json& domain_options_json);
    static JavaVM* jvm;
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_BRIDGE_HPP */
