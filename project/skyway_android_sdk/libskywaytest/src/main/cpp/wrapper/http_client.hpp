//
//  http_client.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#ifndef SKYWAY_ANDROID_NETWORK_HTTP_CLIENT_WRAPPER_HPP
#define SKYWAY_ANDROID_NETWORK_HTTP_CLIENT_WRAPPER_HPP

#include <jni.h>

#include <boost/optional.hpp>
#include <future>
#include <json.hpp>
#include <string>

// Link to android-sdk...cpp/skyway_android/network
#include <core/network/http_client.hpp>

namespace skyway {
namespace network {

using Response = skyway::network::interface::HttpClient::Response;

class HttpClient : public skyway::network::interface::HttpClient {
public:
    static void Setup(jobject j_http);
    HttpClient();
    std::future<boost::optional<Response>> Request(const std::string &url,
                                                   const std::string &method,
                                                   const nlohmann::json &header,
                                                   const nlohmann::json &body);

private:
    static jobject j_http_;
    std::unique_ptr<skyway_android::network::HttpClient> client_;
};

}  // namespace network
}  // namespace skyway

#endif /* SKYWAY_ANDROID_NETWORK_HTTP_CLIENT_WRAPPER_HPP */
