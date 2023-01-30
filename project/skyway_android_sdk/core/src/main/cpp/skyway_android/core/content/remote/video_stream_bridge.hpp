//
//  video_stream_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/stream/remote/video_stream.hpp>

#ifndef SKYWAY_ANDROID_CONTENT_REMOTE_VIDEO_STREAM_BRIDGE_HPP
#define SKYWAY_ANDROID_CONTENT_REMOTE_VIDEO_STREAM_BRIDGE_HPP

namespace skyway_android {
namespace content {
namespace remote {

using RemoteVideoStream = skyway::core::stream::remote::RemoteVideoStream;

class RemoteVideoStreamBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static long GetTrack(JNIEnv* env, jobject j_this, jlong remote_video_stream);

};

}  // namespace remote
}  // namespace content
}  // namespace channel

#endif /* SKYWAY_ANDROID_CONTENT_REMOTE_VIDEO_STREAM_BRIDGE_HPP */
