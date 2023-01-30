//
//  remote_person_bridge.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "remote_person_bridge.hpp"

#include <json.hpp>

#include "core/channel/channel_util.hpp"
#include "core/util/register_methods_helper.hpp"
#include "core/util/jstring_to_string.hpp"

namespace skyway_android {
namespace plugin {
namespace remote_person {


bool RemotePersonBridge::RegisterMethods(JNIEnv* env) {
    JNINativeMethod native_methods[] = {
        {
            "nativeSubscribe",
            "(JLjava/lang/String;)Ljava/lang/String;",
            (void*) RemotePersonBridge::Subscribe
        },
        {
            "nativeUnsubscribe",
            "(JLjava/lang/String;)Z",
            (void*) RemotePersonBridge::Unsubscribe
        },
    };

    return skyway_android::RegisterMethodsHelper(
        env,
        "com/ntt/skyway/plugin/remotePerson/RemotePerson",
        native_methods,
        ARRAY_LENGTH(native_methods)
    );
}

jstring RemotePersonBridge::Subscribe(JNIEnv* env, jobject j_this, jlong remote_person, jstring j_publication_id) {
    auto publication_id = JStringToStdString(env, j_publication_id);
    auto subscription = ((RemotePerson*)remote_person)->Subscribe(publication_id);
    if (!subscription) {
        return nullptr;
    }

    auto subscription_json = core::util::ToJson(subscription);
    return env->NewStringUTF(subscription_json.dump().c_str());
}

bool RemotePersonBridge::Unsubscribe(JNIEnv* env, jobject j_this, jlong remote_person, jstring j_subscription_id) {
    auto subscription_id = JStringToStdString(env, j_subscription_id);
    return ((RemotePerson*)remote_person)->Unsubscribe(subscription_id);
}

}  // namespace remote_person
}  // namespace plugin
}  // namespace skyway_android
