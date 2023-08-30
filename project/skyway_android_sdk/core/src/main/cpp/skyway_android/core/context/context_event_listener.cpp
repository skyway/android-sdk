//
//  context_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "context_event_listener.hpp"

#include <skyway/global/interface/logger.hpp>

#include "core/context/context_bridge.hpp"
#include "core/util/call_java_method.hpp"

namespace skyway_android {
namespace core {

ContextEventListener::ContextEventListener(jobject j_context) : core::EventListener() {
    auto env = core::ContextBridge::AttachCurrentThread();
    _j_context = env->NewGlobalRef(j_context);
}

ContextEventListener::~ContextEventListener() {
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_context);
}

void ContextEventListener::OnReconnectStart() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaStaticMethod(env, _j_context, "onReconnectStart", "()V");
}

void ContextEventListener::OnReconnectSuccess() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaStaticMethod(env, _j_context, "onReconnectSuccess", "()V");
}

void ContextEventListener::OnFatalError(const skyway::global::Error& error) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_message = env->NewStringUTF(error.message.c_str());
    CallJavaStaticMethod(env, _j_context, "onFatalError", "(Ljava/lang/String;)V", j_message);
}

}  // namespace core
}  // namespace skyway_android
