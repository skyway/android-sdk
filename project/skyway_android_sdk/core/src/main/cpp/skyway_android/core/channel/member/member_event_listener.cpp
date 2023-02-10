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
MemberEventListener::MemberEventListener(jobject j_member) : core::EventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    _j_member = env->NewGlobalRef(j_member);
}

MemberEventListener::~MemberEventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_member);
}

void MemberEventListener::OnLeft() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_member, "onLeft", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

void MemberEventListener::OnMetadataUpdated(const std::string &metadata) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        auto j_metadata = env->NewStringUTF(metadata.c_str());
        CallJavaMethod(env, _j_member, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
    });
    _threads.emplace_back(std::move(thread));

}

void MemberEventListener::OnPublicationListChanged() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_member, "onPublicationListChanged", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

void MemberEventListener::OnSubscriptionListChanged() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_member, "onSubscriptionListChanged", "()V");
    });
    _threads.emplace_back(std::move(thread));
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
