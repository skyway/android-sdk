//
//  channel_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/core/channel/channel.hpp>
#include "core/channel/channel/channel_event_listener.hpp"

#ifndef SKYWAY_ANDROID_CORE_CHANNEL_CHANNEL_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_CHANNEL_CHANNEL_BRIDGE_HPP

namespace skyway_android {
namespace core {
namespace channel {

using Channel = skyway::core::channel::Channel;

class ChannelBridge {
public:
    static bool RegisterMethods(JNIEnv* env);

    static jstring Create(JNIEnv* env, jobject j_this, jstring j_channel_name, jstring j_channel_metadata);
    static jstring Find(JNIEnv* env, jobject j_this, jstring j_channel_name, jstring j_channel_id);
    static jstring FindOrCreate(JNIEnv* env, jobject j_this, jstring j_channel_name, jstring j_channel_metadata);

    static jstring Metadata(JNIEnv* env, jobject j_this, jlong channel);
    static jstring State(JNIEnv* env, jobject j_this, jlong channel);
    static void AddEventListener(JNIEnv* env, jobject j_this, jlong channel);
    static void AddInternalEventListener(const std::string& channel_id, EventListener* event_listener);
    static bool UpdateMetadata(JNIEnv* env, jobject j_this, jlong channel, jstring j_metadata);
    static jstring Join(JNIEnv* env, jobject j_this, jlong channel, jstring j_name, jstring j_metadata, jstring j_type, jstring j_subtype, jint keepalive_interval_sec);
    static bool Leave(JNIEnv* env, jobject j_this, jlong channel, jlong member);
    static bool Close(JNIEnv* env, jobject j_this, jlong channel);
    static void Dispose(JNIEnv* env, jobject j_this, jlong channel);

private:
    static std::map<std::string, std::vector<EventListener*>> event_listeners;
};

}  // namespace channel
}  // namespace core
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_CHANNEL_CHANNEL_BRIDGE_HPP */
