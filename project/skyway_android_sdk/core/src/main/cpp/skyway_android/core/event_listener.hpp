//
//  event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include <thread>
#include <vector>

#ifndef SKYWAY_ANDROID_SDK_EVENT_LISTENER_HPP
#define SKYWAY_ANDROID_SDK_EVENT_LISTENER_HPP

namespace skyway_android {
namespace core {

class EventListener {
public:
    EventListener();
    void Dispose();

protected:
    void JoinAllThreads();

    bool _is_disposed;
    std::mutex _thread_mtx;
    std::vector<std::unique_ptr<std::thread>> _threads;
};

}  // namespace core
}  // namespace skyway_android


#endif //SKYWAY_ANDROID_SDK_EVENT_LISTENER_HPP
