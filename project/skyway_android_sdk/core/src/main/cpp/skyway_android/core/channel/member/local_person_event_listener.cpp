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
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_local_person, "onLeft", "()V");
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnMetadataUpdated(const std::string &metadata) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        auto j_metadata = env->NewStringUTF(metadata.c_str());
        CallJavaMethod(env, _j_local_person, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnStreamPublished(Publication* publication) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
        CallJavaMethod(env, _j_local_person, "onStreamPublished", "(Ljava/lang/String;)V", j_publication_id);
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnStreamUnpublished(Publication* publication) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
        CallJavaMethod(env, _j_local_person, "onStreamUnpublished", "(Ljava/lang/String;)V", j_publication_id);
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnPublicationSubscribed(Subscription* subscription) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
        CallJavaMethod(env, _j_local_person, "onPublicationSubscribed", "(Ljava/lang/String;)V", j_subscription_id);
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnPublicationUnsubscribed(Subscription* subscription) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
        CallJavaMethod(env, _j_local_person, "onPublicationUnsubscribed", "(Ljava/lang/String;)V", j_subscription_id);
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnPublicationListChanged() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_local_person, "onPublicationListChanged", "()V");
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

void LocalPersonEventListener::OnSubscriptionListChanged() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=]{
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_local_person, "onSubscriptionListChanged", "()V");
        ContextBridge::DetachCurrentThread();
    });
    _threads.emplace_back(std::move(thread));
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
