//
//  media_util.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/content/content_util.hpp"
#include <skyway/core/interface/local_stream.hpp>

namespace skyway_android {
namespace content {
namespace util {

nlohmann::json getStreamDataJson(Stream* stream_ptr) {
    nlohmann::json stream_json;
    stream_json["nativePointer"] = (long) stream_ptr;
    stream_json["id"] = stream_ptr->Id();
    stream_json["side"] = skyway::model::ToString(stream_ptr->Side());
    stream_json["contentType"] = skyway::model::ToString(stream_ptr->ContentType());
    return stream_json;
}

}  // namespace util
}  // namespace content
}  // namespace skyway_android
