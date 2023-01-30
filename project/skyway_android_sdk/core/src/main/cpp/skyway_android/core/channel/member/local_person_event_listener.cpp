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

LocalPersonEventListener::LocalPersonEventListener(jobject j_local_person) {
    auto env = ContextBridge::GetEnv();
    _j_local_person = env->NewGlobalRef(j_local_person);
}

LocalPersonEventListener::~LocalPersonEventListener() {
    auto env = ContextBridge::GetEnv();
    env->DeleteGlobalRef(_j_local_person);
}

void LocalPersonEventListener::OnLeft() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_local_person, "onLeft", "()V");
}

void LocalPersonEventListener::OnMetadataUpdated(const std::string &metadata) {
    auto env = ContextBridge::GetEnv();
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_local_person, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
}

void LocalPersonEventListener::OnStreamPublished(Publication* publication) {
    auto publication_json = util::ToJson(publication);
    auto env = ContextBridge::GetEnv();
    auto j_publication_json = env->NewStringUTF(publication_json.dump().c_str());
    CallJavaMethod(env, _j_local_person, "onStreamPublished", "(Ljava/lang/String;)V", j_publication_json);
}

void LocalPersonEventListener::OnStreamUnpublished(Publication* publication) {
    auto publication_json = util::ToJson(publication);
    auto env = ContextBridge::GetEnv();
    auto j_publication_json = env->NewStringUTF(publication_json.dump().c_str());
    CallJavaMethod(env, _j_local_person, "onStreamUnpublished", "(Ljava/lang/String;)V", j_publication_json);
}

void LocalPersonEventListener::OnPublicationSubscribed(Subscription* subscription) {
    auto subscription_json = util::ToJson(subscription);
    auto env = ContextBridge::GetEnv();
    auto j_subscription_json = env->NewStringUTF(subscription_json.dump().c_str());
    CallJavaMethod(env, _j_local_person, "onPublicationSubscribed", "(Ljava/lang/String;)V", j_subscription_json);
}

void LocalPersonEventListener::OnPublicationUnsubscribed(Subscription* subscription) {
    auto subscription_json = util::ToJson(subscription);
    auto env = ContextBridge::GetEnv();
    auto j_subscription_json = env->NewStringUTF(subscription_json.dump().c_str());
    CallJavaMethod(env, _j_local_person, "onPublicationUnsubscribed", "(Ljava/lang/String;)V", j_subscription_json);
}

void LocalPersonEventListener::OnPublicationListChanged() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_local_person, "onPublicationListChanged", "()V");
}

void LocalPersonEventListener::OnSubscriptionListChanged() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_local_person, "onSubscriptionListChanged", "()V");
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
