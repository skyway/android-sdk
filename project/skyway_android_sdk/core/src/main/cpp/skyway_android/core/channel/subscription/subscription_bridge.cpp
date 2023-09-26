//
//  subscription_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "subscription_bridge.hpp"

#include <json.hpp>

#include "core/util/register_methods_helper.hpp"
#include "core/channel/subscription/subscription_event_listener.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/context/context_bridge.hpp"
#include "core/channel/channel/channel_bridge.hpp"
#include "core/channel/channel_util.hpp"

namespace skyway_android {
namespace core {

using SubscriptionState = skyway::core::interface::SubscriptionState;


bool SubscriptionBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeAddEventListener",
            "(Ljava/lang/String;J)V",
            (void*) SubscriptionBridge::AddEventListener
        },
        {
            "nativeState",
            "(J)Ljava/lang/String;",
            (void*) SubscriptionBridge::State
        },{
                "nativePreferredEncodingId",
                "(J)Ljava/lang/String;",
                (void*) SubscriptionBridge::PreferredEncodingId
        },
        {
            "nativeCancel",
            "(J)Z",
            (void*) SubscriptionBridge::Cancel
        },
        {
            "nativeEnable",
            "(J)Z",
            (void*) SubscriptionBridge::Enable
        },
        {
            "nativeChangePreferredEncoding",
            "(JLjava/lang/String;)V",
            (void*) SubscriptionBridge::ChangePreferredEncoding
        },
        {
            "nativeDisable",
            "(J)Z",
            (void*) SubscriptionBridge::Disable
        },
        {
            "nativeGetStats",
            "(J)Ljava/lang/String;",
            (void*) SubscriptionBridge::GetStats
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/SubscriptionImpl",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void SubscriptionBridge::AddEventListener(JNIEnv* env, jobject j_this, jstring j_channel_id, jlong subscription) {
    auto subscription_event_listener = new SubscriptionEventListener(j_this);
    ((Subscription*)subscription)->AddEventListener(subscription_event_listener);
    auto channel_id = JStringToStdString(env, j_channel_id);
    channel::ChannelBridge::AddInternalEventListener(channel_id, subscription_event_listener);
}

jstring SubscriptionBridge::State(JNIEnv* env, jobject j_this, jlong subscription) {
    auto state = ((Subscription*)subscription)->State();
    switch (state) {
        case SubscriptionState::kEnabled:
            return env->NewStringUTF("enabled");
        case SubscriptionState::kDisabled:
            return env->NewStringUTF("disabled");
        case SubscriptionState::kCanceled:
            return env->NewStringUTF("canceled");
    }
}

jstring SubscriptionBridge::PreferredEncodingId(JNIEnv* env, jobject j_this, jlong subscription) {
    auto preferred_encoding_id_ = ((Subscription*)subscription)->PreferredEncodingId();
    return preferred_encoding_id_ ? env->NewStringUTF(preferred_encoding_id_->c_str()) : env->NewStringUTF("");
}

bool SubscriptionBridge::Cancel(JNIEnv* env, jobject j_this, jlong subscription) {
    return ((Subscription*)subscription)->Cancel();
}

bool SubscriptionBridge::Enable(JNIEnv* env, jobject j_this, jlong subscription) {
    return ((Subscription*)subscription)->Enable();
}

bool SubscriptionBridge::Disable(JNIEnv* env, jobject j_this, jlong subscription) {
    return ((Subscription*)subscription)->Disable();
}

void SubscriptionBridge::ChangePreferredEncoding(JNIEnv* env, jobject j_this, jlong subscription, jstring j_id) {
    auto id = JStringToStdString(env, j_id);
    ((Subscription*)subscription)->ChangePreferredEncoding(id);
}

jstring SubscriptionBridge::GetStats(JNIEnv* env, jobject j_this, jlong subscription) {
    auto stats = ((Subscription*)subscription)->GetStats();
    if(!stats){
        return nullptr;
    }
    auto stats_json = util::getWebRTCStatsJson(&stats.get());
    return env->NewStringUTF(stats_json.dump().c_str());
}

}  // namespace core
}  // namespace skyway_android
