//
//  logger.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/global/interface/logger.hpp>

#ifndef SKYWAY_ANDROID_UTIL_LOGGER_HPP
#define SKYWAY_ANDROID_UTIL_LOGGER_HPP

namespace skyway_android {
namespace logger_util {

class Logger : public skyway::global::interface::Logger {
public:
    Logger(jobject j_logger);
    ~Logger();

    void Trace(const std::string& msg, const std::string& filename, const std::string& function, int line);
    void Debug(const std::string& msg, const std::string& filename, const std::string& function, int line);
    void Info(const std::string& msg, const std::string& filename, const std::string& function, int line);
    void Warn(const std::string& msg, const std::string& filename, const std::string& function, int line);
    void Error(const std::string& msg, const std::string& filename, const std::string& function, int line);

private:
    void Log(int logLevel, const std::string& msg, const std::string& filename, const std::string& function, int line);
    jobject _j_logger;
};

}  // namespace util
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_UTIL_LOGGER_HPP */
