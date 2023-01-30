//
//  local_person_event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/publication.hpp>
#include <skyway/core/interface/subscription.hpp>
#include <skyway/core/channel/member/local_person.hpp>

#ifndef SKYWAY_ANDROID_CORE_MEMBER_LOCAL_PERSON_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_CORE_MEMBER_LOCAL_PERSON_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {
namespace member {

using Publication = skyway::core::interface::Publication;
using Subscription = skyway::core::interface::Subscription;

class LocalPersonEventListener : public skyway::core::channel::member::LocalPerson::EventListener {

public:
    LocalPersonEventListener(jobject j_local_person);
    ~LocalPersonEventListener();

    void OnLeft() override;
    void OnMetadataUpdated(const std::string &metadata) override;
    void OnStreamPublished(Publication* publication) override;
    void OnStreamUnpublished(Publication* publication) override;
    void OnPublicationSubscribed(Subscription* subscription) override;
    void OnPublicationUnsubscribed(Subscription* subscription) override;
    void OnPublicationListChanged() override;
    void OnSubscriptionListChanged() override;

private:
    jobject _j_local_person;
};

}  // namespace member
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_MEMBER_LOCAL_PERSON_EVENT_LISTENER_HPP */
