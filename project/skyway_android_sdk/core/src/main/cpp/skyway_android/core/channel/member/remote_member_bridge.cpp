//
//  remote_member_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "remote_member_bridge.hpp"

#include "core/channel/publication/publication_bridge.hpp"
#include "core/channel/subscription/subscription_bridge.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"

namespace skyway_android {
namespace core {
namespace member {

bool RemoteMemberBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeGetStatsOfPublication",
            "(JJ)Ljava/lang/String;",
            (void*) RemoteMemberBridge::getStatsOfPublication
        },
        {
            "nativeGetStatsOfSubscription",
            "(JJ)Ljava/lang/String;",
            (void*) RemoteMemberBridge::getStatsOfSubscription
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/member/RemoteMember",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring RemoteMemberBridge::getStatsOfPublication(JNIEnv* env, jobject j_this, jlong remote_member, jlong publication) {
    auto stats = ((RemoteMember*)remote_member)->GetStats((Publication*)publication);

    nlohmann::json stats_json;
    if(stats) {
        if(stats.get().inbound) {
            stats_json["InboundRtp"] = nlohmann::json::object();
            stats_json["InboundRtp"]["bytes_received"] = std::to_string(stats.get().inbound.get().bytes_received);
        }

        if(stats.get().outbound) {
            stats_json["OutboundRtp"] = nlohmann::json::object();
            stats_json["OutboundRtp"]["bytes_sent"] = std::to_string(stats.get().outbound.get().bytes_sent);
        }
    }

    return env->NewStringUTF(stats_json.dump().c_str());
}

jstring RemoteMemberBridge::getStatsOfSubscription(JNIEnv* env, jobject j_this, jlong remote_member, jlong subscription) {
    auto stats = ((RemoteMember*)remote_member)->GetStats((Subscription*)subscription);

    nlohmann::json stats_json;
    if(stats) {
        if(stats.get().inbound) {
            stats_json["InboundRtp"] = nlohmann::json::object();
            stats_json["InboundRtp"]["bytes_received"] = std::to_string(stats.get().inbound.get().bytes_received);
        }

        if(stats.get().outbound) {
            stats_json["OutboundRtp"] = nlohmann::json::object();
            stats_json["OutboundRtp"]["bytes_sent"] = std::to_string(stats.get().outbound.get().bytes_sent);
        }
    }

    return env->NewStringUTF(stats_json.dump().c_str());
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
