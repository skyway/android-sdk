//
//  subscription_event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/subscription.hpp>
#include <skyway/core/connection_state.hpp>

#include "core/event_listener.hpp"

#ifndef SKYWAY_ANDROID_CORE_SUBSCRIPTION_SUBSCRIPTION_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_CORE_SUBSCRIPTION_SUBSCRIPTION_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {

class SubscriptionEventListener : public skyway::core::interface::Subscription::EventListener, public EventListener {
public:
    SubscriptionEventListener(jobject j_subscription);
    ~SubscriptionEventListener();

    void OnCanceled() override;
    void OnEnabled() override;
    void OnDisabled() override;
    void OnConnectionStateChanged(const skyway::core::ConnectionState new_state) override;

private:
    jobject _j_subscription;
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_SUBSCRIPTION_SUBSCRIPTION_EVENT_LISTENER_HPP */
