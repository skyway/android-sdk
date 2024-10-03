//
//  platform_info_delegator.cpp
//  skyway_android
//
//  Copyright © 2024 NTT Communications. All rights reserved.
//

#include "platform_info_delegator.hpp"

namespace skyway {
namespace platform {

std::string PlatformInfoDelegator::GetPlatform() const {
    return "android";
}

std::string PlatformInfoDelegator::GetOsInfo() const {
    return "Android 10";
}

std::string PlatformInfoDelegator::GetModelName() const {
    return "Android Device";
}

std::string PlatformInfoDelegator::GetSdkVersion() const {
    return "2.1.3";
}

} // namespace platform
}  // namespace skyway
