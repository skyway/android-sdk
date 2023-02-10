//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/context.hpp>
#include <skyway/global/error.hpp>

#include "core/event_listener.hpp"

#ifndef SKYWAY_ANDROID_CORE_CONTEXT_AUTH_TOKEN_MANAGER_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_CORE_CONTEXT_AUTH_TOKEN_MANAGER_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {

class AuthTokenManagerEventListener : public skyway::token::interface::AuthTokenManager::Listener, public EventListener  {
public:
    AuthTokenManagerEventListener(jobject j_context);
    ~AuthTokenManagerEventListener();

    void OnTokenRefreshingNeeded() override;
    void OnTokenExpired() override;

private:
    jobject _j_context;
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CONTEXT_AUTH_TOKEN_MANAGER_EVENT_LISTENER_HPP */
