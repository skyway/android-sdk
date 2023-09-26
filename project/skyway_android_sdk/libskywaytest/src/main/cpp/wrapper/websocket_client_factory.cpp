//
//  websocket_client_factory.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//
#include "websocket_client_factory.hpp"

#include <jni.h>

#include <skyway/network/interface/websocket_client.hpp>

namespace skyway {
namespace network {

void WebSocketClientFactory::Setup(jobject j_ws) { WebSocketClientFactory::j_ws_ = j_ws; }

WebSocketClientFactory::WebSocketClientFactory() {
    factory_ = std::make_unique<skyway_android::network::WebSocketClientFactory>(
        WebSocketClientFactory::j_ws_);
}

std::shared_ptr<skyway::network::interface::WebSocketClient> WebSocketClientFactory::Create() {
    return WebSocketClientFactory::factory_->Create();
}

jobject WebSocketClientFactory::j_ws_ = nullptr;

}  // namespace network
}  // namespace skyway
