#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <jni.h>
#include <modules/utility/include/jvm_android.h>
#include <rtc_base/logging.h>
#include <rtc_base/ssl_adapter.h>
#include <sdk/android/native_api/base/init.h>

#include <core/context/context_bridge.hpp>
#include <core/network/http_client.hpp>
#include <core/network/websocket_client_factory.hpp>
#include <fstream>
#include <iostream>
#include <mediasoupclient.hpp>
#include <string>
#include <thread>

#include "android/log.h"
#include "wrapper/http_client.hpp"
#include "wrapper/logger.hpp"
#include "wrapper/websocket_client_factory.hpp"

bool isOpen = false;

void onOpen() {
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "server is opened");
    isOpen = true;
}

JavaVM* jvm;

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    __android_log_write(ANDROID_LOG_INFO, "SkyWayTest", "Init");
    jvm = vm;

    char** argv;
    int argc                     = 0;
    testing::FLAGS_gmock_verbose = "error";
    testing::InitGoogleMock(&argc, argv);
    testing::InitGoogleTest(&argc, argv);

    // select test case
    // testing::GTEST_FLAG(filter) = "SfuIntegrationTest.*";

    freopen("/data/data/com.ntt.skyway.libskywaytest/out.txt", "w", stdout);
    freopen("/data/data/com.ntt.skyway.libskywaytest/out.txt", "w", stderr);

    __android_log_write(ANDROID_LOG_INFO, "SkyWayTest", "Finish");
    return JNI_VERSION_1_6;
}
extern "C" JNIEXPORT int JNICALL
Java_com_ntt_skyway_libskywaytest_MainActivity_startTest(JNIEnv* env,
                                               jobject j_this,
                                               jobject context,
                                               jobject j_http,
                                               jobject j_ws_factory,
                                               jobject j_logger) {
    rtc::LogMessage::LogToDebug(rtc::LS_NONE);
    webrtc::InitAndroid(jvm);
    webrtc::JVM::Initialize(jvm, context);
    mediasoupclient::Initialize();

    skyway_android::core::ContextBridge::SetJavaVMFromEnv(env);

    skyway_android::network::HttpClient::RegisterMethods(env);
    skyway_android::network::WebSocketClient::RegisterMethods(env);

    skyway::network::HttpClient::Setup(j_http);
    skyway::network::WebSocketClientFactory::Setup(j_ws_factory);
    skyway::global::Logger::Setup(j_logger);

    int test_result = 0;
    for (int i = 0; i < 1; i++) {
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "RUN_ALL_TESTS start");
        test_result = RUN_ALL_TESTS();
        if (test_result) break;
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "RUN_ALL_TESTS end");
    }

    fclose(stdout);
    fclose(stderr);

    std::ifstream ifs("/data/data/com.ntt.skyway.libskywaytest/out.txt");
    std::string str = "output...\n";

    if (ifs.fail()) {
        __android_log_write(ANDROID_LOG_ERROR, "skyway_test", "Failed to open file.");
        return -1;
    }

    std::string temp;
    while (std::getline(ifs, temp)) {
        str += temp + "\n";
    }

    __android_log_write(ANDROID_LOG_INFO, "skyway_test", str.c_str());
    return test_result;
}
