//
//  member_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "member_bridge.hpp"

#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {
namespace member {

using MemberState = skyway::core::interface::MemberState;

bool MemberBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeAddEventListener",
            "(J)V",
            (void*) MemberBridge::AddEventListener
        },
        {
            "nativeMetadata",
            "(J)Ljava/lang/String;",
            (void*) MemberBridge::Metadata
        },
        {
            "nativeState",
            "(J)Ljava/lang/String;",
            (void*) MemberBridge::State
        },
        {
            "nativeUpdateMetadata",
            "(JLjava/lang/String;)Z",
            (void*) MemberBridge::UpdateMetadata
        },
        {
            "nativeLeave",
            "(J)Z",
            (void*) MemberBridge::Leave
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/member/Member",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void MemberBridge::AddEventListener(JNIEnv* env, jobject j_this, jlong member) {
    auto member_event_listener = new MemberEventListener(j_this);
    ((Member*)member)->AddEventListener(member_event_listener);
}

jstring MemberBridge::Metadata(JNIEnv* env, jobject j_this, jlong member) {
    auto metadata = ((Member*)member)->Metadata();
    return env->NewStringUTF(metadata->c_str());
}

jstring MemberBridge::State(JNIEnv* env, jobject j_this, jlong member) {
    auto state = ((Member*)member)->State();
    switch (state) {
        case MemberState::kJoined:
            return env->NewStringUTF("joined");
        case MemberState::kLeft:
            return env->NewStringUTF("left");
    }
}

bool MemberBridge::UpdateMetadata(JNIEnv* env, jobject j_this, jlong member, jstring j_metadata) {
    auto metadata = JStringToStdString(env, j_metadata);
    return ((Member*)member)->UpdateMetadata(metadata);
}

bool MemberBridge::Leave(JNIEnv* env, jobject j_this, jlong member) {
    return ((Member*)member)->Leave();
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android

