//
//  data_stream_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/content/remote/date_stream_listener.hpp"

#include "core/context/context_bridge.hpp"
#include "core/util/call_java_method.hpp"

namespace skyway_android {
namespace content {
namespace remote {

RemoteDataStreamListener::RemoteDataStreamListener(jobject j_remote_data_stream) {
    auto env = core::ContextBridge::AttachCurrentThread();
    _j_remote_data_stream = env->NewGlobalRef(j_remote_data_stream);
}

RemoteDataStreamListener::~RemoteDataStreamListener() {
    this->Dispose();
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_remote_data_stream);
}

void RemoteDataStreamListener::OnData(const std::string& data) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = core::ContextBridge::AttachCurrentThread();
        auto j_data = env->NewStringUTF(data.c_str());
        CallJavaMethod(env, _j_remote_data_stream, "onData", "(Ljava/lang/String;)V", j_data);
    });
    _threads.emplace_back(std::move(thread));
}

void RemoteDataStreamListener::OnDataBuffer(const uint8_t* data, size_t length) {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    if(_is_disposed) return;

    auto thread = std::make_unique<std::thread>([=] {
        auto env = core::ContextBridge::AttachCurrentThread();
        jbyteArray result = env->NewByteArray(length);
        env->SetByteArrayRegion( result, 0, length, (const jbyte*) data );
        CallJavaMethod(env, _j_remote_data_stream, "OnDataBuffer", "([B)V", result);
    });
    _threads.emplace_back(std::move(thread));
}

}  // namespace remote
}  // namespace content
}  // namespace channel
