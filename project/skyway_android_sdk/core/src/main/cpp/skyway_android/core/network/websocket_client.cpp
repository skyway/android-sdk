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

WebSocketClient::WebSocketClient(jobject j_ws): _listener(nullptr), _is_connecting(false), _is_closed(false), _is_closing(false) {
    auto env = core::ContextBridge::AttachCurrentThread();
    _j_ws = env->NewGlobalRef(j_ws);
}

WebSocketClient::~WebSocketClient() {
    std::lock_guard<std::mutex> lg(_workers_mtx);
    for (auto& w : _workers) {
        if (w && w->joinable()) {
            w->join();
        }
    }
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_ws);
}

void WebSocketClient::RegisterListener(Listener* listener) {
    _listener = listener;
}

std::future<bool> WebSocketClient::Connect(const std::string& url, const std::string& sub_protocol) {
    std::unique_lock<std::mutex> lk(_connect_mtx);
    _is_connecting = true;
    _is_closed = false;

    std::promise<bool> p;
    _connect_promise = std::move(p);

    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_url = env->NewStringUTF(url.c_str());
    auto j_sub_protocols = _CreateJSubprotocols(env, std::vector<std::string>{sub_protocol});
    auto j_headers = _CreateJHeaders(env, std::unordered_map<std::string, std::string>{});
    auto signature = "(Ljava/lang/String;[Ljava/lang/String;[Lcom/ntt/skyway/core/network/WebSocketHeader;J)V";
    jlong ptr = NativeToJlong(this);
    CallJavaMethod(env, this->_j_ws, "connect", signature, j_url, j_sub_protocols, j_headers, ptr);
    env->DeleteLocalRef(j_sub_protocols);
    env->DeleteLocalRef(j_headers);
    return _connect_promise.get_future();
}


std::future<bool> WebSocketClient::Connect(const std::string& url,
                          const std::vector<std::string>& sub_protocols,
                          const std::unordered_map<std::string, std::string>& headers) {
    std::unique_lock<std::mutex> lk(_connect_mtx);
    _is_connecting = true;
    _is_closed = false;

    std::promise<bool> p;
    _connect_promise = std::move(p);

    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_url = env->NewStringUTF(url.c_str());
    auto j_sub_protocols = _CreateJSubprotocols(env, sub_protocols);
    auto j_headers = _CreateJHeaders(env, headers);
    auto signature = "(Ljava/lang/String;[Ljava/lang/String;[Lcom/ntt/skyway/core/network/WebSocketHeader;J)V";
    jlong ptr = NativeToJlong(this);
    CallJavaMethod(env, this->_j_ws, "connect", signature, j_url, j_sub_protocols, j_headers, ptr);
    env->DeleteLocalRef(j_sub_protocols);
    env->DeleteLocalRef(j_headers);
    return _connect_promise.get_future();

}

std::future<bool> WebSocketClient::Send(const std::string& message) {
    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_message = env->NewStringUTF(message.c_str());
    CallJavaMethod(env, this->_j_ws, "send", "(Ljava/lang/String;)V", j_message);
    env->DeleteLocalRef(j_message);

    std::promise<bool> p;
    p.set_value(true);
    return p.get_future();
}

std::future<bool> WebSocketClient::Close(const int code, const std::string& reason) {
    std::unique_lock<std::mutex> lk(_close_mtx);
    _is_closing = true;

    std::promise<bool> p;
    _close_promise = std::move(p);

    if(_is_closed) {
        _close_promise.set_value(true);
        return _close_promise.get_future();
    }

    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_class = env->GetObjectClass(this->_j_ws);
    auto j_method_id = env->GetMethodID(j_class, "close", "(ILjava/lang/String;)Z");
    auto j_reason = env->NewStringUTF(reason.c_str());
    env->CallBooleanMethod(this->_j_ws, j_method_id, code, j_reason);
    env->DeleteLocalRef(j_class);
    env->DeleteLocalRef(j_reason);
    return _close_promise.get_future();
}

