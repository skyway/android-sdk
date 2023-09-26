//
//  websocket_client_factory.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//
#ifndef SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_FACTORY_WRAPPER_HPP
#define SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_FACTORY_WRAPPER_HPP

#include <jni.h>

#include <skyway/network/interface/websocket_client.hpp>

// Link to android-sdk...cpp/skyway_android/network
#include <core/network/websocket_client.hpp>
#include <core/network/websocket_client_factory.hpp>

namespace skyway {
namespace network {

class WebSocketClientFactory : public skyway::network::interface::WebSocketClientFactory {
public:
    static void Setup(jobject j_http);
    WebSocketClientFactory();
    std::shared_ptr<skyway::network::interface::WebSocketClient> Create();

private:
    static jobject j_ws_;
    std::unique_ptr<skyway_android::network::WebSocketClientFactory> factory_;
};

}  // namespace network
}  // namespace skyway

#endif /* SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_FACTORY_WRAPPER_HPP */
