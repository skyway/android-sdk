//
//  context_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "context_bridge.hpp"

#include <skyway/core/context.hpp>
#include <skyway/plugin/remote_person_plugin/plugin.hpp>
#include <skyway/plugin/sfu_bot_plugin/plugin.hpp>
#include <skyway/plugin/sfu_bot_plugin/sfu_options.hpp>

#include "core/network/http_client.hpp"
#include "core/network/websocket_client_factory.hpp"
#include "core/util/logger.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/channel/channel_util.hpp"

namespace skyway_android {
namespace core {

using ContextOptions = skyway::core::ContextOptions;
using SfuOptions = skyway::plugin::sfu_options::SfuOptionsParams;
using Context = skyway::core::Context;
using RemotePersonPlugin = skyway::plugin::remote_person::Plugin;
using SfuBotPlugin = skyway::plugin::sfu_bot::Plugin;
using PeerConnectionFactoryInterface = webrtc::PeerConnectionFactoryInterface;

JavaVM* ContextBridge::jvm;
std::vector<EventListener*> ContextBridge::event_listeners;

bool ContextBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeSetup",
            "(Ljava/lang/String;Ljava/lang/String;JLcom/ntt/skyway/core/network/HttpClient;Lcom/ntt/skyway/core/network/WebSocketClientFactory;Lcom/ntt/skyway/core/util/Logger;)Z",
            (void*) ContextBridge::Setup
        },
        {
            "nativeUpdateAuthToken",
            "(Ljava/lang/String;)Z",
            (void*) ContextBridge::UpdateAuthToken
        },
        {
            "nativeUpdateRtcConfig",
            "(Ljava/lang/String;)V",
            (void*) ContextBridge::_UpdateRtcConfig
        },
        {
            "nativeDispose",
            "()V",
            (void*) ContextBridge::Dispose
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/SkyWayContext",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jboolean ContextBridge::Setup(JNIEnv* env, jobject j_this, jstring j_auth_token, jstring j_options,
                              jlong j_pc_factory, jobject j_http, jobject j_ws_factory, jobject j_logger) {
    SetJavaVMFromEnv(env);

    auto peer_connection_factory = (PeerConnectionFactoryInterface*) j_pc_factory;
    auto peer_connection_factory_ptr = rtc::scoped_refptr <PeerConnectionFactoryInterface>(peer_connection_factory);

    // Setup Context
    auto auth_token = JStringToStdString(env, j_auth_token);

    auto http =       std::make_unique<network::HttpClient>(j_http);
    auto http_ptr = http.get();

    auto ws_factory = std::make_unique<network::WebSocketClientFactory>(j_ws_factory);
    auto logger = std::make_unique<logger_util::Logger>(j_logger);
    auto listener = new ContextEventListener(j_this);
    auto auth_token_manager_listener = new AuthTokenManagerEventListener(j_this);

    auto options_string = JStringToStdString(env, j_options);
    auto options_json = nlohmann::json::parse(options_string);

    ContextOptions options = {};
    ApplyContextOptions(options, options_json);
    options.token.listener = auth_token_manager_listener;

    auto setup_result = Context::Setup(auth_token, std::move(http), std::move(ws_factory), std::move(logger), listener, options);

    if (!setup_result) {
        return false;
    }

    event_listeners.emplace_back(listener);
    event_listeners.emplace_back(auth_token_manager_listener);

    // Register remote_person_plugin
    auto remote_person_plugin = std::make_unique<RemotePersonPlugin>(peer_connection_factory_ptr);
    Context::RegisterPlugin(std::move(remote_person_plugin));

    // Register sfu_bot_plugin
    SfuOptions sfu_options = {};
    if(options_json.contains("sfu")) {
        auto sfu_api = options_json["sfu"];
        if(sfu_api.contains("domain")) {
            sfu_options.domain = sfu_api["domain"];
        }
        if(sfu_api.contains("version")) {
            sfu_options.version = sfu_api["version"];
        }
        if(sfu_api.contains("secure")) {
            sfu_options.secure = sfu_api["secure"];
        }

    }
    auto sfu_bot_plugin = std::make_unique<SfuBotPlugin>(http_ptr, peer_connection_factory_ptr, sfu_options);
    Context::RegisterPlugin(std::move(sfu_bot_plugin));

    return true;
}

jboolean ContextBridge::UpdateAuthToken(JNIEnv* env, jobject j_this, jstring j_auth_token) {
    auto auth_token = JStringToStdString(env, j_auth_token);
    return Context::UpdateAuthToken(auth_token);
}

void ContextBridge::_UpdateRtcConfig(JNIEnv* env, jobject j_this, jstring j_rtc_config) {
    auto rtc_config_string = JStringToStdString(env, j_rtc_config);
    auto rtc_config_json = nlohmann::json::parse(rtc_config_string);

    ContextOptions::RtcConfig rtc_config;
    if(rtc_config_json.contains("timeout")) {
        rtc_config.timeout = rtc_config_json["timeout"];
    }
    if(rtc_config_json.contains("policy")) {
        if(rtc_config_json["policy"] == "ENABLE") {
            rtc_config.policy = skyway::core::TurnPolicy::kEnable;
        } else if(rtc_config_json["policy"] == "DISABLE") {
            rtc_config.policy = skyway::core::TurnPolicy::kDisable;
        } else if(rtc_config_json["policy"] == "TURN_ONLY") {
            rtc_config.policy = skyway::core::TurnPolicy::kTurnOnly;
        }
    }
    Context::_UpdateRtcConfig(rtc_config);
}

void ContextBridge::SetJavaVMFromEnv(JNIEnv* env) {
    env->GetJavaVM(&jvm);
}

JNIEnv* ContextBridge::AttachCurrentThread() {
    JNIEnv* env = nullptr;
    jvm->AttachCurrentThread(&env, NULL);
    return env;
}

void ContextBridge::DetachCurrentThread() {
    jvm->DetachCurrentThread();
}

void ContextBridge::Dispose(JNIEnv* env, jobject j_this) {
    for(const auto event_listener : event_listeners) {
        event_listener->Dispose();
    }
    Context::Dispose();
}

void ContextBridge::ApplyContextOptions(ContextOptions& options, nlohmann::json& options_json) {
    if(options_json.contains("rtcApi")) {
        auto rtc_api = options_json["rtcApi"];
        if(rtc_api.contains("domain")) {
            options.rtc_api.domain = rtc_api["domain"];
        }
        if(rtc_api.contains("secure")) {
            options.rtc_api.secure = rtc_api["secure"];
        }
    }
    if(options_json.contains("iceParams")) {
        auto ice_params = options_json["iceParams"];
        if(ice_params.contains("domain")) {
            options.ice_params.domain = ice_params["domain"];
        }
        if(ice_params.contains("version")) {
            options.ice_params.version = ice_params["version"];
        }
        if(ice_params.contains("secure")) {
            options.ice_params.secure = ice_params["secure"];
        }
    }
    if(options_json.contains("signaling")) {
        auto signaling = options_json["signaling"];
        if(signaling.contains("domain")) {
            options.signaling.domain = signaling["domain"];
        }
        if(signaling.contains("secure")) {
            options.signaling.secure = signaling["secure"];
        }
    }
    if(options_json.contains("rtcConfig")) {
        auto rtc_config = options_json["rtcConfig"];
        if(rtc_config.contains("timeout")) {
            options.rtc_config.timeout = rtc_config["timeout"];
        }
        if(rtc_config.contains("policy")) {
            if(rtc_config["policy"] == "ENABLE") {
                options.rtc_config.policy = skyway::core::TurnPolicy::kEnable;
            } else if(rtc_config["policy"] == "DISABLE") {
                options.rtc_config.policy = skyway::core::TurnPolicy::kDisable;
            } else if(rtc_config["policy"] == "TURN_ONLY") {
                options.rtc_config.policy = skyway::core::TurnPolicy::kTurnOnly;
            }
        }
    }
    if(options_json.contains("token")) {
        auto token = options_json["token"];
        if(token.contains("tokenReminderTimeSec")) {
            options.token.remind_time_sec = token["tokenReminderTimeSec"];
        }

    }
}

}  // namespace core
}  // namespace skyway_android
