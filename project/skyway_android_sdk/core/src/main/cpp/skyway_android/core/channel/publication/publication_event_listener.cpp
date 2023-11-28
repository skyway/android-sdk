//
//  publication_event_listener.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "publication_event_listener.hpp"
#include "core/util/call_java_method.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {

PublicationEventListener::PublicationEventListener(jobject j_publication) : core::EventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    _j_publication = env->NewGlobalRef(j_publication);
}

PublicationEventListener::~PublicationEventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_publication);
}

void PublicationEventListener::OnUnpublished() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_publication, "onUnpublished", "()V");
}

void PublicationEventListener::OnSubscribed(skyway::core::interface::Subscription* subscription) {
    auto subscription_json = util::ToJson(subscription);
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_json = env->NewStringUTF(subscription_json.dump().c_str());
    CallJavaMethod(env, _j_publication, "onSubscribed", "(Ljava/lang/String;)V", j_subscription_json);
    env->DeleteLocalRef(j_subscription_json);
}

void PublicationEventListener::OnUnsubscribed(skyway::core::interface::Subscription* subscription) {
    auto subscription_json = util::ToJson(subscription);
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_json = env->NewStringUTF(subscription_json.dump().c_str());
    CallJavaMethod(env, _j_publication, "onUnsubscribed", "(Ljava/lang/String;)V", j_subscription_json);
    env->DeleteLocalRef(j_subscription_json);
}

void PublicationEventListener::OnSubscriptionListChanged() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_publication, "onSubscriptionListChanged", "()V");
}

void PublicationEventListener::OnMetadataUpdated(const std::string& metadata) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_publication, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
    env->DeleteLocalRef(j_metadata);
}

void PublicationEventListener::OnEnabled() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_publication, "onEnabled", "()V");
}

void PublicationEventListener::OnDisabled() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_publication, "onDisabled", "()V");
}

void PublicationEventListener::OnConnectionStateChanged(const skyway::core::ConnectionState new_state) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_new_state = env->NewStringUTF(skyway::core::StringFromConnectionState(new_state).get().c_str());
    CallJavaMethod(env, _j_publication, "onConnectionStateChanged", "(Ljava/lang/String;)V", j_new_state);
    env->DeleteLocalRef(j_new_state);
}

}  // namespace core
}  // namespace skyway_android
