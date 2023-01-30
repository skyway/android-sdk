//
//  logger.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/util/logger.hpp"

#include "core/context/context_bridge.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/util/call_java_method.hpp"

namespace skyway_android {
namespace logger_util {

Logger::Logger(jobject j_logger) {
    auto env = core::ContextBridge::GetEnv();
    _j_logger = env->NewGlobalRef(j_logger);
}

Logger::~Logger() {
    auto env = core::ContextBridge::GetEnv();
    env->DeleteGlobalRef(_j_logger);
}

void Logger::Trace(const std::string& msg, const std::string& filename, const std::string& function, int line) {
    this->Log(5, msg, filename, function, line);
}

void Logger::Debug(const std::string& msg, const std::string& filename, const std::string& function, int line) {
    this->Log(4, msg, filename, function, line);
}

void Logger::Info(const std::string& msg, const std::string& filename, const std::string& function, int line) {
    this->Log(3, msg, filename, function, line);
}

void Logger::Warn(const std::string& msg, const std::string& filename, const std::string& function, int line) {
    this->Log(2, msg, filename, function, line);
}

void Logger::Error(const std::string& msg, const std::string& filename, const std::string& function, int line) {
    this->Log(1, msg, filename, function, line);
}

void Logger::Log(int logLevel, const std::string& msg, const std::string& filename, const std::string& function, int line) {
    auto env = core::ContextBridge::GetEnv();
    auto j_msg = env->NewStringUTF(msg.c_str());
    auto j_filename = env->NewStringUTF(filename.c_str());
    auto j_function = env->NewStringUTF(function.c_str());
    CallJavaMethod(env,
                   this->_j_logger,
                   "log",
                   "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V",
                   logLevel,
                   j_msg,
                   j_filename,
                   j_function,
                   line);
}

}  // namespace util
}  // namespace skyway_android
