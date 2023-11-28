//
//  call_java_method.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <string>
#include <jni.h>
#include <skyway/global/interface/logger.hpp>

#ifndef SKYWAY_ANDROID_UTIL_CALL_JAVA_METHOD_HPP
#define SKYWAY_ANDROID_UTIL_CALL_JAVA_METHOD_HPP

void CallJavaMethod(JNIEnv* env, jobject obj, const std::string& method_name, const std::string& signature) {
    if (obj == nullptr) {
        SKW_WARN("obj is null when calling %s", method_name);
        return;
    }
    auto j_class = env->GetObjectClass(obj);
    auto j_method_id = env->GetMethodID(j_class, method_name.c_str(), signature.c_str());
    if (!j_method_id) {
        return;
    }
    env->CallVoidMethod(obj, j_method_id);
    env->DeleteLocalRef(j_class);
}

template <typename... Args>
void CallJavaMethod(JNIEnv* env, jobject obj, const std::string& method_name, const std::string& signature, Args&&... args) {
    if (obj == nullptr) {
        SKW_WARN("obj is null when calling %s", method_name);
        return;
    }
    auto j_class = env->GetObjectClass(obj);
    auto j_method_id = env->GetMethodID(j_class, method_name.c_str(), signature.c_str());
    if (!j_method_id) {
        return;
    }
    env->CallVoidMethod(obj, j_method_id, std::forward<Args>(args)...);
    env->DeleteLocalRef(j_class);
}


template <typename... Args>
void CallJavaStaticMethod(JNIEnv* env, jobject obj, const std::string& method_name, const std::string& signature, Args&&... args) {
    if (obj == nullptr) {
        SKW_WARN("obj is null when calling %s", method_name);
        return;
    }
    auto j_class = env->GetObjectClass(obj);
    auto j_method_id = env->GetStaticMethodID(j_class, method_name.c_str(), signature.c_str());
    if (!j_method_id) {
        return;
    }
    env->CallStaticVoidMethod(j_class, j_method_id, std::forward<Args>(args)...);
    env->DeleteLocalRef(j_class);
}

#endif /* SKYWAY_ANDROID_UTIL_CALL_JAVA_METHOD_HPP */
