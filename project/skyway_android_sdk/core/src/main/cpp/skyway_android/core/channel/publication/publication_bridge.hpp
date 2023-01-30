//
//  publication_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/publication.hpp>
#include <skyway/core/channel/channel.hpp>

#ifndef SKYWAY_ANDROID_CORE_PUBLICATION_PUBLICATION_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_PUBLICATION_PUBLICATION_BRIDGE_HPP

namespace skyway_android {
namespace core {

using Publication = skyway::core::interface::Publication;
using Encoding = skyway::model::Encoding;

class PublicationBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jstring Metadata(JNIEnv* env, jobject j_this, jlong publication);
    static jstring State(JNIEnv* env, jobject j_this, jlong publication);
    static jstring Encodings(JNIEnv* env, jobject j_this, jlong publication);

    static void AddEventListener(JNIEnv* env, jobject j_this, jlong publication);
    static bool UpdateMetadata(JNIEnv* env, jobject j_this, jlong publication, jstring j_metadata);
    static bool Cancel(JNIEnv* env, jobject j_this, jlong publication);
    static bool Enable(JNIEnv* env, jobject j_this, jlong publication);
    static bool Disable(JNIEnv* env, jobject j_this, jlong publication);
    static void UpdateEncodings(JNIEnv* env, jobject j_this, jlong publication, jstring j_encodings);
};

}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_PUBLICATION_PUBLICATION_BRIDGE_HPP */
