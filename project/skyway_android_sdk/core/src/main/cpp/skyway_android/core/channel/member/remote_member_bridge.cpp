//
//  member_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "remote_member_bridge.hpp"

#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/context/context_bridge.hpp"
#include "core/channel/channel/channel_bridge.hpp"

namespace skyway_android {
namespace core {
namespace member {

bool RemoteMemberBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeAddEventListener",
            "(Ljava/lang/String;J)V",
            (void*) RemoteMemberBridge::AddEventListener
        },
        {
            "nativeMetadata",
            "(J)Ljava/lang/String;",
            (void*) RemoteMemberBridge::Metadata
        },
        {
            "nativeState",
            "(J)Ljava/lang/String;",
            (void*) RemoteMemberBridge::State
        },
        {
            "nativeUpdateMetadata",
            "(JLjava/lang/String;)Z",
            (void*) RemoteMemberBridge::UpdateMetadata
        },
        {
            "nativeLeave",
            "(J)Z",
            (void*) RemoteMemberBridge::Leave
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/member/RemoteMemberImpl",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void RemoteMemberBridge::AddEventListener(JNIEnv* env, jobject j_this, jstring j_channel_id, jlong member) {
    auto member_event_listener = new MemberEventListener(j_this);
    ((RemoteMember*)member)->AddEventListener(member_event_listener);
    auto channel_id = JStringToStdString(env, j_channel_id);
    channel::ChannelBridge::AddInternalEventListener(channel_id, member_event_listener);
}

jstring RemoteMemberBridge::Metadata(JNIEnv* env, jobject j_this, jlong member) {
    auto metadata = ((RemoteMember*)member)->Metadata();
    return env->NewStringUTF(metadata->c_str());
}

jstring RemoteMemberBridge::State(JNIEnv* env, jobject j_this, jlong member) {
    auto state = ((RemoteMember*)member)->State();
    switch (state) {
        case skyway::core::interface::MemberState::kJoined:
            return env->NewStringUTF("joined");
        case skyway::core::interface::MemberState::kLeft:
            return env->NewStringUTF("left");
    }
}

bool RemoteMemberBridge::UpdateMetadata(JNIEnv* env, jobject j_this, jlong member, jstring j_metadata) {
    auto metadata = JStringToStdString(env, j_metadata);
    return ((RemoteMember*)member)->UpdateMetadata(metadata);
}

bool RemoteMemberBridge::Leave(JNIEnv* env, jobject j_this, jlong member) {
    return ((RemoteMember*)member)->Leave();
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android

