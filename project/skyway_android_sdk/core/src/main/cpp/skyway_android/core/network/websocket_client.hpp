//
//  websocket_client.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <string>
#include <future>
#include <thread>
#include <vector>
#include <unordered_map>

#include <skyway/network/interface/websocket_client.hpp>

#ifndef SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_HPP
#define SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_HPP

namespace skyway_android {
namespace network {

using Listener = skyway::network::interface::WebSocketClient::Listener;

class WebSocketClient : public skyway::network::interface::WebSocketClient {
public:
    static bool RegisterMethods(JNIEnv* env);
    static void OnConnect(JNIEnv *env, jobject j_this, jlong j_ws);
    static void OnMessage(JNIEnv* env, jobject j_this, jlong j_ws, jstring j_message);
    static void OnClose(JNIEnv *env, jobject j_this, jlong j_ws, jint code);
    static void OnError(JNIEnv *env, jobject j_this, jlong j_ws, jint code);

    WebSocketClient(jobject j_ws);
    ~WebSocketClient();

    void RegisterListener(Listener* listener) override;

    std::future<bool> Connect(const std::string& url,
                              const std::vector<std::string>& sub_protocols,
                              const std::unordered_map<std::string, std::string>& headers) override;
    std::future<bool> Send(const std::string& message) override;
    std::future<bool> Close(const int code, const std::string& reason) override;
    std::future<bool> Destroy() override;

    void _OnConnect();
    void _OnMessage(const std::string& message);
    void _OnClose(int code);
    void _OnError(int code);

    Listener* _listener;

private:
    jobjectArray _CreateJSubprotocols(JNIEnv* env, const std::vector<std::string>& sub_protocols);
    jobjectArray _CreateJHeaders(JNIEnv* env, const std::unordered_map<std::string, std::string>& headers);
    jobject _j_ws;
    bool _is_connecting;
    std::promise<bool> _connect_promise;
    std::mutex _connect_mtx;
    bool _is_closing;
    bool _is_closed;
    std::promise<bool> _close_promise;
    std::mutex _close_mtx;
    std::atomic<bool> _is_destroyed = false;
};

}  // namespace network
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_HPP */
