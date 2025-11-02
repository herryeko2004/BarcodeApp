package com.bmt_jatim.barcodeapp.repository

import android.util.Log
import com.bmt_jatim.barcodeapp.model.Barang
import com.bmt_jatim.barcodeapp.network.ApiClient
import com.bmt_jatim.barcodeapp.model.BarangResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class BarangRepository(private val apiKey: String) {
    fun fetchBarang(barcode: String, warehouse: Int, date: String, onResult: (BarangResponse?) -> Unit) {
        val endpoint="items/$barcode?warehouse=$warehouse&date=$date"
        val request = ApiClient.get(endpoint, apiKey)

        ApiClient.client().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BarangRepository", "Error: ${e.message}", e)
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    // buat debug
                    // val bodyString = response.body?.string() ?: ""
                    // Log.d("API_DEBUG", "Response body: $bodyString") // 👈 tampil di Logcat

                    if (!response.isSuccessful) {
                        onResult(null)
                        return
                    }

                    val json = JSONObject(response.body?.string() ?: "")

                    if (json.optBoolean("success")) {
                        val data = json.getJSONObject("data")
                        val barang = Barang(
                            kdbrg = data.optString("kdbrg", ""),
                            barcode = data.optString("barcode", ""),
                            nama = data.optString("nama", ""),
                            stock_oh = data.optInt("stock_oh", 0),
                            satuan = data.optString("satuan", ""),
                        )
                        val alreadyRecorded = json.optBoolean("already_recorded", false)
                        val stockRecordId = if (alreadyRecorded) json.optInt("stock_record_id") else null

                        val result = BarangResponse(
                            barang = barang,
                            alreadyRecorded = alreadyRecorded,
                            stockRecordId = stockRecordId
                        )
                        //debugging
                        Log.d("API_DEBUG", "Result body: $result") // 👈 tampil di Logcat

                        onResult(result)
                    } else {
                        onResult(null)
                    }
                }
            }
        })
    }

    fun fetchBarangKdbrg(kdbrg: String,warehouse: Int, date: String, onResult: (BarangResponse?) -> Unit) {
        val request = ApiClient.get("items/kdbrg/$kdbrg?warehouse=$warehouse&date=$date", apiKey)

        ApiClient.client().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BarangRepository", "Error: ${e.message}", e)
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onResult(null)
                        return
                    }

                    val json = JSONObject(response.body?.string() ?: "")

                    if (json.optBoolean("success")) {
                        val data = json.getJSONObject("data")
                        val barang = Barang(
                            kdbrg = data.optString("kdbrg", ""),
                            barcode = data.optString("barcode", ""),
                            nama = data.optString("nama", ""),
                            stock_oh = data.optInt("stock_oh", 0),
                            satuan = data.optString("satuan", ""),
                        )
                        val alreadyRecorded = json.optBoolean("already_recorded", false)
                        val stockRecordId = if (alreadyRecorded) json.optInt("stock_record_id") else null

                        val result = BarangResponse(
                            barang = barang,
                            alreadyRecorded = alreadyRecorded,
                            stockRecordId = stockRecordId
                        )
                        //debugging
                        Log.d("API_DEBUG", "Result body: $result") // 👈 tampil di Logcat

                        onResult(result)
                    } else {
                        onResult(null)
                    }
                }
            }
        })
    }


    fun saveDataToServer(barang: Barang, onComplete: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("kdbrg", barang.kdbrg)
            put("barcode", barang.barcode)
            put("nama", barang.nama)
            put("stock_oh", barang.stock_oh)
            put("satuan", barang.satuan)
        }

        val request = ApiClient.post("items/save", apiKey, json.toString())

        ApiClient.client().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BarangRepository", "Error saving data: ${e.message}")
                onComplete(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val success = response.isSuccessful
                Log.d("BarangRepository", "Save response: ${response.code}")
                onComplete(success)
            }
        })
    }
}