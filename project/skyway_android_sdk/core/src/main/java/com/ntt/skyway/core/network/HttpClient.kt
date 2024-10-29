package com.ntt.skyway.core.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.util.Logger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

/**
 *  @suppress
 */
object HttpClient {
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val logging = HttpLoggingInterceptor { message -> Logger.logD(message) }
    private var client: OkHttpClient

    init {
        logging.level = HttpLoggingInterceptor.Level.BASIC
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private fun request(
        url: String,
        method: String,
        header: String,
        body: String,
        id: String
    ) {
        val requestBuilder = Request.Builder().url(url)
        val headerJson = Gson().fromJson(header, JsonObject::class.java)

        val headersBuilder = Headers.Builder()
        headerJson.keySet().forEach {
            headersBuilder.add(it, headerJson.get(it).asString)

        }
        requestBuilder.headers(headersBuilder.build())


        val request = when (method) {
            "GET" -> requestBuilder.get().build()
            "POST" -> requestBuilder.post(body.toRequestBody(JSON)).build()
            "PUT" -> requestBuilder.put(body.toRequestBody(JSON)).build()
            "DELETE" -> requestBuilder.delete().build()
            else -> throw IllegalArgumentException("Invalid method")
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                nativeOnResponse(
                    id,
                    response.code,
                    response.body?.string() ?: "{}",
                    Gson().toJson(response.headers).toString()
                )
            }

            override fun onFailure(call: Call, e: IOException) {
                nativeOnFailure(id)
            }
        })
    }

    private external fun nativeOnResponse(
        id: String,
        status: Int,
        body: String,
        headers: String
    )

    private external fun nativeOnFailure(id: String)
}
