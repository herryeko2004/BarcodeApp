package com.bmt_jatim.barcodeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEt = findViewById(R.id.usernameEt)
        passwordEt = findViewById(R.id.passwordEt)
        loginBtn = findViewById(R.id.loginBtn)
        progressBar = findViewById(R.id.progressBar)



        // Cek manual sementara (nanti bisa ganti ke API)
        val session = SessionManager(this)

        loginBtn.setOnClickListener {
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi username dan password dulu ya 😅", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Cek manual sementara (nanti bisa ganti ke API)
//            if (username == "admin" && password == "1234") {
//                session.saveLogin(username)
//                Toast.makeText(this, "Selamat datang $username 👋", Toast.LENGTH_SHORT).show()
//
//                val intent = Intent(this, MainActivity::class.java)
//                intent.putExtra("USERNAME", username)
//                startActivity(intent)
//                finish()
//            } else {
//                Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
//            }

            //Cek User Manual
            manualUser(username,password)

            // Cek User dengan API
            //loginUser(username, password)
        }
    }

    private fun manualUser(username: String, password: String){
        val session = SessionManager(this)
        if (username == "admin" && password == "1234") {
            session.saveLogin(username)
            Toast.makeText(this, "Selamat datang $username 👋", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(username: String, password: String) {
        val client = OkHttpClient()
        val url = "http://code91.bmtnujatim.id:8887/api/login"

        val jsonBody = """
            {
                "username": "$username",
                "password": "$password"
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        runOnUiThread { progressBar.visibility = View.VISIBLE }

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "Gagal konek ke server: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    try {
                        val json = JSONObject(responseBody)
                        val success = json.optBoolean("success", false)

                        if (success) {
                            val data = json.optJSONObject("data")
                            val userName = data?.optString("username") ?: username

                            Toast.makeText(this@LoginActivity, "Login sukses! Selamat datang, $userName 👋", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("USERNAME", userName)
                            startActivity(intent)
                            finish()
                        } else {
                            val msg = json.optString("message", "Login gagal: ${json.optString("message", "Username atau password salah")}")
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@LoginActivity,
                            "Response tidak sesuai format JSON:\n$responseBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        })
    }
}
