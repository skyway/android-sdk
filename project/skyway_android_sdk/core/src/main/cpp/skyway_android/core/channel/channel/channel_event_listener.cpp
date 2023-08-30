//
//  channel_event_listener.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/channel/channel/channel_event_listener.hpp"

#include "core/channel/channel_util.hpp"
#include "core/util/call_java_method.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {
namespace channel {

ChannelEventListener::ChannelEventListener(jobject j_channel) : core::EventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    _j_channel = env->NewGlobalRef(j_channel);
}

ChannelEventListener::~ChannelEventListener() {
    auto env = ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_channel);
}

void ChannelEventListener::OnClosed() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_channel, "onClosed", "()V");
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnMetadataUpdated(const std::string& metadata) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_channel, "onMetadataUpdated", "(Ljava/lang/String;)V", j_metadata);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnMemberListChanged() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_channel, "onMemberListChanged", "()V");
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnMemberJoined(Member* member) {
    auto member_json = util::ToJson(member);
    auto env = ContextBridge::AttachCurrentThread();
    auto j_member_json = env->NewStringUTF(member_json.dump().c_str());
    CallJavaMethod(env, _j_channel, "onMemberJoined", "(Ljava/lang/String;)V", j_member_json);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnMemberLeft(Member* member) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_member_id = env->NewStringUTF(member->Id().c_str());
    CallJavaMethod(env, _j_channel, "onMemberLeft", "(Ljava/lang/String;)V", j_member_id);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnMemberMetadataUpdated(Member* member, const std::string& metadata) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_member_id = env->NewStringUTF(member->Id().c_str());
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_channel, "onMemberMetadataUpdated", "(Ljava/lang/String;Ljava/lang/String;)V", j_member_id, j_metadata);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnStreamPublished(Publication* publication) {
    auto publication_json = util::ToJson(publication);
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_json = env->NewStringUTF(publication_json.dump().c_str());
    CallJavaMethod(env, _j_channel, "onStreamPublished", "(Ljava/lang/String;)V", j_publication_json);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnStreamUnpublished(Publication* publication) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
    CallJavaMethod(env, _j_channel, "onStreamUnpublished", "(Ljava/lang/String;)V", j_publication_id);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnPublicationListChanged() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_channel, "onPublicationListChanged", "()V");
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnPublicationEnabled(Publication* publication) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
    CallJavaMethod(env, _j_channel, "onPublicationEnabled", "(Ljava/lang/String;)V", j_publication_id);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnPublicationDisabled(Publication* publication) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
    CallJavaMethod(env, _j_channel, "onPublicationDisabled", "(Ljava/lang/String;)V", j_publication_id);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnPublicationMetadataUpdated(Publication* publication, const std::string& metadata) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_publication_id = env->NewStringUTF(publication->Id().c_str());
    auto j_metadata = env->NewStringUTF(metadata.c_str());
    CallJavaMethod(env, _j_channel, "onPublicationMetadataUpdated", "(Ljava/lang/String;Ljava/lang/String;)V", j_publication_id, j_metadata);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnSubscriptionListChanged() {
    auto env = ContextBridge::AttachCurrentThread();
    CallJavaMethod(env, _j_channel, "onSubscriptionListChanged", "()V");
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnPublicationSubscribed(Subscription* subscription) {
    auto subscription_json = util::ToJson(subscription);
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_json = env->NewStringUTF(subscription_json.dump().c_str());
    CallJavaMethod(env, _j_channel, "onPublicationSubscribed", "(Ljava/lang/String;)V", j_subscription_json);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnPublicationUnsubscribed(Subscription* subscription) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
    CallJavaMethod(env, _j_channel, "onPublicationUnsubscribed", "(Ljava/lang/String;)V", j_subscription_id);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnSubscriptionEnabled(Subscription* subscription) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
    CallJavaMethod(env, _j_channel, "onSubscriptionEnabled", "(Ljava/lang/String;)V", j_subscription_id);
    ContextBridge::DetachCurrentThread();
}

void ChannelEventListener::OnSubscriptionDisabled(Subscription* subscription) {
    auto env = ContextBridge::AttachCurrentThread();
    auto j_subscription_id = env->NewStringUTF(subscription->Id().c_str());
    CallJavaMethod(env, _j_channel, "onSubscriptionDisabled", "(Ljava/lang/String;)V", j_subscription_id);
    ContextBridge::DetachCurrentThread();
}

}  // namespace channel
}  // namespace core
}  // namespace skyway_android
