//
//  context_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>
#include <map>
#include <mutex>
#include <vector>
#include <skyway/core/context.hpp>
#include "context_event_listener.hpp"
#include "auth_token_manager_event_listener.hpp"

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
    static void _UpdateRtcConfig(JNIEnv* env, jobject j_this, jstring j_rtc_config);
    static void SetJavaVMFromEnv(JNIEnv* env);
    static JNIEnv* AttachCurrentThread();
    static void DetachCurrentThread();
    static void Dispose(JNIEnv* env, jobject j_this);

private:
    static void ApplyContextOptions(skyway::core::ContextOptions& options, nlohmann::json& domain_options_json);
    static JavaVM* jvm;
    static std::vector<EventListener*> event_listeners;
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_BRIDGE_HPP */
