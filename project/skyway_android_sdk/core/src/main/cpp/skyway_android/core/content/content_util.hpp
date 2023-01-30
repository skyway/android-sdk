//
//  media_util.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <json.hpp>

#include <skyway/core/interface/stream.hpp>

#ifndef SKYWAY_ANDROID_CONTENT_VIDEO_SOURCE_BRIDGE_HPP
#define SKYWAY_ANDROID_CONTENT_VIDEO_SOURCE_BRIDGE_HPP


namespace skyway_android {
namespace content {
namespace util {

using Stream = skyway::core::interface::Stream;

nlohmann::json getStreamDataJson(Stream* stream_ptr);

}  // namespace util
}  // namespace content
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CONTENT_VIDEO_SOURCE_BRIDGE_HPP */
