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
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_subscription, "onCanceled", "()V");
}

void SubscriptionEventListener::OnEnabled() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_subscription, "onEnabled", "()V");
}

void SubscriptionEventListener::OnDisabled() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_subscription, "onDisabled", "()V");
}

void SubscriptionEventListener::OnConnectionStateChanged(const skyway::core::ConnectionState new_state) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_new_state = env->NewStringUTF(skyway::core::StringFromConnectionState(new_state).get().c_str());
    CallJavaMethod(env, _j_subscription, "onConnectionStateChanged", "(Ljava/lang/String;)V", j_new_state);
    env->DeleteLocalRef(j_new_state);
}

}  // namespace core
}  // namespace skyway_android
