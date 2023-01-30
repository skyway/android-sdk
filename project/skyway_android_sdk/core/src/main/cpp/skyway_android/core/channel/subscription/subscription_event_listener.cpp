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

SubscriptionEventListener::SubscriptionEventListener(jobject j_subscription) {
    auto env = ContextBridge::GetEnv();
    _j_subscription = env->NewGlobalRef(j_subscription);
}

SubscriptionEventListener::~SubscriptionEventListener() {
    auto env = ContextBridge::GetEnv();
    env->DeleteGlobalRef(_j_subscription);
}

void SubscriptionEventListener::OnCanceled() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_subscription, "onCanceled", "()V");
}

void SubscriptionEventListener::OnEnabled() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_subscription, "onEnabled", "()V");
}

void SubscriptionEventListener::OnDisabled() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_subscription, "onDisabled", "()V");
}

}  // namespace core
}  // namespace skyway_android
