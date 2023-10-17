cmake_minimum_required(VERSION 3.10.2)

project("skyway")

include(env.cmake)
add_definitions(-Wno-deprecated-declarations)

set( LIBWEBRTC_INCLUDE_PATH ${LIBWEBRTC_DIR}/src CACHE STRING "libwebrtc include path" FORCE)
set( LIBWEBRTC_BINARY_PATH ${LIBWEBRTC_DIR}/src/out/${ANDROID_ABI}/obj CACHE STRING "libwebrtc binary path" FORCE)

# Build libmediasoupclient
add_subdirectory( ${LIBMEDIASOUPCLIENT_DIR} ${LIBMEDIASOUPCLIENT_DIR}/${ANDROID_ABI} )

# External libraries
add_library( libwebrtc STATIC IMPORTED )
set_target_properties( libwebrtc
    PROPERTIES
        IMPORTED_LOCATION
            ${LIBWEBRTC_BINARY_PATH}/libwebrtc.a
)

# libskyway
FILE( GLOB_RECURSE LIBSKYWAY ${LIBSKYWAY_DIR}/include/skyway/*.cpp )
add_library( libskyway SHARED ${LIBSKYWAY} )

target_include_directories( libskyway PRIVATE
    ${LIBMEDIASOUPCLIENT_DIR}/include
    ${LIBMEDIASOUPCLIENT_DIR}/deps/libsdptransform/include
    ${LIBWEBRTC_INCLUDE_PATH}
    ${LIBWEBRTC_INCLUDE_PATH}/third_party/abseil-cpp
    ${LIBWEBRTC_INCLUDE_PATH}/third_party/boringssl/src/include
    ${LIBSKYWAY_DIR}/include
    ${LIBSKYWAY_DIR}/deps/boost
    ${LIBSKYWAY_DIR}/deps/spdlog/include
)

target_compile_options(libskyway PRIVATE -Werror)

target_compile_definitions( libskyway PRIVATE
    WEBRTC_LINUX=1
    WEBRTC_ANDROID=1
    WEBRTC_POSIX=1
    WEBRTC_AUDIO_DEVICE_INCLUDE_ANDROID_AAUDIO=1
)

target_link_libraries( libskyway mediasoupclient libwebrtc OpenSLES log -Wl,--allow-multiple-definition )

# Bridges
FILE( GLOB_RECURSE SKYWAY_ANDROID skyway_android/*.cpp )
add_library( skyway_android SHARED ${SKYWAY_ANDROID} )

target_include_directories( skyway_android PRIVATE
    ${LIBWEBRTC_DIR}/src
    ${LIBWEBRTC_DIR}/src/third_party/abseil-cpp
    ${LIBMEDIASOUPCLIENT_DIR}/include
    ${LIBMEDIASOUPCLIENT_DIR}/deps/libsdptransform/include
    ${LIBSKYWAY_DIR}/include
    ${LIBSKYWAY_DIR}/deps/spdlog/include
    ${LIBSKYWAY_DIR}/deps/boost
    skyway_android
)

# Linking
target_link_libraries( skyway_android libskyway )
