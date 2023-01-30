//
//  register_methods_helper.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "register_methods_helper.hpp"

namespace skyway_android {

bool RegisterMethodsHelper(JNIEnv *env, const char *class_name, JNINativeMethod *methods, int num_methods) {
    jclass clazz = env->FindClass(class_name);
    if (!clazz) {
        return false;
    }

    int result = env->RegisterNatives(clazz, methods, num_methods);
    if (result < 0) {
        return false;
    }

    return true;
}

}  // namespace skyway_android
