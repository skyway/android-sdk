//
// Created by riad on 2024/08/27.
//
#include <string>
#include "test_event_listener.hpp"

namespace util {


TestEventListener::TestEventListener() = default;
TestEventListener::~TestEventListener() = default;

void TestEventListener::OnTestProgramStart(const testing::UnitTest& unit_test) {
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[=============] Running %d tests from %d test suites.",
    unit_test.total_test_count(), unit_test.total_test_suite_count());
}

void TestEventListener::OnTestSuiteStart(const testing::TestSuite& test_suite) {
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[-------------] Running %d tests from %s",
    test_suite.total_test_count(), test_suite.name());
}

void TestEventListener::OnTestSuiteEnd(const testing::TestSuite& test_suite) {
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[-------------] PASSED: %d/%d tests from %s (%lld ms total)",
                        test_suite.successful_test_count(), test_suite.total_test_count(), test_suite.name(), test_suite.elapsed_time());
}

void TestEventListener::OnTestStart(const testing::TestInfo& test_info) {
    std::string test_name = std::string(test_info.test_case_name()) + "." + test_info.name();
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[ RUN         ] %s", test_name.c_str());
}

void TestEventListener::OnTestEnd(const testing::TestInfo& test_info) {
    std::string test_name = std::string(test_info.test_case_name()) + "." + test_info.name();
    if (test_info.result()->Passed()) {
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[          OK ] %s (%lld ms)", test_name.c_str(), test_info.result()->elapsed_time());
    } else if (test_info.result()->Failed()) {
        __android_log_print(ANDROID_LOG_ERROR, "skyway_test", "[      FAILED ] %s (%lld ms)", test_name.c_str(), test_info.result()->elapsed_time());
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "skyway_test", "[     UNKNOWN ] %s (%lld ms)", test_name.c_str(), test_info.result()->elapsed_time());
    }
}


void TestEventListener::OnTestProgramEnd(const testing::UnitTest& unit_test) {
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[-------------] Global test environment tear-down");
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[=============] %d tests from %d test suites ran. (%lld ms total)",
    unit_test.total_test_count(), unit_test.total_test_suite_count(), unit_test.elapsed_time());
    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[   PASSED    ] %d tests.", unit_test.successful_test_count());
    if (unit_test.skipped_test_count() > 0) {
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[   SKIPPED   ] %d tests.", unit_test.skipped_test_count());
    }

    int failed_tests = unit_test.failed_test_count();

    if (failed_tests > 0) {
        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[   FAILED   ] %d test%s, listed below:",
                            failed_tests, failed_tests > 1 ? "s" : "");
        for (int i = 0; i < unit_test.total_test_case_count(); ++i) {
            const testing::TestCase* test_case = unit_test.GetTestCase(i);
            for (int j = 0; j < test_case->total_test_count(); ++j) {
                const testing::TestInfo* test_info = test_case->GetTestInfo(j);
                if (test_info->result()->Failed()) {
                    std::string test_name = std::string(test_info->test_case_name()) + "." + test_info->name();
                    __android_log_print(ANDROID_LOG_INFO, "skyway_test", "[   FAILED   ] %s", test_name.c_str());
                }
            }
        }

        __android_log_print(ANDROID_LOG_INFO, "skyway_test", "%d FAILED TEST.", failed_tests);
    }

}

}  // namespace util

