package com.bmt_jatim.barcodeapp.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object ApiClient {

    private val client = OkHttpClient()
    //private const val BASE_URL = "http://192.168.254.200:822/api/"
    private const val BASE_URL = "http://192.168.254.200:822/api/"
    // 🔹 Fungsi GET
    fun get(endpoint: String, apiKey: String): Request {
        return Request.Builder()
            .url(BASE_URL + endpoint)
            .addHeader("X-API-KEY", apiKey)
            .build()
    }

    // 🔹 Fungsi POST
    fun post(endpoint: String, apiKey: String, jsonBody: String): Request {
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody
        )

        return Request.Builder()
            .url(BASE_URL + endpoint)
            .addHeader("X-API-KEY", apiKey)
            .post(requestBody)
            .build()
    }

    // 🔹 Getter client untuk dieksekusi
    fun client(): OkHttpClient {
        return client
    }
}
