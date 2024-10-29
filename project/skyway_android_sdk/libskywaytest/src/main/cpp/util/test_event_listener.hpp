//
// Created by riad on 2024/08/27.
//

#ifndef SKYWAY_ANDROID_TEST_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_TEST_EVENT_LISTENER_HPP

#include <gtest/gtest.h>
#include <android/log.h>

namespace util {

class TestEventListener : public testing::EmptyTestEventListener {
public:
    TestEventListener();
    ~TestEventListener() override;

    void OnTestProgramStart(const testing::UnitTest& unit_test) override;
    void OnTestSuiteStart(const testing::TestSuite& test_suite) override;
    void OnTestSuiteEnd(const testing::TestSuite& test_suite) override;
    void OnTestStart(const testing::TestInfo& test_info) override;
    void OnTestEnd(const testing::TestInfo& test_info) override;
    void OnTestProgramEnd(const testing::UnitTest& unit_test) override;
};

}  // namespace util

#endif // SKYWAY_ANDROID_TEST_EVENT_LISTENER_HPP

