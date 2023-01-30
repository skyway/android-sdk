//
//  sfu_bot_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <skyway/plugin/sfu_bot_plugin/sfu_bot.hpp>

#ifndef SKYWAY_ANDROID_CORE_SFU_BOT_BRIDGE_HPP
#define SKYWAY_ANDROID_CORE_SFU_BOT_BRIDGE_HPP

namespace skyway_android {
namespace plugin {
namespace sfu_bot {

using SfuBot = skyway::plugin::sfu_bot::SfuBot;
using Forwarding = skyway::plugin::sfu_bot::Forwarding;

class SfuBotBridge {
public:
    static bool RegisterMethods(JNIEnv* env);
    static jstring CreateBot(JNIEnv* env, jobject j_this, jlong channel);
    static jstring StartForwarding(JNIEnv* env, jobject j_this, jlong sfu_bot, jlong publication, jobject j_max_subscribers);
    static bool StopForwarding(JNIEnv* env, jobject j_this,  jlong sfu_bot, jlong forwarding);

private:
    static nlohmann::json GetForwardingDataJson(Forwarding* forwarding);

};

}  // namespace sfu_bot
}  // namespace plugin
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_CORE_SFU_BOT_BRIDGE_HPP */
