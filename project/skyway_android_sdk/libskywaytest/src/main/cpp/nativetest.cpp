#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <jni.h>
#include <modules/utility/include/jvm_android.h>
#include <rtc_base/logging.h>
#include <rtc_base/ssl_adapter.h>

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
#include "util/test_event_listener.hpp"

JavaVM* jvm;

std::string getCurrentDateTime() {
    auto now = std::chrono::system_clock::now();
    auto now_time_t = std::chrono::system_clock::to_time_t(now);
    auto now_localtime = *std::localtime(&now_time_t);

    std::ostringstream oss;
    oss << std::put_time(&now_localtime, "%Y-%m-%d %H:%M:%S");
    return oss.str();
}


extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    jvm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT int JNICALL
Java_com_ntt_skyway_libskywaytest_SkywayTest_startTestNative(JNIEnv* env,
                                               jobject j_this,
                                               jobject context,
                                               jobject j_http,
                                               jobject j_ws_factory,
                                               jobject j_logger) {
    __android_log_write(ANDROID_LOG_INFO, "SkyWayTest", "startTest init");
    rtc::LogMessage::LogToDebug(rtc::LS_NONE);
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

    // register TestEventListener
    testing::TestEventListeners& listeners = testing::UnitTest::GetInstance()->listeners();
    listeners.Append(new util::TestEventListener);


    // select test case
//     testing::GTEST_FLAG(filter) = "PublicationIntegrationTest.*";

    const auto OUTPUT_PATH = "/data/data/com.ntt.skyway.libskywaytest/out.txt";
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "TESTS OUTPUT_PATH: %s", OUTPUT_PATH);
    
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

    std::ofstream ofs(OUTPUT_PATH, std::ios_base::out | std::ios_base::app);
    ofs << "Test ended at: " << getCurrentDateTime() << "\n";
    ofs.close();

    std::ifstream ifs(OUTPUT_PATH);
    std::string str = "output...\n";

    if (ifs.fail()) {
        __android_log_write(ANDROID_LOG_ERROR, "skyway_test", "Failed to open file.");
        return -1;
    }

    std::string line;
    int lineCount = 0;
    std::string chunk;

    while (std::getline(ifs, line)) {
        chunk += line + "\n";
        lineCount++;

        if (lineCount == 50) {
            __android_log_write(ANDROID_LOG_INFO, "skyway_test_output", chunk.c_str());
            chunk.clear();
            lineCount = 0;
        }
    }

// log any remaining lines (if less than 50)
    if (!chunk.empty()) {
        __android_log_write(ANDROID_LOG_INFO, "skyway_test_output", chunk.c_str());
    }

    ifs.close();

    __android_log_write(ANDROID_LOG_INFO, "skyway_test", "Test completed");
    
    return test_result;
}
