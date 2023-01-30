//
//  register_methods_helper.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#ifndef SKYWAY_ANDROID_UTIL_REGISTER_METHODS_HELPER_HPP
#define SKYWAY_ANDROID_UTIL_REGISTER_METHODS_HELPER_HPP

#define ARRAY_LENGTH(array) (sizeof(array) / sizeof(array[0]))

namespace skyway_android {

bool RegisterMethodsHelper(JNIEnv *env, const char *class_name, JNINativeMethod *methods, int num_methods);

}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_UTIL_REGISTER_METHODS_HELPER_HPP */
