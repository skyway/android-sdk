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

JavaVM* jvm;

extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    jvm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT int JNICALL
Java_com_ntt_skyway_libskywaytest_MainActivity_startTest(JNIEnv* env,
                                               jobject j_this,
                                               jobject context,
                                               jobject j_http,
                                               jobject j_ws_factory,
                                               jobject j_logger) {
    __android_log_write(ANDROID_LOG_INFO, "SkyWayTest", "startTest init");
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

    testing::FLAGS_gmock_verbose = "error";
    testing::InitGoogleMock();
    testing::InitGoogleTest();

    // select test case
    // testing::GTEST_FLAG(filter) = "SfuIntegrationTest.*";

    const auto OUTPUT_PATH = "/data/data/com.ntt.skyway.libskywaytest/out.txt";
    freopen(OUTPUT_PATH, "w", stdout);
    freopen(OUTPUT_PATH, "w", stderr);

    const int NUMBER_OF_TIMES_TO_TEST = 1;
    int test_result = 0;
    for (int i = 0; i < NUMBER_OF_TIMES_TO_TEST; i++) {
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "RUN_ALL_TESTS start");
        test_result = RUN_ALL_TESTS();
        if (test_result) break;
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "RUN_ALL_TESTS end");
    }

    fclose(stdout);
    fclose(stderr);

    std::ifstream ifs(OUTPUT_PATH);
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
