//
//  publication_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "publication_bridge.hpp"

#include <json.hpp>

#include "core/util/jstring_to_string.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/channel/publication/publication_event_listener.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {

using PublicationState = skyway::core::interface::PublicationState;


bool PublicationBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeMetadata",
            "(J)Ljava/lang/String;",
            (void*) PublicationBridge::Metadata
        },
        {
            "nativeAddEventListener",
            "(J)V",
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
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/Publication",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void PublicationBridge::AddEventListener(JNIEnv* env, jobject j_this, jlong publication) {
    auto publication_event_listener = new PublicationEventListener(j_this);
    ((Publication*)publication)->AddEventListener(publication_event_listener);
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

}  // namespace core
}  // namespace skyway_android
