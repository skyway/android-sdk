//
//  logger.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "logger.hpp"

#include <jni.h>

#include <skyway/global/interface/logger.hpp>

#include "android/log.h"

namespace skyway {
namespace global {

void Logger::Setup(jobject j_logger) { Logger::j_logger_ = j_logger; }

Logger::Logger(global::Logger::Level level) {
    logger_ = std::make_unique<skyway_android::logger_util::Logger>(Logger::j_logger_);
}

void Logger::Trace(const std::string &msg,
                   const std::string &filename,
                   const std::string &function,
                   int line) {
    logger_->Trace(msg, filename, function, line);
}

void Logger::Debug(const std::string &msg,
                   const std::string &filename,
                   const std::string &function,
                   int line) {
    logger_->Debug(msg, filename, function, line);
}

void Logger::Info(const std::string &msg,
                  const std::string &filename,
                  const std::string &function,
                  int line) {
    logger_->Info(msg, filename, function, line);
}

void Logger::Warn(const std::string &msg,
                  const std::string &filename,
                  const std::string &function,
                  int line) {
    logger_->Warn(msg, filename, function, line);
}

void Logger::Error(const std::string &msg,
                   const std::string &filename,
                   const std::string &function,
                   int line) {
    logger_->Error(msg, filename, function, line);
}

jobject Logger::j_logger_ = nullptr;

}  // namespace global
}  // namespace skyway
