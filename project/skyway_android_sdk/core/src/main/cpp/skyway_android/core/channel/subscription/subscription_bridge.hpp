//
//  subscription_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/subscription.hpp>

#ifndef SKYWAY_ANDROID_CORE_SUBSCRIPTION_SUBSCRIPTION_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_SUBSCRIPTION_SUBSCRIPTION_BRIDGE_HPP

namespace skyway_android {
namespace core {

using Subscription = skyway::core::interface::Subscription;

class SubscriptionBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jstring State(JNIEnv* env, jobject j_this, jlong subscription);

    static void AddEventListener(JNIEnv* env, jobject j_this, jlong subscription);
    static bool Cancel(JNIEnv* env, jobject j_this, jlong subscription);
    static bool Enable(JNIEnv* env, jobject j_this, jlong subscription);
    static bool Disable(JNIEnv* env, jobject j_this, jlong subscription);
    static void ChangePreferredEncoding(JNIEnv* env, jobject j_this, jlong subscription, jstring j_id);
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_SUBSCRIPTION_SUBSCRIPTION_BRIDGE_HPP */
