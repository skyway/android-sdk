//
//  sfu_bot_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "sfu_bot_bridge.hpp"

#include <json.hpp>

#include <skyway/plugin/sfu_bot_plugin/plugin.hpp>

#include "forwarding_bridge.hpp"
#include "core/channel/channel/channel_bridge.hpp"
#include "core/channel/channel_util.hpp"
#include "core/channel/publication/publication_bridge.hpp"
#include "core/channel/subscription/subscription_bridge.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/util/native_to_jlong.hpp"

namespace skyway_android {
namespace plugin {
namespace sfu_bot {

using ForwardingConfigure = skyway::plugin::sfu_bot::ForwardingConfigure;
using Channel = skyway::core::channel::Channel;
using Publication = skyway::core::interface::Publication;

bool SfuBotBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeCreateBot",
            "(J)Ljava/lang/String;",
            (void*) SfuBotBridge::CreateBot
        },
        {
            "nativeStartForwarding",
            "(JJLjava/lang/Integer;)Ljava/lang/String;",
            (void*) SfuBotBridge::StartForwarding
        },
        {
            "nativeStopForwarding",
            "(JJ)Z",
            (void*) SfuBotBridge::StopForwarding
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/plugin/sfuBot/SFUBot",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring SfuBotBridge::CreateBot(JNIEnv* env, jobject j_this, jlong channel) {
    auto plugin = skyway::core::Context::FindRemoteMemberPluginBySubtype(
        skyway::plugin::sfu_bot::config::SUBTYPE);
    auto sfu_bot_plugin = dynamic_cast<skyway::plugin::sfu_bot::Plugin*>(plugin);
    auto sfu_bot = sfu_bot_plugin->CreateBot((Channel*)channel);
    if(!sfu_bot) {
        return nullptr;
    }
    auto sfu_bot_json = core::util::ToJson(sfu_bot);
    return env->NewStringUTF(sfu_bot_json.dump().c_str());
}

jstring SfuBotBridge::StartForwarding(JNIEnv* env, jobject j_this, jlong sfu_bot, jlong publication, jobject j_max_subscribers) {
    ForwardingConfigure configure;
    if (j_max_subscribers) {
        jclass integer_class = env->FindClass("java/lang/Integer");
        auto max_subscribers = env->CallIntMethod(j_max_subscribers, env->GetMethodID(integer_class, "intValue", "()I"));
        configure.max_subscribers = max_subscribers;
    }
    auto forwarding = ((SfuBot*)sfu_bot)->StartForwarding((Publication*)publication, configure);
    if(!forwarding) {
        return nullptr;
    }
    auto forwarding_json = GetForwardingDataJson(forwarding);
    return env->NewStringUTF(forwarding_json.dump().c_str());
}

bool SfuBotBridge::StopForwarding(JNIEnv* env, jobject j_this, jlong sfu_bot, jlong forwarding) {
    return ((SfuBot*)sfu_bot)->StopForwarding((Forwarding*)forwarding);
}

// private

nlohmann::json SfuBotBridge::GetForwardingDataJson(Forwarding* forwarding_ptr) {
    nlohmann::json forwarding_json;
    forwarding_json["nativePointer"] = NativeToJlong(forwarding_ptr);
    forwarding_json["id"] = forwarding_ptr->Id();
    forwarding_json["configure"] = nlohmann::json::object();
    forwarding_json["configure"]["maxSubscribers"] = forwarding_ptr->Configure().max_subscribers;
    forwarding_json["originPublicationId"] = forwarding_ptr->OriginPublication()->Id();
    forwarding_json["relayingPublicationId"] = forwarding_ptr->RelayingPublication()->Id();
    return forwarding_json;
}

}  // namespace sfu_bot
}  // namespace plugin
}  // namespace skyway_android
