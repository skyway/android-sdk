//
//  local_person_event_listener.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "member_event_listener.hpp"

#include "core/channel/channel_util.hpp"
#include "core/util/call_java_method.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {
namespace member {
MemberEventListener::MemberEventListener(jobject j_member) {
    auto env = ContextBridge::GetEnv();
    _j_member = env->NewGlobalRef(j_member);
}

MemberEventListener::~MemberEventListener() {
    auto env = ContextBridge::GetEnv();
    env->DeleteGlobalRef(_j_member);
}

void MemberEventListener::OnLeft() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_member, "onLeft", "()V");
}

void MemberEventListener::OnMetadataUpdated(const std::string &metadata) {
    auto env = ContextBridge::GetEnv();
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_member, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
}

void MemberEventListener::OnPublicationListChanged() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_member, "onPublicationListChanged", "()V");
}

void MemberEventListener::OnSubscriptionListChanged() {
    auto env = ContextBridge::GetEnv();
    CallJavaMethod(env, _j_member, "onSubscriptionListChanged", "()V");
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
