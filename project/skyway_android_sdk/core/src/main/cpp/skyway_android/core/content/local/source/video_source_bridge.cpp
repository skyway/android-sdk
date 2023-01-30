//
//  video_source_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "video_source_bridge.hpp"

#include <skyway/core/stream/local/video_stream.hpp>
#include <skyway/global/interface/logger.hpp>

#include "core/content/content_util.hpp"
#include "core/util/register_methods_helper.cpp"
#include "core/util/jstring_to_string.hpp"

namespace skyway_android {
namespace content {
namespace local {
namespace source {

using LocalVideoStream = skyway::core::stream::local::LocalVideoStream;

bool VideoSource::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeCreateVideoStream",
            "(Lorg/webrtc/VideoTrack;)Ljava/lang/String;",
            (void*) VideoSource::CreateVideoStream
        }
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/content/local/source/VideoSource",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring
VideoSource::CreateVideoStream(JNIEnv* env, jobject j_this, jobject j_track) {
    auto video_track_ptr = VideoSource::GetVideoTrackPointer(env, j_track);
    auto local_video_stream = new LocalVideoStream(video_track_ptr);
    auto local_video_stream_json = util::getStreamDataJson(local_video_stream);
    return env->NewStringUTF(local_video_stream_json.dump().c_str());
}

rtc::scoped_refptr <VideoTrack> VideoSource::GetVideoTrackPointer(JNIEnv* env, jobject j_track) {
    auto j_class = env->GetObjectClass(j_track);
    auto method_id = env->GetMethodID(j_class, "getNativeVideoTrack", "()J");
    auto video_track_ptr = reinterpret_cast<VideoTrack*>(env->CallLongMethod(j_track, method_id));
    return rtc::scoped_refptr<VideoTrack>(video_track_ptr);
}

}  // namespace source
}  // namespace local
}  // namespace content
}  // namespace skyway_android
