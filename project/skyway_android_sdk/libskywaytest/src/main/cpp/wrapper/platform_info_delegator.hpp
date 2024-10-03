//
//  platform_info_delegator.hpp
//  skyway_android
//
//  Copyright © 2024 NTT Communications. All rights reserved.
//

#ifndef SKYWAY_ANDROID_CORE_PLATFORM_PLATFORM_INFO_DELEGATOR_WRAPPER_HPP
#define SKYWAY_ANDROID_CORE_PLATFORM_PLATFORM_INFO_DELEGATOR_WRAPPER_HPP

#include <skyway/platform/interface/platform_info_delegator.hpp>

namespace skyway {
namespace platform {

class PlatformInfoDelegator : public skyway::platform::interface::PlatformInfoDelegator{
public:
    std::string GetPlatform() const override;
    std::string GetOsInfo() const override;
    std::string GetModelName() const override;
    std::string GetSdkVersion() const override;
protected:
};

}  // namespace platform
}  // namespace skyway


#endif //SKYWAY_ANDROID_CORE_PLATFORM_PLATFORM_INFO_DELEGATOR_WRAPPER_HPP
