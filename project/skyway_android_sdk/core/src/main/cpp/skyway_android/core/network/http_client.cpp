//
//  http_client.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "core/network/http_client.hpp"

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/lexical_cast.hpp>

#include "core/context/context_bridge.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"
#include "core/util/call_java_method.hpp"

using namespace boost::uuids;

namespace skyway_android {
namespace network {

std::map<std::string, std::promise<boost::optional<Response>>> HttpClient::_promises;

HttpClient::HttpClient(jobject j_http) {
    auto env = core::ContextBridge::AttachCurrentThread();
    _j_http = env->NewGlobalRef(j_http);
}

HttpClient::~HttpClient() {
    auto env = core::ContextBridge::AttachCurrentThread();
    env->DeleteGlobalRef(_j_http);
}

bool HttpClient::RegisterMethods(JNIEnv *env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeOnResponse",
            "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
            (void *) HttpClient::OnResponse
        },
        {
            "nativeOnFailure",
            "(Ljava/lang/String;)V",
            (void *) HttpClient::OnFailure
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/core/network/HttpClient",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

void HttpClient::OnResponse(JNIEnv *env, jobject j_this, jstring j_request_id, jint j_status, jstring j_body, jstring j_header) {
    Response response;
    response.status = j_status;

    auto body = JStringToStdString(env, j_body);
    response.body = nlohmann::json::parse(body);

    auto header = JStringToStdString(env, j_header);
    response.header = nlohmann::json::parse(header);

    auto request_id = JStringToStdString(env, j_request_id);
    _promises[request_id].set_value(response);
}

void HttpClient::OnFailure(JNIEnv *env, jobject j_this, jstring j_request_id) {
    auto request_id = JStringToStdString(env, j_request_id);
    _promises[request_id].set_value(boost::none);
}

std::future<boost::optional<Response>> HttpClient::Request(const std::string &url,
                                                                          const std::string &method,
                                                                          const nlohmann::json &header,
                                                                          const nlohmann::json &body) {
    std::promise<boost::optional<Response>> p;
    std::future<boost::optional<Response>> f = p.get_future();
    auto uuid = random_generator()();
    auto request_id = boost::lexical_cast<std::string>(uuid);
    _promises[request_id] = std::move(p);

    auto env = core::ContextBridge::AttachCurrentThread();
    auto j_url = env->NewStringUTF(url.c_str());
    auto j_method = env->NewStringUTF(method.c_str());
    auto j_header = env->NewStringUTF(header.dump().c_str());
    auto j_body = env->NewStringUTF(body.dump().c_str());
    auto j_request_id = env->NewStringUTF(request_id.c_str());
    CallJavaMethod(env,
                   this->_j_http,
                   "request",
                   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                   j_url,
                   j_method,
                   j_header,
                   j_body,
                   j_request_id);
    env->DeleteLocalRef(j_url);
    env->DeleteLocalRef(j_method);
    env->DeleteLocalRef(j_header);
    env->DeleteLocalRef(j_body);
    env->DeleteLocalRef(j_request_id);
    return f;
}

}  // namespace network
}  // namespace skyway_android
