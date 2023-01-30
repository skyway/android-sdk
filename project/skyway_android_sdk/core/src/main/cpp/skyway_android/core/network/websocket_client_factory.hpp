//
//  websocket_client_factory.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/network/interface/websocket_client.hpp>

#include "core/network/websocket_client.hpp"

#ifndef SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_FACTORY_HPP
#define SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_FACTORY_HPP

namespace skyway_android {
namespace network {

class WebSocketClientFactory :  public skyway::network::interface::WebSocketClientFactory {
public:
    WebSocketClientFactory(jobject j_ws_factory);
    ~WebSocketClientFactory();

    std::shared_ptr<skyway::network::interface::WebSocketClient> Create() override;

private:
    jobject _j_ws_factory;
};

}  // namespace network
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_FACTORY_HPP */
