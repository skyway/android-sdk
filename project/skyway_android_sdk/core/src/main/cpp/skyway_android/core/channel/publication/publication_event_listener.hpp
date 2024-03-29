//
//  publication_event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/publication.hpp>
#include <skyway/core/connection_state.hpp>

#include "core/event_listener.hpp"
#include "core/channel/channel_util.hpp"

#ifndef SKYWAY_ANDROID_CORE_PUBLICATION_PUBLICATION_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_CORE_PUBLICATION_PUBLICATION_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {

class PublicationEventListener : public skyway::core::interface::Publication::EventListener, public EventListener {
public:
    PublicationEventListener(jobject j_publication);
    ~PublicationEventListener();

    void OnUnpublished() override;
    void OnSubscribed(skyway::core::interface::Subscription* subscription) override;
    void OnUnsubscribed(skyway::core::interface::Subscription* subscription) override;
    void OnSubscriptionListChanged() override;
    void OnMetadataUpdated(const std::string& metadata) override;
    void OnEnabled() override;
    void OnDisabled() override;
    void OnConnectionStateChanged(const skyway::core::ConnectionState new_state) override;
private:
    jobject _j_publication;
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_PUBLICATION_PUBLICATION_EVENT_LISTENER_HPP */
