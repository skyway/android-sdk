package com.example.regressiontest.model

import com.google.gson.JsonObject

data class SessionDataResponse(
    var requestId: String,
    var taskId: String,
    var command: String,
    var payload: PayLoad?
)

data class PayLoad(
    var taskRoom: String,
    var clients: Int,
    var options: JsonObject
)
