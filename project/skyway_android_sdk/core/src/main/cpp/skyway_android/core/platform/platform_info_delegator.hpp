//
//  platform_info_delegator.hpp
//  skyway_android
//
//  Copyright © 2024 NTT Communications. All rights reserved.
//

#ifndef SKYWAY_ANDROID_CORE_PLATFORM_PLATFORM_INFO_DELEGATOR_HPP
#define SKYWAY_ANDROID_CORE_PLATFORM_PLATFORM_INFO_DELEGATOR_HPP

#include "skyway/platform/interface/platform_info_delegator.hpp"

namespace skyway_android {
namespace core {
namespace platform {

class PlatformInfoDelegator : public skyway::platform::interface::PlatformInfoDelegator{
public:
    std::string GetPlatform() const override;

    std::string GetOsInfo() const override;

    std::string GetModelName() const override;
protected:
};

}  // namespace platform
}  // namespace core
}  // namespace skyway_android

#endif //SKYWAY_ANDROID_CORE_PLATFORM_PLATFORM_INFO_DELEGATOR_HPP
