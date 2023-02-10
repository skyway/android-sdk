//
//  event_listener.hpp
//  skyway_android
//
//  Copyright © 2022 NTT Communications. All rights reserved.
//

#include "event_listener.hpp"

namespace skyway_android {
namespace core {

EventListener::EventListener(): _is_disposed(false) {}

void EventListener::Dispose() {
    std::lock_guard<std::mutex> lg(_thread_mtx);
    JoinAllThreads();
    _is_disposed = true;
}

void EventListener::JoinAllThreads() {
    for (auto& t : _threads) {
        if (t && t->joinable()) {
            t->join();
        }
    }
}

}  // namespace core
}  // namespace skyway_android