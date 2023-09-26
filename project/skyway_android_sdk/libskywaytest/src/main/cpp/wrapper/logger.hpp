//
//  logger.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#ifndef SKYWAY_ANDROID_UTIL_LOGGER_WRAPPER_HPP
#define SKYWAY_ANDROID_UTIL_LOGGER_WRAPPER_HPP

#include <jni.h>

#include <skyway/global/interface/logger.hpp>

#include "android/log.h"

// Link to android-sdk...cpp/skyway_android/util
#include <core/util/logger.hpp>

namespace skyway {
namespace global {
using LoggerFactory = skyway_android::logger_util::Logger;

class Logger : public skyway::global::interface::Logger {
public:
    static void Setup(jobject j_logger);

    Logger(global::Logger::Level level);

    void Trace(const std::string &msg,
               const std::string &filename,
               const std::string &function,
               int line);

    void Debug(const std::string &msg,
               const std::string &filename,
               const std::string &function,
               int line);

    void Info(const std::string &msg,
              const std::string &filename,
              const std::string &function,
              int line);

    void Warn(const std::string &msg,
              const std::string &filename,
              const std::string &function,
              int line);

    void Error(const std::string &msg,
               const std::string &filename,
               const std::string &function,
               int line);

private:
    static jobject j_logger_;
    std::unique_ptr<skyway_android::logger_util::Logger> logger_;
};

}  // namespace global
}  // namespace skyway

#endif /* SKYWAY_ANDROID_UTIL_LOGGER_HPP */
