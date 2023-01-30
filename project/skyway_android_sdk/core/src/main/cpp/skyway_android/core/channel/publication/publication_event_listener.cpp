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

PublicationEventListener::PublicationEventListener(jobject j_publication) {
    auto env = ContextBridge::GetEnv();
    _j_publication = env->NewGlobalRef(j_publication);
}

PublicationEventListener::~PublicationEventListener() {
    auto env = ContextBridge::GetEnv();
    env->DeleteGlobalRef(_j_publication);
}

void PublicationEventListener::OnUnpublished() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_publication, "onUnpublished", "()V");
}

void PublicationEventListener::OnSubscribed() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_publication, "onSubscribed", "()V");
}

void PublicationEventListener::OnUnsubscribed() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_publication, "onUnsubscribed", "()V");
}

void PublicationEventListener::OnSubscriptionListChanged() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_publication, "onSubscriptionListChanged", "()V");
}

void PublicationEventListener::OnMetadataUpdated(const std::string& metadata) {
    auto env = ContextBridge::GetEnv();
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_publication, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
}

void PublicationEventListener::OnEnabled() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_publication, "onEnabled", "()V");
}

void PublicationEventListener::OnDisabled() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_publication, "onDisabled", "()V");
}

}  // namespace core
}  // namespace skyway_android
