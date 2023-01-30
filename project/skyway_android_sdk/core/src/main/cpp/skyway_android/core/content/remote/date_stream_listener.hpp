//
//  data_stream_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/stream/remote/data_stream.hpp>

#ifndef SKYWAY_ANDROID_CONTENT_REMOTE_DATA_STREAM_LISTENER_HPP
#define SKYWAY_ANDROID_CONTENT_REMOTE_DATA_STREAM_LISTENER_HPP

namespace skyway_android {
namespace content {
namespace remote {

class RemoteDataStreamListener : public skyway::core::stream::remote::RemoteDataStream::Listener {
public:
    RemoteDataStreamListener(jobject j_remote_data_stream);
    ~RemoteDataStreamListener();

    void OnData(const std::string& data);
    void OnDataBuffer(const uint8_t* data, size_t length);

private:
    jobject _j_remote_data_stream;

};

}  // namespace remote
}  // namespace content
}  // namespace channel

#endif /* SKYWAY_ANDROID_CONTENT_REMOTE_DATA_STREAM_LISTENER_HPP */
