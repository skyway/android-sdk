//
//  remote_person_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/plugin/remote_person_plugin/remote_person.hpp>

#ifndef SKYWAY_ANDROID_CORE_REMOTE_PERON_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_REMOTE_PERON_BRIDGE_HPP

namespace skyway_android {
namespace plugin {
namespace remote_person {

using RemotePerson = skyway::plugin::remote_person::RemotePerson;

class RemotePersonBridge {
public:
    static bool RegisterMethods(JNIEnv* env);
    static jstring Subscribe(JNIEnv* env, jobject j_this, jlong remote_person, jstring j_publication_id);
    static bool Unsubscribe(JNIEnv* env, jobject j_this, jlong remote_person, jstring j_subscription_id);
};

}  // namespace remote_person
}  // namespace plugin
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_REMOTE_PERON_BRIDGE_HPP */
