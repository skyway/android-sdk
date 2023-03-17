//
//  channel_util.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <json.hpp>

#include <skyway/core/channel/channel.hpp>

#ifndef SKYWAY_ANDROID_CORE_CORE_UTIL_HPP
#define SKYWAY_ANDROID_CORE_CORE_UTIL_HPP

namespace skyway_android {
namespace core {
namespace util {

using Channel = skyway::core::channel::Channel;
using Member = skyway::core::interface::Member;
using Publication = skyway::core::interface::Publication;
using Subscription = skyway::core::interface::Subscription;
using ContextOptions = skyway::core::ContextOptions;

nlohmann::json getWebRTCStatsJson(skyway::model::WebRTCStats* webRtcStats_ptr);

nlohmann::json ToJson(Member* member_ptr);
nlohmann::json ToJson(Publication* publication_ptr);
nlohmann::json ToJson(Subscription* subscription_ptr);
nlohmann::json ToJson(Channel* channel_ptr);

}  // namespace util
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CORE_UTIL_HPP */
