//
//  native_to_jlong.cpp
//  skyway_android
//
//  Copyright © 2023 NTT Communications. All rights reserved.
//

#include "native_to_jlong.hpp"

jlong NativeToJlong(void* ptr) {
    jlong jlong_ptr = reinterpret_cast<intptr_t>(ptr);
    return jlong_ptr;
}
