//
//  jstring_to_string.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "jstring_to_string.hpp"

std::string JStringToStdString(JNIEnv* env, jstring jstr) {
    if (!jstr)
        return "";

    const jclass stringClass = env->GetObjectClass(jstr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jstr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);

    return ret;
}
