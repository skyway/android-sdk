//
//  websocket_client.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/network/websocket_client.hpp"

#include <skyway/global/interface/logger.hpp>
#include <skyway/global/util.hpp>

#include "core/context/context_bridge.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/util/call_java_method.hpp"
#include "core/util/native_to_jlong.hpp"

namespace skyway_android {
namespace network {

bool WebSocketClient::RegisterMethods(JNIEnv *env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeOnConnect",
            "(J)V",
            (void *) WebSocketClient::OnConnect
        },
        {
            "nativeOnMessage",
            "(JLjava/lang/String;)V",
            (void *) WebSocketClient::OnMessage
        },
        {
            "nativeOnClose",
            "(JI)V",
            (void *) WebSocketClient::OnClose
        },
        {
            "nativeOnError",
            "(JI)V",
            (void *) WebSocketClient::OnError
        }
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/network/WebSocketClient",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void WebSocketClient::OnConnect(JNIEnv *env, jobject j_this, jlong j_ws) {
    auto ws = (WebSocketClient *) j_ws;
    ws->_OnConnect();
}

void WebSocketClient::OnMessage(JNIEnv *env, jobject j_this, jlong j_ws, jstring j_message) {
    auto ws = (WebSocketClient *) j_ws;
    auto message = JStringToStdString(env, j_message);
    ws->_OnMessage(message);
}

void WebSocketClient::OnClose(JNIEnv *env, jobject j_this, jlong j_ws, jint code) {
    auto ws = (WebSocketClient *) j_ws;
    ws->_OnClose(code);
}

void WebSocketClient::OnError(JNIEnv *env, jobject j_this, jlong j_ws, jint code) {
    auto ws = (WebSocketClient *) j_ws;
    ws->_OnError(code);
}

WebSocketClient::WebSocketClient(jobject j_ws): listener_(nullptr) {
    auto env = core::ContextBridge::AttachCurrentThread();
    j_ws_ = env->NewGlobalRef(j_ws);
}

WebSocketClient::~WebSocketClient() {
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(j_ws_);
}

void WebSocketClient::RegisterListener(Listener* listener) {
    listener_ = listener;
}

std::future<bool> WebSocketClient::Connect(const std::string& url,
                          const std::vector<std::string>& sub_protocols,
                          const std::unordered_map<std::string, std::string>& headers) {
    if (state_ != State::kNew && state_ != kClosed) {
        return this->GetFutureWithFalse();
    }
    state_ = State::kConnecting;
    connect_promise_ = std::promise<bool>();
    this->JavaConnect(url, sub_protocols, headers);
    return connect_promise_.get_future();
}

std::future<bool> WebSocketClient::Send(const std::string& message) {
    if (state_ != kConnected) {
        return this->GetFutureWithFalse();
    }
    this->JavaSend(message);
    return this->GetFutureWithTrue();
}

std::future<bool> WebSocketClient::Close(const int code, const std::string& reason) {
    if (state_ != State::kConnected) {
        return this->GetFutureWithTrue();
    }
    state_ = State::kClosing;
    close_promise_ = std::promise<bool>();
    this->JavaClose(code, reason);
    return close_promise_.get_future();
}

std::future<bool> WebSocketClient::Destroy() {
    {
        std::lock_guard<std::mutex> lg(clean_promise_mtx_);
        if (state_ == State::kConnecting) {
            connect_promise_.set_value(false);
        }
        if (state_ == State::kClosing) {
            close_promise_.set_value(false);
        }
        state_ = State::kDestroyed;
    }
    listener_ = nullptr;
    this->JavaClose(1000, "destroy");
    return this->GetFutureWithTrue();
}

void WebSocketClient::_OnConnect() {
    if (state_ != State::kConnecting) return;
    state_ = State::kConnected;
    connect_promise_.set_value(true);
}

void WebSocketClient::_OnMessage(const std::string& message ) {
    if (state_ != State::kConnected) return;

    if (!listener_) return;
    listener_->OnMessage(message);
}

void WebSocketClient::_OnClose(int code) {
    if (state_ != State::kClosing) return;
    state_ = State::kClosed;
    close_promise_.set_value(true);

    if (!listener_) return;
    listener_->OnClose(code);
}

void WebSocketClient::_OnError(int code) {
    {
        std::lock_guard<std::mutex> lg(clean_promise_mtx_);
        if (state_ == State::kConnecting) {
            connect_promise_.set_value(false);
        }
        if (state_ == State::kClosing) {
            close_promise_.set_value(false);
        }
        state_ = State::kClosed;
    }

    if (!listener_) return;
    listener_->OnError(code);
}

void WebSocketClient::JavaConnect(const std::string& url,
                 const std::vector<std::string>& sub_protocols,
                 const std::unordered_map<std::string, std::string>& headers) {
    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_url = env->NewStringUTF(url.c_str());
    auto j_sub_protocols = this->CreateJSubprotocols(env, sub_protocols);
    auto j_headers = this->CreateJHeaders(env, headers);
    auto signature = "(Ljava/lang/String;[Ljava/lang/String;[Lcom/ntt/skyway/core/network/WebSocketHeader;J)V";
    jlong ptr = NativeToJlong(this);
    CallJavaMethod(env, this->j_ws_, "connect", signature, j_url, j_sub_protocols, j_headers, ptr);
    env->DeleteLocalRef(j_sub_protocols);
    env->DeleteLocalRef(j_headers);
}

void WebSocketClient::JavaSend(const std::string& message) {
    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_message = env->NewStringUTF(message.c_str());
    CallJavaMethod(env, this->j_ws_, "send", "(Ljava/lang/String;)V", j_message);
    env->DeleteLocalRef(j_message);
}

void WebSocketClient::JavaClose(const int code, const std::string& reason) {
    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_class = env->GetObjectClass(this->j_ws_);
    auto j_method_id = env->GetMethodID(j_class, "close", "(ILjava/lang/String;)Z");
    auto j_reason = env->NewStringUTF(reason.c_str());
    env->CallBooleanMethod(this->j_ws_, j_method_id, code, j_reason);
    env->DeleteLocalRef(j_class);
    env->DeleteLocalRef(j_reason);
}

jobjectArray WebSocketClient::CreateJSubprotocols(JNIEnv* env, const std::vector<std::string>& sub_protocols) {
    auto j_str_class = env->FindClass("java/lang/String");
    auto j_sub_protocols = env->NewObjectArray(sub_protocols.size(), j_str_class, env->NewStringUTF(""));
    for (int i = 0; i < sub_protocols.size(); i++) {
        auto j_sub_protocol = env->NewStringUTF(sub_protocols[i].c_str());
        env->SetObjectArrayElement(j_sub_protocols, i, j_sub_protocol);
    }
    return j_sub_protocols;
}

jobjectArray WebSocketClient::CreateJHeaders(JNIEnv* env, const std::unordered_map<std::string, std::string>& headers){
    auto header_array_signature = "(I)[Lcom/ntt/skyway/core/network/WebSocketHeader;";
    auto j_ws_class = env->GetObjectClass(j_ws_);
    auto j_create_header_array = env->GetMethodID(j_ws_class, "createHeaderArray", header_array_signature);
    jobjectArray j_headers = (jobjectArray)(env->CallObjectMethod(j_ws_, j_create_header_array, headers.size()));
    auto header_signature = "(Ljava/lang/String;Ljava/lang/String;)Lcom/ntt/skyway/core/network/WebSocketHeader;";
    auto j_create_header = env->GetMethodID(j_ws_class, "createHeader", header_signature);
    int index = 0;
    for (auto itr = headers.begin(); itr != headers.end(); itr++) {
        auto j_header_key = env->NewStringUTF(itr->first.c_str());
        auto j_header_val = env->NewStringUTF(itr->second.c_str());
        auto j_header = env->CallObjectMethod(j_ws_, j_create_header, j_header_key, j_header_val);
        env->SetObjectArrayElement((jobjectArray)j_headers, index, j_header);
        index++;
    }
    return j_headers;
}

std::future<bool> WebSocketClient::GetFutureWithTrue() {
    std::promise<bool> p;
    p.set_value(true);
    return p.get_future();
}

std::future<bool> WebSocketClient::GetFutureWithFalse() {
    std::promise<bool> p;
    p.set_value(false);
    return p.get_future();
}

}  // namespace network
}  // namespace skyway_android
