package com.bmt_jatim.barcodeapp.repository


import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bmt_jatim.barcodeapp.model.StockOpname
import com.bmt_jatim.barcodeapp.network.ApiClient
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class StockOpnameRepository(private val apiKey: String) {

    private val endpoint = "stocks"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun saveOpname(opname: StockOpname, onComplete: (Boolean) -> Unit) {
        try {
            // Buat JSON body
            val json = JSONObject().apply {
                put("kdbrg", opname.kdbrg)
                put("barcode", opname.barcode)
                put("nama", opname.nama)
                put("satuan", opname.satuan)
                put("warehouse", opname.warehouse)
                put("nama_operator", opname.nama_operator)
                put("stock_oh", opname.stock_oh)
                put("stock_op", opname.stock_op)
            }

            // Buat request POST ke API
            val request = ApiClient.post(endpoint, apiKey, json.toString())

            // Jalankan request
            ApiClient.client().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("StockOpnameRepo", "Gagal kirim opname: ${e.message}")
                    mainHandler.post { onComplete(false) }
                }

                override fun onResponse(call: Call, response: Response) {
                    val success = response.isSuccessful
                    Log.d("StockOpnameRepo", "Response opname: ${response.code}")
                    mainHandler.post { onComplete(success) }
                }
            })
        } catch (e: Exception) {
            Log.e("StockOpnameRepo", "Exception: ${e.message}")
            // onComplete(false)
            mainHandler.post { onComplete(false) }
        }
    }

    fun updateOpname(currentStockId: Int, opname: StockOpname, onComplete: (Boolean) -> Unit) {
        try {
            // JSON body
            val json = JSONObject().apply {
                put("kdbrg", opname.kdbrg)
                put("barcode", opname.barcode)
                put("nama", opname.nama)
                put("satuan", opname.satuan)
                put("warehouse", opname.warehouse)
                put("nama_operator", opname.nama_operator)
                put("stock_oh", opname.stock_oh)
                put("stock_op", opname.stock_op)
            }

            // Contoh endpoint: /api/opname/{id}
            val url = "$endpoint/id/$currentStockId"

            // Buat request PUT ke API
            val request = ApiClient.post(url, apiKey, json.toString())

            // Jalankan request
            ApiClient.client().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("StockOpnameRepo", "Gagal update opname: ${e.message}")
                    mainHandler.post { onComplete(false) }
                }

                override fun onResponse(call: Call, response: Response) {
                    val success = response.isSuccessful
                    Log.d("StockOpnameRepo", "Response update opname: ${response.code}")
                    mainHandler.post { onComplete(success) }
                }
            })
        } catch (e: Exception) {
            Log.e("StockOpnameRepo", "Exception update: ${e.message}")
            mainHandler.post { onComplete(false) }
        }
    }


}
