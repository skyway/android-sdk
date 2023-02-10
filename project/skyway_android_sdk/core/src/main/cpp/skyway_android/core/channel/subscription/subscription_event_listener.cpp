//
//  subscription_event_listener.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "subscription_event_listener.hpp"
#include "core/util/call_java_method.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {

SubscriptionEventListener::SubscriptionEventListener(jobject j_subscription) : core::EventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    _j_subscription = env->NewGlobalRef(j_subscription);
}

SubscriptionEventListener::~SubscriptionEventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_subscription);
}

void SubscriptionEventListener::OnCanceled() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_subscription, "onCanceled", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

void SubscriptionEventListener::OnEnabled() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_subscription, "onEnabled", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

void SubscriptionEventListener::OnDisabled() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_subscription, "onDisabled", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

}  // namespace core
}  // namespace skyway_android
