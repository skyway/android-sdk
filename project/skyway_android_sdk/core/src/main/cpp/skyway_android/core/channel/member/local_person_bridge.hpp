//
//  local_person_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/channel/member/local_person.hpp>
#include <skyway/core/interface/local_stream.hpp>

#ifndef SKYWAY_ANDROID_CORE_MEMBER_LOCAL_PERSON_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_MEMBER_LOCAL_PERSON_BRIDGE_HPP

namespace skyway_android {
namespace core {
namespace member {

using LocalPerson = skyway::core::channel::member::LocalPerson;
using LocalStream = skyway::core::interface::LocalStream;

class LocalPersonBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static void AddEventListener(JNIEnv* env, jobject j_this, jstring j_channel_id, jlong local_person);
    static jstring Publish(JNIEnv* env, jobject j_this, jlong local_person, jlong local_stream, jstring j_publication_options);
    static bool Unpublish(JNIEnv* env, jobject j_this, jlong local_person, jstring publication_id);
    static jstring Subscribe(JNIEnv* env, jobject j_this, jlong local_person, jstring publication_id, jstring j_subscription_options);
    static bool Unsubscribe(JNIEnv* env, jobject j_this, jlong local_person, jstring subscription_id);

};

}  // namespace member
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_MEMBER_LOCAL_PERSON_BRIDGE_HPP */
