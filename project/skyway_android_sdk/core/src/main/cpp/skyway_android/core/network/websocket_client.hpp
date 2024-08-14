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
    enum State { kNew, kConnecting, kConnected, kClosing, kClosed, kDestroyed };

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

private:
    void JavaConnect(const std::string& url,
                     const std::vector<std::string>& sub_protocols,
                     const std::unordered_map<std::string, std::string>& headers);
    void JavaSend(const std::string& message);
    void JavaClose(const int code, const std::string& reason);
    jobjectArray CreateJSubprotocols(JNIEnv* env, const std::vector<std::string>& sub_protocols);
    jobjectArray CreateJHeaders(JNIEnv* env, const std::unordered_map<std::string, std::string>& headers);
    std::future<bool> GetFutureWithTrue();
    std::future<bool> GetFutureWithFalse();

    jobject j_ws_;
    std::mutex clean_promise_mtx_;
    std::promise<bool> connect_promise_;
    std::promise<bool> close_promise_;
    std::atomic<State> state_ = State::kNew;
    Listener* listener_;
};

}  // namespace network
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_NETWORK_WEBSOCKET_CLIENT_HPP */
