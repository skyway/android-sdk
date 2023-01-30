//
//  channel_event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/channel.hpp>

#ifndef SKYWAY_ANDROID_CORE_CHANNEL_CHANNEL_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_CORE_CHANNEL_CHANNEL_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {
namespace channel {

using Member = skyway::core::interface::Member;
using Publication = skyway::core::interface::Publication;
using Subscription = skyway::core::interface::Subscription;

class ChannelEventListener : public skyway::core::interface::Channel::EventListener {
public:
    ChannelEventListener(jobject j_channel);
    ~ChannelEventListener();

    void OnClosed() override;
    void OnMetadataUpdated(const std::string& metadata) override;
    void OnMemberListChanged() override;
    void OnMemberJoined(Member* member) override;
    void OnMemberLeft(Member* member) override;
    void OnMemberMetadataUpdated(Member* member, const std::string& metadata) override;
    void OnPublicationMetadataUpdated(Publication* publication, const std::string& metadata) override;
    void OnPublicationListChanged() override;
    void OnStreamPublished(Publication* publication) override;
    void OnStreamUnpublished(Publication* publication) override;
    void OnPublicationEnabled(Publication* publication) override;
    void OnPublicationDisabled(Publication* publication) override;
    void OnSubscriptionListChanged() override;
    void OnPublicationSubscribed(Subscription* subscription) override;
    void OnPublicationUnsubscribed(Subscription* subscription) override;
    void OnSubscriptionEnabled(Subscription* subscription) override;
    void OnSubscriptionDisabled(Subscription* subscription) override;

private:
    jobject _j_channel;
};

}  // namespace channel
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CHANNEL_CHANNEL_EVENT_LISTENER_HPP */
