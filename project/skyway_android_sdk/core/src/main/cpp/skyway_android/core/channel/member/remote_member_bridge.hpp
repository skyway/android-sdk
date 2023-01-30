//
//  remote_member_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/interface/remote_member.hpp>

#ifndef SKYWAY_ANDROID_CORE_MEMBER_REMOTE_MEMBER_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_MEMBER_REMOTE_MEMBER_BRIDGE_HPP

namespace skyway_android {
namespace core {
namespace member {

using RemoteMember = skyway::core::interface::RemoteMember;

class RemoteMemberBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jstring getStatsOfPublication(JNIEnv* env, jobject j_this, jlong remote_member, jlong publication);
    static jstring getStatsOfSubscription(JNIEnv* env, jobject j_this, jlong remote_member, jlong subscription);
};

}  // namespace member
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_MEMBER_REMOTE_MEMBER_BRIDGE_HPP */
