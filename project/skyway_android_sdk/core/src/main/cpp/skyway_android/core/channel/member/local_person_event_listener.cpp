//
//  local_person_event_listener.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "local_person_event_listener.hpp"

#include "core/channel/channel_util.hpp"
#include "core/util/call_java_method.hpp"
#include <skyway/global/interface/logger.hpp>
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {
namespace member {

LocalPersonEventListener::LocalPersonEventListener(jobject j_local_person) : core::EventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    _j_local_person = env->NewGlobalRef(j_local_person);
}

LocalPersonEventListener::~LocalPersonEventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_local_person);
}

void LocalPersonEventListener::OnLeft() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_local_person, "onLeft", "()V");
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnMetadataUpdated(const std::string &metadata) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_local_person, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
    env->DeleteLocalRef(j_metadata);
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnStreamPublished(Publication* publication) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
    CallJavaMethod(env, _j_local_person, "onStreamPublished", "(Ljava/lang/String;)V", j_publication_id);
    env->DeleteLocalRef(j_publication_id);
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnStreamUnpublished(Publication* publication) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
    CallJavaMethod(env, _j_local_person, "onStreamUnpublished", "(Ljava/lang/String;)V", j_publication_id);
    env->DeleteLocalRef(j_publication_id);
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnPublicationSubscribed(Subscription* subscription) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
    CallJavaMethod(env, _j_local_person, "onPublicationSubscribed", "(Ljava/lang/String;)V", j_subscription_id);
    env->DeleteLocalRef(j_subscription_id);
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnPublicationUnsubscribed(Subscription* subscription) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
    CallJavaMethod(env, _j_local_person, "onPublicationUnsubscribed", "(Ljava/lang/String;)V", j_subscription_id);
    env->DeleteLocalRef(j_subscription_id);
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnPublicationListChanged() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_local_person, "onPublicationListChanged", "()V");
    ContextBridge::DetachCurrentThread();
}

void LocalPersonEventListener::OnSubscriptionListChanged() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_local_person, "onSubscriptionListChanged", "()V");
    ContextBridge::DetachCurrentThread();
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
