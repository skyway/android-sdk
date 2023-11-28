//
//  websocket_client_factory.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <skyway/network/interface/websocket_client.hpp>

#include "core/network/websocket_client_factory.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace network {

WebSocketClientFactory::WebSocketClientFactory(jobject j_ws_factory) {
    auto env = core::ContextBridge::AttachCurrentThread();
    _j_ws_factory = env->NewGlobalRef(j_ws_factory);
}

WebSocketClientFactory::~WebSocketClientFactory() {
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_ws_factory);
}

std::shared_ptr<skyway::network::interface::WebSocketClient> WebSocketClientFactory::Create() {
    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_class = env->GetObjectClass(this->_j_ws_factory);
    auto j_method_id = env->GetStaticMethodID(j_class, "create", "()Lcom/ntt/skyway/core/network/WebSocketClient;");
    if (!j_method_id) {
        return nullptr;
    }
    auto j_ws = env->CallStaticObjectMethod(j_class, j_method_id);
    env->DeleteLocalRef(j_class);
    return std::make_shared<WebSocketClient>(j_ws);
}

}  // namespace network
}  // namespace skyway_android
