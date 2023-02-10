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
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_publication, "onUnpublished", "()V");

    });
    _threads.emplace_back(std::move(thread));
}

void PublicationEventListener::OnSubscribed() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_publication, "onSubscribed", "()V");

    });
    _threads.emplace_back(std::move(thread));
}

void PublicationEventListener::OnUnsubscribed() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_publication, "onUnsubscribed", "()V");

    });
    _threads.emplace_back(std::move(thread));
}

void PublicationEventListener::OnSubscriptionListChanged() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_publication, "onSubscriptionListChanged", "()V");

    });
    _threads.emplace_back(std::move(thread));
}

void PublicationEventListener::OnMetadataUpdated(const std::string& metadata) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        auto j_metadata = env->NewStringUTF(metadata.c_str());
        CallJavaMethod(env, _j_publication, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);

    });
    _threads.emplace_back(std::move(thread));
}

void PublicationEventListener::OnEnabled() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_publication, "onEnabled", "()V");

    });
    _threads.emplace_back(std::move(thread));
}

void PublicationEventListener::OnDisabled() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = ContextBridge::AttachCurrentThread();
        CallJavaMethod(env, _j_publication, "onDisabled", "()V");

    });
    _threads.emplace_back(std::move(thread));
}

}  // namespace core
}  // namespace skyway_android
