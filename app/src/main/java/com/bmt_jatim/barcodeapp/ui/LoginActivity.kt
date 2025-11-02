package com.bmt_jatim.barcodeapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bmt_jatim.barcodeapp.R
import com.bmt_jatim.barcodeapp.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEt = findViewById<EditText>(R.id.usernameEt)
        val passwordEt = findViewById<EditText>(R.id.passwordEt)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val session = SessionManager(this)

        // 🔥 Kalau sudah login langsung ke MainActivity
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        loginBtn.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi username dan password dulu om 😄", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            doLogin(username, password, session)
        }
    }

    private fun doLogin(username: String, password: String, session: SessionManager) {
        //val url = "http://code91.bmtnujatim.id:8887/api/login"
        val url = "http://192.168.254.200:822/api/login"
        // 🔥 Body JSON
        val json = JSONObject().apply {
            put("nama", username)
            put("password", password)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // 🔥 Jalankan di background
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Gagal konek ke server: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread { handleLoginResponse(response, body, session) }
            }
        })
    }

    private fun handleLoginResponse(response: Response, body: String?, session: SessionManager) {
        if (response.isSuccessful && !body.isNullOrEmpty()) {
            try {
                val jsonResponse = JSONObject(body)
                val success = jsonResponse.optBoolean("success", false)

                if (success) {
                    val data = jsonResponse.getJSONObject("data")
                    val nama = data.optString("nama", "-")
                    val apiKey = data.optString("api_key", "")

                    // 🔥 Simpan session
                    session.saveLogin(apiKey, nama)

                    Toast.makeText(this, "Selamat datang $nama 👋", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Format respon tidak sesuai!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Login gagal: ${response.code}", Toast.LENGTH_SHORT).show()
        }
    }
}
