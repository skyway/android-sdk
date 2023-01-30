//
//  channel_util.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/channel/channel_util.hpp"

#include <json.hpp>

#include "core/content/content_util.hpp"

namespace skyway_android {
namespace core {
namespace util {

nlohmann::json ToJson(Member* member_ptr) {
    nlohmann::json member_json;
    member_json["nativePointer"] = (long) member_ptr;
    member_json["id"] = member_ptr->Id();
    member_json["name"] = member_ptr->Name().has_value() ? member_ptr->Name()->c_str() : "";
    member_json["subtype"] = member_ptr->Subtype();
    member_json["type"] = skyway::model::ToString(member_ptr->Type());
    member_json["side"] = skyway::model::ToString(member_ptr->Side());
    return member_json;
}

nlohmann::json ToJson(Publication* publication_ptr) {
    nlohmann::json publication_json;
    publication_json["nativePointer"] = (long) publication_ptr;
    publication_json["id"] = publication_ptr->Id();
    if (auto publisher = publication_ptr->Publisher()) {
        publication_json["publisherId"] = publisher->Id();
    }
    publication_json["contentType"] = skyway::model::ToString(publication_ptr->ContentType());
    publication_json["originId"] = publication_ptr->Origin() ? publication_ptr->Origin()->Id() : "";

    publication_json["codecCapabilities"] = nlohmann::json::array();
    for (const auto& codec: publication_ptr->CodecCapabilities()) {
        nlohmann::json codec_json;
        skyway::model::to_json(codec_json, codec);
        publication_json["codecCapabilities"].emplace_back(codec_json);
    }

    publication_json["encodings"] = nlohmann::json::array();
    for (const auto& encoding: publication_ptr->Encodings()) {
        nlohmann::json encoding_json;
        skyway::model::to_json(encoding_json, encoding);
        publication_json["encodings"].emplace_back(encoding_json);
    }

    return publication_json;
}

nlohmann::json ToJson(Subscription* subscription_ptr) {
    nlohmann::json subscription_json;
    subscription_json["nativePointer"] = (long) subscription_ptr;
    subscription_json["id"] = subscription_ptr->Id();

    if(auto publication = subscription_ptr->Publication()) {
        subscription_json["contentType"] = skyway::model::ToString(subscription_ptr->ContentType());
        subscription_json["publicationId"] = subscription_ptr->Publication()->Id();
    }

    if (auto subscriber = subscription_ptr->Subscriber()) {
        subscription_json["subscriberId"] = subscriber->Id();
    }

    if (subscription_ptr->State() != skyway::core::interface::SubscriptionState::kCanceled) {
        auto stream = subscription_ptr->Stream().get();
        if (stream) {
            subscription_json["stream"] = content::util::getStreamDataJson(stream);
        }
    }

    return subscription_json;
}

nlohmann::json ToJson(Channel* channel_ptr) {
    nlohmann::json channel_json;
    channel_json["nativePointer"] = (long) channel_ptr;
    channel_json["id"] = channel_ptr->Id();
    channel_json["name"] = channel_ptr->Name().has_value() ? channel_ptr->Name()->c_str() : "";

    channel_json["members"] = nlohmann::json::array();
    for (const auto& member: channel_ptr->Members()) {
        auto member_json = ToJson(member);
        channel_json["members"].emplace_back(member_json);
    }

    channel_json["publications"] = nlohmann::json::array();
    for (const auto &publication : channel_ptr->Publications()) {
        auto publication_json = ToJson(publication);
        channel_json["publications"].push_back(publication_json);
    }

    channel_json["subscriptions"] = nlohmann::json::array();
    for (const auto &subscription : channel_ptr->Subscriptions()) {
        auto subscription_json = ToJson(subscription);
        channel_json["subscriptions"].push_back(subscription_json);
    }

    return channel_json;
}

}  // namespace util
}  // namespace core
}  // namespace skyway_android
