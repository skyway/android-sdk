//
//  jstring_to_string.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <string>
#include <jni.h>

#ifndef SKYWAY_ANDROID_UTIL_JSTRING_TO_STRING_HPP
#define SKYWAY_ANDROID_UTIL_JSTRING_TO_STRING_HPP

std::string JStringToStdString(JNIEnv* env, jstring jstr);

#endif /* SKYWAY_ANDROID_UTIL_JSTRING_TO_STRING_HPP */
