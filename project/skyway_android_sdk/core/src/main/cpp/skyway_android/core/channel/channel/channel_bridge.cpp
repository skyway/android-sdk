//
//  channel_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "channel_bridge.hpp"

#include <json.hpp>

#include <skyway/model/domain.hpp>

#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/channel/channel_util.hpp"
#include "core/context/context_bridge.hpp"

namespace skyway_android {
namespace core {
namespace channel {

using ChannelInit = skyway::model::Channel::Init;
using ChannelQuery = skyway::core::channel::ChannelQuery;
using ChannelState = skyway::core::interface::ChannelState;
using LocalPerson = skyway::core::channel::member::LocalPerson;
using MemberInit = skyway::model::Member::Init;
using Member = skyway::core::interface::Member;

std::map<std::string, std::vector<EventListener*>> ChannelBridge::event_listeners;
std::mutex ChannelBridge::event_listeners_mtx;


bool ChannelBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeCreate",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) ChannelBridge::Create
        },
        {
            "nativeFind",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) ChannelBridge::Find
        },
        {
            "nativeFindOrCreate",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) ChannelBridge::FindOrCreate
        },
        {
            "nativeMetadata",
            "(J)Ljava/lang/String;",
            (void*) ChannelBridge::Metadata
        },
        {
            "nativeState",
            "(J)Ljava/lang/String;",
            (void*) ChannelBridge::State
        },
        {
            "nativeAddEventListener",
            "(J)V",
            (void*) ChannelBridge::AddEventListener
        },
        {
            "nativeUpdateMetadata",
            "(JLjava/lang/String;)Z",
            (void*) ChannelBridge::UpdateMetadata
        },
        {
            "nativeJoin",
            "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;",
            (void*) ChannelBridge::Join
        },
        {
            "nativeLeave",
            "(JJ)Z",
            (void*) ChannelBridge::Leave
        },
        {
            "nativeClose",
            "(J)Z",
            (void*) ChannelBridge::Close
        },
        {
            "nativeDispose",
            "(J)V",
            (void*) ChannelBridge::Dispose
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/channel/ChannelImpl",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring ChannelBridge::Create(JNIEnv* env, jobject j_this, jstring j_channel_name, jstring j_channel_metadata) {
    ChannelInit channel_init;
    if(j_channel_name) {
        channel_init.name = JStringToStdString(env, j_channel_name);
    }
    if(j_channel_metadata) {
        channel_init.metadata = JStringToStdString(env, j_channel_metadata);
    }

    auto channel = Channel::Create(channel_init);

    if (!channel) {
        return nullptr;
    }

    auto channel_json = util::ToJson(channel.get());
    return env->NewStringUTF(channel_json.dump().c_str());
}

jstring ChannelBridge::Find(JNIEnv* env, jobject j_this, jstring j_channel_name, jstring j_channel_id) {
    ChannelQuery channel_query;
    if(j_channel_name) {
        channel_query.name = JStringToStdString(env, j_channel_name);
    }
    if(j_channel_id) {
        channel_query.id = JStringToStdString(env, j_channel_id);
    }
    auto channel = Channel::Find(channel_query);

    if (!channel) {
        return nullptr;
    }

    auto channel_json = util::ToJson(channel.get());
    return env->NewStringUTF(channel_json.dump().c_str());
}

jstring ChannelBridge::FindOrCreate(JNIEnv* env, jobject j_this, jstring j_channel_name, jstring j_channel_metadata) {
    ChannelInit channel_init;
    if(j_channel_name) {
        channel_init.name = JStringToStdString(env, j_channel_name);
    }
    if(j_channel_metadata) {
        channel_init.metadata = JStringToStdString(env, j_channel_metadata);
    }

    auto channel = Channel::FindOrCreate(channel_init);

    if (!channel) {
        return nullptr;
    }

    auto channel_json = util::ToJson(channel.get());
    return env->NewStringUTF(channel_json.dump().c_str());
}

jstring ChannelBridge::Metadata(JNIEnv* env, jobject j_this, jlong channel) {
    auto metadata = ((Channel*)channel)->Metadata();
    return metadata ? env->NewStringUTF(metadata->c_str()) : env->NewStringUTF("");
}

jstring ChannelBridge::State(JNIEnv* env, jobject j_this, jlong channel) {
    auto state = ((Channel*)channel)->State();
    switch (state) {
        case ChannelState::kOpened:
            return env->NewStringUTF("opened");
        case ChannelState::kClosed:
            return env->NewStringUTF("closed");
    }
}

void ChannelBridge::AddEventListener(JNIEnv* env, jobject j_this, jlong channel) {
    auto channel_event_listener = new ChannelEventListener(j_this);
    ((Channel*)channel)->AddEventListener(channel_event_listener);
    auto channel_id = ((Channel*)channel)->Id();
    std::lock_guard<std::mutex> lg(event_listeners_mtx);
    event_listeners[channel_id].emplace_back(channel_event_listener);
}

void ChannelBridge::AddInternalEventListener(const std::string& channel_id, EventListener* event_listener) {
    std::lock_guard<std::mutex> lg(event_listeners_mtx);
    event_listeners[channel_id].emplace_back(event_listener);
}

bool ChannelBridge::UpdateMetadata(JNIEnv* env, jobject j_this, jlong channel, jstring j_metadata) {
    auto metadata = JStringToStdString(env, j_metadata);
    return ((Channel*)channel)->UpdateMetadata(metadata);
}

jstring ChannelBridge::Join(JNIEnv* env, jobject j_this, jlong channel, jstring j_name, jstring j_metadata, jstring j_type, jstring j_subtype, jint keepalive_interval_sec) {
    MemberInit member_init;
    auto type = JStringToStdString(env, j_type);
    if (type == "PERSON") {
        member_init.type = skyway::model::MemberType::kPerson;
    } else {
        member_init.type = skyway::model::MemberType::kBot;
    }
    member_init.subtype = JStringToStdString(env, j_subtype);

    auto name = JStringToStdString(env, j_name);
    if(!name.empty()) {
        member_init.name = name;
    } else {
        member_init.name = boost::none;
    }

    auto metadata = JStringToStdString(env, j_metadata);
    if(!metadata.empty()) {
        member_init.metadata = metadata;
    } else {
        member_init.metadata = boost::none;
    }

    if(keepalive_interval_sec == 0) {
        member_init.keepalive_interval_sec = boost::none;
    } else {
        member_init.keepalive_interval_sec = keepalive_interval_sec;
    }

    auto local_person = ((Channel*)channel)->Join(member_init);
    if(local_person == nullptr){
        return nullptr;
    }
    auto member_json = util::ToJson(local_person);
    return env->NewStringUTF(member_json.dump().c_str());
}

bool ChannelBridge::Leave(JNIEnv* env, jobject j_this, jlong channel, jlong member) {
    return ((Channel*)channel)->Leave((Member*)member);
}

bool ChannelBridge::Close(JNIEnv* env, jobject j_this, jlong channel) {
    return ((Channel*)channel)->Close();
}

void ChannelBridge::Dispose(JNIEnv* env, jobject j_this, jlong channel) {
    auto channel_id = ((Channel *)channel)->Id();
    event_listeners[channel_id].clear();
    ((Channel *)channel)->Dispose(false);
}

}  // namespace channel
}  // namespace core
}  // namespace skyway_android
