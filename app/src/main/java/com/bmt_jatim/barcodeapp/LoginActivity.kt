package com.bmt_jatim.barcodeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        loginBtn.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi username dan password dulu om", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            doLogin(username, password, session)
        }
    }

    private fun doLogin(username: String, password: String, session: SessionManager) {
        val url = "http://code91.bmtnujatim.id:8887/api/login"

        // 🔥 Request body JSON
        val json = JSONObject()
        json.put("nama", username)
        json.put("password", password)

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .addHeader("X-API-KEY", "ygv9zKAw08knllyK99OuuR6Us2ODHwe4Voeo6EJ4YGuA7XHNIxKC")
            .url(url)
            .post(requestBody)
            .build()

        // 🔥 Jalankan di background thread
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Gagal konek ke server: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    try {
                        val jsonResponse = JSONObject(body)
                        val success = jsonResponse.optBoolean("success", false)

                        if (success) {
                            val data = jsonResponse.getJSONObject("data")
                            val nama = data.getString("nama")

                            runOnUiThread {
                                session.saveLogin(nama)
                                Toast.makeText(this@LoginActivity, "Selamat datang $nama 👋", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("USERNAME", nama)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Username atau password salah!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Format respon tidak sesuai!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login gagal: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}
