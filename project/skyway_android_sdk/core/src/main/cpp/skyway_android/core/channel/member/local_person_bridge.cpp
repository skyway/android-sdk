//
//  local_person_bridge.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "local_person_bridge.hpp"

#include <string>

#include <skyway/core/interface/publication.hpp>
#include <skyway/core/channel/channel.hpp>
#include <skyway/core/stream/local/video_stream.hpp>
#include <skyway/core/stream/remote/video_stream.hpp>

#include "local_person_event_listener.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/channel/channel/channel_bridge.hpp"
#include "core/channel/channel_util.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {
namespace member {

using PublicationOptions = skyway::core::channel::member::LocalPerson::PublicationOptions;
using SubscriptionOptions = skyway::core::channel::member::LocalPerson::SubscriptionOptions;

bool LocalPersonBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeAddEventListener",
            "(Ljava/lang/String;J)V",
            (void*) LocalPersonBridge::AddEventListener
        },
        {
            "nativeMetadata",
            "(J)Ljava/lang/String;",
            (void*) LocalPersonBridge::Metadata
        },
        {
            "nativeState",
            "(J)Ljava/lang/String;",
            (void*) LocalPersonBridge::State
        },
        {
            "nativeUpdateMetadata",
            "(JLjava/lang/String;)Z",
            (void*) LocalPersonBridge::UpdateMetadata
        },
        {
            "nativeLeave",
            "(J)Z",
            (void*) LocalPersonBridge::Leave
        },
        {
            "nativePublish",
            "(JJLjava/lang/String;)Ljava/lang/String;",
            (void*) LocalPersonBridge::Publish
        },
        {
            "nativeUnpublish",
            "(JLjava/lang/String;)Z",
            (void*) LocalPersonBridge::Unpublish
        },
        {
            "nativeSubscribe",
            "(JLjava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) LocalPersonBridge::Subscribe
        },
        {
            "nativeUnsubscribe",
            "(JLjava/lang/String;)Z",
            (void*) LocalPersonBridge::Unsubscribe
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/member/LocalPersonImpl",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void LocalPersonBridge::AddEventListener(JNIEnv* env, jobject j_this, jstring j_channel_id, jlong local_person) {
    auto local_person_event_listener = new LocalPersonEventListener(j_this);
    ((LocalPerson*)local_person)->AddEventListener(local_person_event_listener);
    auto channel_id = JStringToStdString(env, j_channel_id);
    channel::ChannelBridge::AddInternalEventListener(channel_id, local_person_event_listener);
}

jstring LocalPersonBridge::Metadata(JNIEnv* env, jobject j_this, jlong member) {
    auto metadata = ((LocalPerson*)member)->Metadata();
    return metadata ? env->NewStringUTF(metadata->c_str()) : env->NewStringUTF("");
}

jstring LocalPersonBridge::State(JNIEnv* env, jobject j_this, jlong member) {
    auto state = ((LocalPerson*)member)->State();
    switch (state) {
        case skyway::core::interface::MemberState::kJoined:
            return env->NewStringUTF("joined");
        case skyway::core::interface::MemberState::kLeft:
            return env->NewStringUTF("left");
    }
}

bool LocalPersonBridge::UpdateMetadata(JNIEnv* env, jobject j_this, jlong member, jstring j_metadata) {
    auto metadata = JStringToStdString(env, j_metadata);
    return ((LocalPerson*)member)->UpdateMetadata(metadata);
}

bool LocalPersonBridge::Leave(JNIEnv* env, jobject j_this, jlong member) {
    return ((LocalPerson*)member)->Leave();
}

jstring LocalPersonBridge::Publish(JNIEnv* env, jobject j_this, jlong local_person, jlong local_stream, jstring j_publication_options) {
    auto publication_options_str = JStringToStdString(env, j_publication_options);
    auto publication_options_json = nlohmann::json::parse(publication_options_str);

    PublicationOptions publication_options;

    if (publication_options_json.contains("metadata")) {
        publication_options.metadata = publication_options_json["metadata"];
    }

    if (publication_options_json.contains("codecCapabilities")) {
        for (const auto& codec_json: publication_options_json["codecCapabilities"]) {
            skyway::model::Codec codec;
            skyway::model::from_json(codec_json, codec);
            publication_options.codec_capabilities.emplace_back(codec);
        }
    }

    if (publication_options_json.contains("encodings")) {
        for (const auto& encoding_json: publication_options_json["encodings"]) {
            skyway::model::Encoding encoding;
            skyway::model::from_json(encoding_json, encoding);
            publication_options.encodings.emplace_back(encoding);
        }
    }

    if (publication_options_json.contains("isEnabled")) {
        publication_options.is_enabled = publication_options_json["isEnabled"];
    }

    std::shared_ptr<LocalStream> local_stream_shared((LocalStream*)local_stream);
    auto publication = ((LocalPerson*)local_person)->Publish(local_stream_shared, publication_options);
    if (!publication) {
        return nullptr;
    }

    auto publication_json = util::ToJson(publication);
    return env->NewStringUTF(publication_json.dump().c_str());
}

bool LocalPersonBridge::Unpublish(JNIEnv* env, jobject j_this, jlong local_person, jstring j_publication_id) {
    auto publication_id = JStringToStdString(env, j_publication_id);
    return ((LocalPerson*)local_person)->Unpublish(publication_id);
}

jstring LocalPersonBridge::Subscribe(JNIEnv* env, jobject j_this, jlong local_person, jstring j_publication_id, jstring j_subscription_options) {
    auto publication_id = JStringToStdString(env, j_publication_id);
    auto subscription_options_str = JStringToStdString(env, j_subscription_options);
    auto subscription_options_json = nlohmann::json::parse(subscription_options_str);

    SubscriptionOptions subscription_options;
    if(subscription_options_json.contains("isEnabled")) {
        subscription_options.is_enabled = subscription_options_json["isEnabled"];
    }
    if(subscription_options_json.contains("preferredEncodingId")) {
        subscription_options.preferred_encoding_id = subscription_options_json["preferredEncodingId"];
    }

    auto subscription = ((LocalPerson*)local_person)->Subscribe(publication_id, subscription_options);
    if (!subscription) {
        return nullptr;
    }

    auto subscription_json = util::ToJson(subscription);
    return env->NewStringUTF(subscription_json.dump().c_str());
}

bool LocalPersonBridge::Unsubscribe(JNIEnv* env, jobject j_this, jlong local_person, jstring j_subscription_id) {
    auto subscription_id = JStringToStdString(env, j_subscription_id);
    return ((LocalPerson*)local_person)->Unsubscribe(subscription_id);
}

}  // namespace member
}  // namespace core
}  // namespace skyway_android
