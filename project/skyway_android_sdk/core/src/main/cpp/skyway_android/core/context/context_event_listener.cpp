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
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaStaticMethod(env, _j_context, "onReconnectStart", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

void ContextEventListener::OnReconnectSuccess() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaStaticMethod(env, _j_context, "onReconnectSuccess", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

void ContextEventListener::OnFatalError(const skyway::global::Error& error) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        auto j_message = env->NewStringUTF(error.message.c_str());
        CallJavaStaticMethod(env, _j_context, "onFatalError", "(Ljava/lang/String;)V", j_message);
    });
    _threads.emplace_back(std::move(thread));
}

}  // namespace core
}  // namespace skyway_android
