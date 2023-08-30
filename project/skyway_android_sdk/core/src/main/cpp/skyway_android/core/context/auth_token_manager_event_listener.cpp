//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "auth_token_manager_event_listener.hpp"

#include <skyway/global/interface/logger.hpp>

#include "core/context/context_bridge.hpp"
#include "core/util/call_java_method.hpp"

namespace skyway_android {
namespace core {

AuthTokenManagerEventListener::AuthTokenManagerEventListener(jobject j_context) : core::EventListener() {
    auto env = core::ContextBridge::AttachCurrentThread();
    _j_context = env->NewGlobalRef(j_context);
}

AuthTokenManagerEventListener::~AuthTokenManagerEventListener() {
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_context);
}

void AuthTokenManagerEventListener::OnTokenRefreshingNeeded() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaStaticMethod(env, _j_context, "onTokenRefreshingNeeded", "()V");
}

void AuthTokenManagerEventListener::OnTokenExpired() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaStaticMethod(env, _j_context, "onTokenExpired", "()V");
}

}  // namespace core
}  // namespace skyway_android
