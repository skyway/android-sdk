//
//  http_client.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <jni.h>

#include <string>
#include <future>

#include <boost/optional.hpp>
#include <json.hpp>

#include <skyway/network/interface/http_client.hpp>

#ifndef SKYWAY_ANDROID_NETWORK_HTTP_CLIENT_HPP
#define SKYWAY_ANDROID_NETWORK_HTTP_CLIENT_HPP

namespace skyway_android {
namespace network {

using Response = skyway::network::interface::HttpClient::Response;

class HttpClient : public skyway::network::interface::HttpClient {
public:
    static bool RegisterMethods(JNIEnv* env);
    static void OnResponse(JNIEnv* env, jobject j_this, jstring j_request_id, jint j_code, jstring j_body, jstring j_headers);
    static void OnFailure(JNIEnv* env, jobject j_this, jstring j_request_id);

    HttpClient(jobject j_http);
    ~HttpClient();

    std::future<boost::optional<Response>> Request(const std::string& url,
                                                      const std::string& method,
                                                      const nlohmann::json& header,
                                                      const nlohmann::json& body) override;
    static std::map<std::string, std::promise<boost::optional<Response>>> _promises;
private:
    jobject _j_http;
};

}  // namespace network
}  // namespace skyway_android

#endif /* SKYWAY_ANDROID_NETWORK_HTTP_CLIENT_HPP */