std::future<bool> WebSocketClient::Destroy() {
    std::promise<bool> p;
    auto result = this->Close(1000, "");

    if(!result.get()) {
        p.set_value(false);
        return p.get_future();
    }

    std::lock_guard<std::mutex> lg(_workers_mtx);
    for (auto& w : _workers) {
        if (w && w->joinable()) {
            w->join();
        }
    }
    _listener = nullptr;

    p.set_value(true);
    return p.get_future();
}

void WebSocketClient::_OnConnect() {
    std::unique_lock<std::mutex> lk(_connect_mtx);
    if (_is_connecting) {
        _connect_promise.set_value(true);
        _is_connecting = false;
    }
}

void WebSocketClient::_OnMessage(const std::string& message ) {
    std::lock_guard<std::mutex> lg(_workers_mtx);
    auto worker = std::make_unique<std::thread>([=]{
        if (!_listener) return;
        _listener->OnMessage(message);
    });
    _workers.emplace_back(std::move(worker));
}

void WebSocketClient::_OnClose(int code) {
    std::unique_lock<std::mutex> lk(_close_mtx);
    _is_closed = true;
    auto worker = std::make_unique<std::thread>([=]{
        if (!_listener) return;
        _listener->OnClose(code);
    });

    {
        std::lock_guard <std::mutex> lg(_workers_mtx);
        _workers.emplace_back(std::move(worker));
    }

    if (_is_closing) {
        _close_promise.set_value(true);
        _is_closing = false;
    }
}

void WebSocketClient::_OnError(int code) {
    if (_is_closing) {
        _close_promise.set_value(false);
        SKW_ERROR("Websocket error occurred when closing.");
    }
    _is_closed = true;
    if (_is_connecting) {
        _connect_promise.set_value(false);
        SKW_ERROR("Websocket error occurred when connecting.");
    }

    if (_is_closing || _is_connecting){
        _is_closing = false;
        _is_connecting = false;
        return;
    }

    std::lock_guard<std::mutex> lg(_workers_mtx);
    auto worker = std::make_unique<std::thread>([=]{
        if (!_listener) return;
        _listener->OnError(code);
    });
    _workers.emplace_back(std::move(worker));
}


jobjectArray WebSocketClient::_CreateJSubprotocols(JNIEnv* env, const std::vector<std::string>& sub_protocols) {
    auto j_str_class = env->FindClass("java/lang/String");
    auto j_sub_protocols = env->NewObjectArray(sub_protocols.size(), j_str_class, env->NewStringUTF(""));
    for (int i = 0; i < sub_protocols.size(); i++) {
        auto j_sub_protocol = env->NewStringUTF(sub_protocols[i].c_str());
        env->SetObjectArrayElement(j_sub_protocols, i, j_sub_protocol);
    }
    return j_sub_protocols;
}

jobjectArray WebSocketClient::_CreateJHeaders(JNIEnv* env, const std::unordered_map<std::string, std::string>& headers){
    auto header_array_signature = "(I)[Lcom/ntt/skyway/core/network/WebSocketHeader;";
    auto j_ws_class = env->GetObjectClass(_j_ws);
    auto j_create_header_array = env->GetMethodID(j_ws_class, "createHeaderArray", header_array_signature);
    jobjectArray j_headers = (jobjectArray)(env->CallObjectMethod(_j_ws, j_create_header_array, headers.size()));
    auto header_signature = "(Ljava/lang/String;Ljava/lang/String;)Lcom/ntt/skyway/core/network/WebSocketHeader;";
    auto j_create_header = env->GetMethodID(j_ws_class, "createHeader", header_signature);
    int index = 0;
    for (auto itr = headers.begin(); itr != headers.end(); itr++) {
        auto j_header_key = env->NewStringUTF(itr->first.c_str());
        auto j_header_val = env->NewStringUTF(itr->second.c_str());
        auto j_header = env->CallObjectMethod(_j_ws, j_create_header, j_header_key, j_header_val);
        env->SetObjectArrayElement((jobjectArray)j_headers, index, j_header);
        index++;
    }
    return j_headers;
}

}  // namespace network
}  // namespace skyway_android
