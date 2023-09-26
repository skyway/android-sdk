//
//  member_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/member.hpp>
#include "member_event_listener.hpp"

#ifndef SKYWAY_ANDROID_CORE_MEMBER_REMOTE_MEMBER_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_MEMBER_REMOTE_MEMBER_BRIDGE_HPP

namespace skyway_android {
namespace core {
namespace member {

using RemoteMember = skyway::core::interface::RemoteMember;

class RemoteMemberBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static void AddEventListener(JNIEnv* env, jobject j_this, jstring j_channel_id, jlong member);
    static jstring Metadata(JNIEnv* env, jobject j_this, jlong member);
    static jstring State(JNIEnv* env, jobject j_this, jlong member);
    static bool UpdateMetadata(JNIEnv* env, jobject j_this, jlong member, jstring j_metadata);
    static bool Leave(JNIEnv* env, jobject j_this, jlong member);
};

}  // namespace member
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_MEMBER_REMOTE_MEMBER_BRIDGE_HPP */
