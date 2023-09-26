//
//  http_client.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//
#include "http_client.hpp"

#include <jni.h>

#include <boost/optional.hpp>
#include <future>
#include <json.hpp>
#include <string>

namespace skyway {
namespace network {

using Response = skyway::network::interface::HttpClient::Response;

void HttpClient::Setup(jobject j_http) { HttpClient::j_http_ = j_http; }

HttpClient::HttpClient() {
    client_ = std::make_unique<skyway_android::network::HttpClient>(HttpClient::j_http_);
}

std::future<boost::optional<Response>> HttpClient::Request(const std::string &url,
                                                           const std::string &method,
                                                           const nlohmann::json &header,
                                                           const nlohmann::json &body) {
    return client_->Request(url, method, header, body);
}

jobject HttpClient::j_http_ = nullptr;

}  // namespace network
}  // namespace skyway
