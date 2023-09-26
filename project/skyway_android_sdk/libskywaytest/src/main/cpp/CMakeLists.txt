cmake_minimum_required(VERSION 3.10.2)

project("nativetest")

include(../../../../core/src/main/cpp/env.cmake)
add_definitions(-Wno-deprecated-declarations)

# External libraries
add_library( libwebrtc STATIC IMPORTED )
set_target_properties( libwebrtc
        PROPERTIES
        IMPORTED_LOCATION
        ${LIBWEBRTC_BINARY_PATH}/libwebrtc.a
        )

# Build libmediasoupclient
add_subdirectory( ${LIBMEDIASOUPCLIENT_DIR} ${LIBMEDIASOUPCLIENT_DIR}/${ANDROID_ABI} )

# libskyway_
FILE( GLOB_RECURSE LIBSKYWAY ${LIBSKYWAY_DIR}/src/*.cpp )
add_library( libskyway_ SHARED ${LIBSKYWAY} )

target_include_directories( libskyway_ PRIVATE
        ${LIBMEDIASOUPCLIENT_DIR}/include
        ${LIBMEDIASOUPCLIENT_DIR}/deps/libsdptransform/include
        ${LIBWEBRTC_INCLUDE_PATH}
        ${LIBWEBRTC_INCLUDE_PATH}/third_party/abseil-cpp
        ${LIBWEBRTC_INCLUDE_PATH}/third_party/boringssl/src/include
        ${LIBSKYWAY_DIR}/include
        ${LIBSKYWAY_DIR}/deps/boost
        ${LIBSKYWAY_DIR}/deps/spdlog/include
        )

target_compile_options(libskyway_ PRIVATE -Wno-unknown-attributes)

option(ANDROID_PLATFORM "WEBRTC_ANDROID build for Android" ON)

if (ANDROID_PLATFORM)
    MESSAGE(STATUS "WITH_WEBRTC_ANDROID_PLATFORM")
    add_definitions(-DANDROID_PLATFORM)
endif()

target_compile_definitions( libskyway_ PRIVATE
        WEBRTC_LINUX=1
        WEBRTC_ANDROID=1
        WEBRTC_POSIX=1
        WEBRTC_AUDIO_DEVICE_INCLUDE_ANDROID_AAUDIO=1
        )

target_link_libraries( libskyway_ mediasoupclient libwebrtc OpenSLES log -Wl,--allow-multiple-definition )

#gtest
set(GOOGLETEST_ROOT ${LIBSKYWAY_DIR}/test/deps/googletest/googletest/)
add_library(gtest STATIC ${GOOGLETEST_ROOT}/src/gtest_main.cc ${GOOGLETEST_ROOT}/src/gtest-all.cc)
target_include_directories(gtest PRIVATE ${GOOGLETEST_ROOT})
target_include_directories(gtest PUBLIC ${GOOGLETEST_ROOT}/include)

#gmock
set(GOOGLEMOCK_ROOT ${LIBSKYWAY_DIR}/test/deps/googletest/googlemock/)
add_library(gmock STATIC ${GOOGLEMOCK_ROOT}/src/gmock_main.cc ${GOOGLEMOCK_ROOT}/src/gmock-all.cc)
target_include_directories(gmock PRIVATE ${GOOGLEMOCK_ROOT})
target_include_directories(gmock PUBLIC ${GOOGLETEST_ROOT}/include ${GOOGLEMOCK_ROOT}/include/)


#tests
FILE( GLOB_RECURSE TEST_UTIL ${LIBSKYWAY_DIR}/test/util/*.cpp )
FILE( GLOB_RECURSE UNIT_TEST ${LIBSKYWAY_DIR}/test/unit/*.cpp )
FILE( GLOB_RECURSE INTEGRATION_TEST ${LIBSKYWAY_DIR}/test/integration/*.cpp )
FILE( GLOB_RECURSE FAKER_TEST ${LIBSKYWAY_DIR}/test/faker/*.cpp )
FILE( GLOB_RECURSE ANDROID_CONTEXT ../../../../core/src/main/cpp/skyway_android/core/context/*.cpp )
FILE( GLOB_RECURSE ANDROID_EVENT_LISTENER ../../../../core/src/main/cpp/skyway_android/core/event_listener.cpp )
FILE( GLOB_RECURSE ANDROID_UTIL ../../../../core/src/main/cpp/skyway_android/core/util/*.cpp )
FILE( GLOB_RECURSE ANDROID_NETWORK ../../../../core/src/main/cpp/skyway_android/core/network/*.cpp )
FILE( GLOB_RECURSE ANDROID_WRAPPER wrapper/*.cpp )

add_library(nativetest SHARED
        ${TEST_UTIL}
        ${UNIT_TEST}
        ${INTEGRATION_TEST}
        ${FAKER_TEST}
        ${ANDROID_CONTEXT}
        ${ANDROID_EVENT_LISTENER}
        ${ANDROID_NETWORK}
        ${ANDROID_UTIL}
        ${ANDROID_WRAPPER}
        nativetest.cpp)

target_compile_options(nativetest PRIVATE -Wno-unknown-attributes)

target_compile_definitions( nativetest PRIVATE WEBRTC_ANDROID=1)
target_include_directories( nativetest PRIVATE
        ${LIBSKYWAY_DIR}/deps/boost
        ${LIBSKYWAY_DIR}/include
        ${LIBSKYWAY_DIR}
        ${LIBMEDIASOUPCLIENT_DIR}/include
        ${LIBMEDIASOUPCLIENT_DIR}/deps/libsdptransform/include
        ${LIBWEBRTC_INCLUDE_PATH}
        ${LIBWEBRTC_INCLUDE_PATH}/third_party/abseil-cpp
        ${LIBWEBRTC_INCLUDE_PATH}/third_party/boringssl/src/include
        ../../../../core/src/main/cpp/skyway_android/
        ./)

target_link_libraries( nativetest
        libskyway_
        gmock
        gtest
        -static-libstdc++)