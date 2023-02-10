//
//  context_event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/context.hpp>
#include <skyway/global/error.hpp>

#include "core/event_listener.hpp"

#ifndef SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {

class ContextEventListener : public skyway::core::Context::EventListener, public EventListener  {
public:
    ContextEventListener(jobject j_context);
    ~ContextEventListener();

    void OnReconnectStart() override;
    void OnReconnectSuccess() override;
    void OnFatalError(const skyway::global::Error& error) override;

private:
    jobject _j_context;
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CONTEXT_CONTEXT_EVENT_LISTENER_HPP */
