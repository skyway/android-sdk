package com.example.regressiontest.model

data class SessionDataRequest(
    var requestId: String,
    var result: Result
)

data class Result(
    var success: Boolean,
    var fail: String? = "",
    var from: String? = "Android"
)
