//
//  publication_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "publication_bridge.hpp"

#include <json.hpp>

#include <skyway/core/interface/local_stream.hpp>

#include "core/util/jstring_to_string.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/channel/publication/publication_event_listener.hpp"
#include "core/context/context_bridge.hpp"
#include "core/channel/channel/channel_bridge.hpp"
#include "core/channel/channel_util.hpp"

namespace skyway_android {
namespace core {

using PublicationState = skyway::core::interface::PublicationState;
using LocalStream = skyway::core::interface::LocalStream;

bool PublicationBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeMetadata",
            "(J)Ljava/lang/String;",
            (void*) PublicationBridge::Metadata
        },
        {
            "nativeAddEventListener",
            "(Ljava/lang/String;J)V",
            (void*) PublicationBridge::AddEventListener
        },
        {
            "nativeState",
            "(J)Ljava/lang/String;",
            (void*) PublicationBridge::State
        },
        {
            "nativeEncodings",
            "(J)Ljava/lang/String;",
            (void*) PublicationBridge::Encodings
        },
        {
            "nativeUpdateMetadata",
            "(JLjava/lang/String;)Z",
            (void*) PublicationBridge::UpdateMetadata
        },
        {
            "nativeCancel",
            "(J)Z",
            (void*) PublicationBridge::Cancel
        },
        {
            "nativeEnable",
            "(J)Z",
            (void*) PublicationBridge::Enable
        },
        {
            "nativeDisable",
            "(J)Z",
            (void*) PublicationBridge::Disable
        },
        {
            "nativeUpdateEncodings",
            "(JLjava/lang/String;)V",
            (void*) PublicationBridge::UpdateEncodings
        },
        {
            "nativeReplaceStream",
            "(JJ)Z",
            (void*) PublicationBridge::ReplaceStream
        },
        {
            "nativeGetStats",
            "(Ljava/lang/String;J)Ljava/lang/String;",
            (void*) PublicationBridge::GetStats
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/PublicationImpl",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void PublicationBridge::AddEventListener(JNIEnv* env, jobject j_this, jstring j_channel_id, jlong publication) {
    auto publication_event_listener = new PublicationEventListener(j_this);
    auto publication_ptr = ((Publication*)publication);
    publication_ptr->AddEventListener(publication_event_listener);
    auto channel_id = JStringToStdString(env, j_channel_id);
    channel::ChannelBridge::AddInternalEventListener(channel_id, publication_event_listener);
}

jstring PublicationBridge::Metadata(JNIEnv* env, jobject j_this, jlong publication) {
    auto metadata = ((Publication*)publication)->Metadata();
    return metadata ? env->NewStringUTF(metadata->c_str()) : env->NewStringUTF("");
}

jstring PublicationBridge::State(JNIEnv* env, jobject j_this, jlong publication) {
    auto state = ((Publication*)publication)->State();
    switch (state) {
        case PublicationState::kEnabled:
            return env->NewStringUTF("enabled");
        case PublicationState::kDisabled:
            return env->NewStringUTF("disabled");
        case PublicationState::kCanceled:
            return env->NewStringUTF("canceled");
    }
}

jstring PublicationBridge::Encodings(JNIEnv* env, jobject j_this, jlong publication) {
    auto encodings = ((Publication*)publication)->Encodings();
    auto encodings_json = nlohmann::json::array();
    for (auto encoding : encodings)
    {
        nlohmann::json encoding_json;
        skyway::model::to_json(encoding_json, encoding);
        encodings_json.emplace_back(encoding_json);
    }
    return env->NewStringUTF(encodings_json.dump().c_str());
}

bool PublicationBridge::UpdateMetadata(JNIEnv* env, jobject j_this, jlong publication, jstring j_metadata) {
    auto metadata = JStringToStdString(env, j_metadata);
    return ((Publication*)publication)->UpdateMetadata(metadata);
}

bool PublicationBridge::Cancel(JNIEnv* env, jobject j_this, jlong publication) {
    return ((Publication*)publication)->Cancel();
}

bool PublicationBridge::Enable(JNIEnv* env, jobject j_this, jlong publication) {
    return ((Publication*)publication)->Enable();
}

bool PublicationBridge::Disable(JNIEnv* env, jobject j_this, jlong publication) {
    return ((Publication*)publication)->Disable();
}

void PublicationBridge::UpdateEncodings(JNIEnv* env, jobject j_this, jlong publication, jstring j_encodings) {
    auto encodings_str = JStringToStdString(env, j_encodings);
    auto encodings_json = nlohmann::json::parse(encodings_str);
    std::vector<Encoding> encodings;
    for (const auto& encoding_json: encodings_json) {
        skyway::model::Encoding encoding;
        skyway::model::from_json(encoding_json, encoding);
        encodings.emplace_back(encoding);
    }

    ((Publication*)publication)->UpdateEncodings(encodings);
}

bool PublicationBridge::ReplaceStream(JNIEnv* env, jobject j_this, jlong publication, jlong local_stream) {
    std::shared_ptr<LocalStream> local_stream_shared((LocalStream*)local_stream);
    return ((Publication*)publication)->ReplaceStream(local_stream_shared);
}

jstring PublicationBridge::GetStats(JNIEnv* env, jobject j_this, jstring j_remote_member_id, jlong publication) {
    auto remote_member_id = JStringToStdString(env, j_remote_member_id);
    auto stats = ((Publication*)publication)->GetStats(remote_member_id);
    if(!stats){
        return nullptr;
    }
    auto stats_json = util::getWebRTCStatsJson(&stats.get());
    return env->NewStringUTF(stats_json.dump().c_str());
}

}  // namespace core
}  // namespace skyway_android
