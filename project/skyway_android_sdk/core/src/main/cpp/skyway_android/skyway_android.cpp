//
//  skyway_android.cpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "skyway_android.hpp"

#include <jni.h>
#include "core/context/context_bridge.hpp"
#include "core/channel/channel/channel_bridge.hpp"
#include "core/channel/publication/publication_bridge.hpp"
#include "core/channel/subscription/subscription_bridge.hpp"
#include "core/channel/member/member_bridge.hpp"
#include "core/channel/member/remote_member_bridge.hpp"
#include "core/channel/member/local_person_bridge.hpp"
#include "core/content/content_util.hpp"
#include "core/content/local/source/video_source_bridge.hpp"
#include "core/content/local/source/audio_source_bridge.hpp"
#include "core/content/local/source/data_source_bridge.hpp"
#include "core/content/local/data_stream_bridge.hpp"
#include "core/content/remote/video_stream_bridge.hpp"
#include "core/content/remote/audio_stream_bridge.hpp"
#include "core/content/remote/data_stream_bridge.hpp"
#include "plugin/remote_person_plugin/remote_person_bridge.hpp"
#include "plugin/sfu_bot_plugin/sfu_bot_bridge.hpp"
#include "core/network/http_client.hpp"
#include "core/network/websocket_client.hpp"

namespace skyway_android {

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }

  if (!core::ContextBridge::RegisterMethods(env) ||
      !core::channel::ChannelBridge::RegisterMethods(env) ||
      !core::PublicationBridge::RegisterMethods(env) ||
      !core::SubscriptionBridge::RegisterMethods(env) ||
      !core::member::MemberBridge::RegisterMethods(env) ||
      !core::member::RemoteMemberBridge::RegisterMethods(env) ||
      !core::member::LocalPersonBridge::RegisterMethods(env) ||
      !content::local::source::VideoSource::RegisterMethods(env) ||
      !content::local::source::AudioSource::RegisterMethods(env) ||
      !content::local::source::DataSource::RegisterMethods(env) ||
      !content::local::LocalDataStreamBridge::RegisterMethods(env) ||
      !content::remote::RemoteVideoStreamBridge::RegisterMethods(env) ||
      !content::remote::RemoteAudioStreamBridge::RegisterMethods(env) ||
      !content::remote::RemoteDataStreamBridge::RegisterMethods(env) ||
      !plugin::remote_person::RemotePersonBridge::RegisterMethods(env) ||
      !network::HttpClient::RegisterMethods(env) ||
      !network::WebSocketClient::RegisterMethods(env)) {
    return -1;
  }
  plugin::sfu_bot::SfuBotBridge::RegisterMethods(env);
  env->ExceptionClear();
  return JNI_VERSION_1_6;
}

}  // namespace skyway_android
