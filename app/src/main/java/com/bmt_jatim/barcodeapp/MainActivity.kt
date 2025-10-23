package com.bmt_jatim.barcodeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // Deklarasi semua view
    lateinit var scanBtn: Button
    lateinit var cariBtn: Button
    lateinit var saveBtn: Button
    lateinit var scanResultTv: TextView
    lateinit var productKodeTv: EditText
    lateinit var productKodeBarangTv: TextView
    lateinit var productBarcodeBarangTv: TextView
    lateinit var productNameTv: TextView
    lateinit var productPriceTv: TextView
    lateinit var productStockOHTv: TextView
    lateinit var productSatuanTv: TextView
    lateinit var productStockOPTv: EditText
    lateinit var progressBar: ProgressBar
    lateinit var spinner: Spinner
    // 🆕 Tambahkan di sini (bukan di dalam onCreate)
    lateinit var recyclerView: RecyclerView
    lateinit var opnameList: MutableList<OpnameData>
    lateinit var adapter: OpnameAdapter


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // insets bawaan Android
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //    insets
        //}

        // inisialisasi view
        scanBtn = findViewById(R.id.scanBtn)
        cariBtn = findViewById(R.id.cariBtn)
        saveBtn = findViewById(R.id.saveBtn)
        scanResultTv = findViewById(R.id.scanResultTv)
        productKodeTv = findViewById(R.id.productkodeTv)
        productKodeBarangTv = findViewById(R.id.productkodebarangTv)
        productBarcodeBarangTv = findViewById(R.id.productbarcodebarangTv)
        productNameTv = findViewById(R.id.productnameTv)
        productPriceTv = findViewById(R.id.productpriceTv)
        productStockOHTv = findViewById(R.id.productstockohTv)
        productSatuanTv = findViewById(R.id.productsatuanTv)
        productStockOPTv = findViewById(R.id.productstockopTv)
        progressBar = findViewById(R.id.progressBar)



        recyclerView = findViewById(R.id.recyclerViewOpname)
        opnameList = mutableListOf()
        adapter = OpnameAdapter(opnameList)
//        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter


        // Spinner
        //val spinner = findViewById<Spinner>(R.id.spinnerCombo)
        spinner = findViewById(R.id.spinnerCombo)

        val items = listOf("Pilih Lokasi", "Rak", "Gudang")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (position != 0) {
                    Toast.makeText(this@MainActivity, "Kamu pilih: $selectedItem", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

//        val username = intent.getStringExtra("USERNAME")
//        findViewById<TextView>(R.id.userLabelTv)?.text = "Halo, $username!"

        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val username = session.getUsername()
        findViewById<TextView>(R.id.userLabelTv)?.text = "Halo, $username!"

        // Tombol logout
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
//        logoutBtn.setOnClickListener {
//            session.logout()
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
        }

        // Tombol list
        val viewListBtn = findViewById<Button>(R.id.viewListBtn)
        viewListBtn.setOnClickListener {
            val intent = Intent(this, OpnameListActivity::class.java)
            startActivity(intent)
        }

        // Tombol scan
        scanBtn.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("Arahkan kamera ke barcode")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            options.setCaptureActivity(CaptureActivity::class.java)
            barcodeLauncher.launch(options)
        }

        // Tombol cari barang manual
        cariBtn.setOnClickListener {
            val kodeBarangInput = productKodeTv.text.toString().trim()
            if (kodeBarangInput.isEmpty()) {
                Toast.makeText(this, "Masukkan kode barang dulu om", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scanResultTv.text = "Cari barang dengan kode: $kodeBarangInput"
            fetchBarang(kodeBarangInput)
        }

        // Tombol save
        //saveBtn.setOnClickListener {
        //    val kode = productKodeBarangTv.text.toString()
        //    val qtyOp = productStockOPTv.text.toString()
        //    Toast.makeText(this, "Data disimpan: $kode - $qtyOp", Toast.LENGTH_SHORT).show()
            // TODO: Kirim ke server / simpan ke database lokal
        //}

        val saveBtn = findViewById<Button>(R.id.saveBtn)

        saveBtn.setOnClickListener {
            val kodebarang = productKodeBarangTv.text.toString()
                .replace("Kode Barang: ", "")
                .trim()
            val barcode = productBarcodeBarangTv.text.toString()
                .replace("Barcode Barang: ", "")
                .trim()
            val namabarang = productNameTv.text.toString()
                .replace("Nama Barang: ", "")
                .trim()
            val qtyohText = productStockOHTv.text.toString()
                .replace("Stock OH: ", "")
                .replace("Qty OH", "")
                .trim()
            val qtyopText = productStockOPTv.text.toString().trim()
            val lokasi = spinner.selectedItem.toString()

            if (kodebarang.isEmpty() || qtyopText.isEmpty() || lokasi == "Pilih Lokasi") {
                Toast.makeText(this, "Lengkapi data dulu om 😅", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert qty ke Number (Double)
            val qtyoh = qtyohText.toDoubleOrNull() ?: 0.0
            val qtyop = qtyopText.toDoubleOrNull() ?: 0.0

            // Kirim data ke server
            saveDataToServer(kodebarang, barcode, namabarang, qtyoh, qtyop, lokasi)
        }


    }

    // Event hasil scan barcode
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val kodeBarang = result.contents
            scanResultTv.text = "Hasil scan: $kodeBarang"
            fetchBarang(kodeBarang)
        } else {
            Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDataToServer(
        kodeBarang: String,
        barcode: String,
        nama: String,
        qtyoh: Number,
        qtyop: Number,
        lokasi: String
    ) {
        val client = OkHttpClient()
        val url = "https://code91.bmtnujatim.id:8887/api/opnamecreate"

        // Pastikan nilai Number tidak null
        val stockOhNum = qtyoh.toDouble()
        val stockOpNum = qtyop.toDouble()

        val jsonBody = """
        {
            "kdbrg": "$kodeBarang",
            "barcode": "$barcode",
            "nama": "$nama",
            "stock_oh": $stockOhNum,
            "stock_op": $stockOpNum,
            "lokasi": "$lokasi"
        }
    """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            saveBtn.isEnabled = false
            Toast.makeText(this, "⏳ Menyimpan data ke server...", Toast.LENGTH_SHORT).show()
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    saveBtn.isEnabled = true
                    Toast.makeText(
                        this@MainActivity,
                        "❌ Gagal konek ke server: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string() ?: ""

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    saveBtn.isEnabled = true

                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "⚠️ Gagal simpan (${response.code})",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "✅ Data berhasil disimpan!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Tambah ke daftar lokal di tampilan saat ini
                        opnameList.add(OpnameData(kodeBarang, barcode, nama, qtyoh, qtyop, lokasi))
                        adapter.notifyItemInserted(opnameList.size - 1)

                        // Simpan juga ke global store (untuk ditampilkan di OpnameListActivity)
                        OpnameDataStore.add(OpnameData(kodeBarang, barcode, nama, qtyoh, qtyop, lokasi))


                        // Kosongkan input
                        productStockOPTv.setText("")
                        spinner.setSelection(0)
                    }
                }

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()

                    // Tambah ke daftar lokal
                    opnameList.add(
                        OpnameData(kodeBarang, barcode, nama, qtyoh, qtyop, lokasi)
                    )
                    adapter.notifyItemInserted(opnameList.size - 1)

                    // Kosongkan input
                    productStockOPTv.setText("")
                    spinner.setSelection(0)
                }


                // Debugging log
                println("JSON Sent: $jsonBody")
                println("Response body: $bodyString")
            }
        })
    }


    // Fungsi ambil data barang dari API berdasar kdbrg
    private fun fetchBarang(kode: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://code91.bmtnujatim.id:8887/api/items/kdbrg/$kode")
            .build()

        progressBar.visibility = View.VISIBLE

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    runOnUiThread { progressBar.visibility = View.GONE }

                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Server error-h: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

//                    val bodyString = response.body?.string()
//                    if (bodyString.isNullOrEmpty()) return
                    val bodyString = response.body?.string()
                    android.util.Log.d("API_RESPONSE", "Response body: $bodyString")

                    if (bodyString.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Respon kosong dari server", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }


                    val json = JSONObject(bodyString)
                    val success = json.optBoolean("success", false)

                    runOnUiThread {
                        if (success) {
                            val data = json.getJSONObject("data")
                            productKodeBarangTv.text = "Kode Barang: ${data.optString("kdbrg", "-")}"
                            productBarcodeBarangTv.text = "Barcode Barang: ${data.optString("barcode", "-")}"
                            productNameTv.text = "Nama Barang: ${data.optString("nama", "-")}"
                            productPriceTv.text = "Harga: Rp ${data.optString("harga", "0")}"
                            productStockOHTv.text = "Stock OH: ${data.optString("stock_oh", "0")}"
                            productSatuanTv.text = "Satuan: ${data.optString("satuan", "-")}"
                            productStockOPTv.setText(data.optString("qtyop", "0"))
                        } else {
                            Toast.makeText(this@MainActivity, "Barang tidak ditemukan", Toast.LENGTH_SHORT).show()
                            productKodeBarangTv.text = "Kode Barang: -"
                            productBarcodeBarangTv.text = "Barcode Barang: -"
                            productNameTv.text = "Nama Barang: -"
                            productPriceTv.text = "Harga: -"
                            productStockOHTv.text = "Stock OH: 0"
                            productSatuanTv.text = "Satuan: -"
                            productStockOPTv.setText("0")
                        }
                    }
                }
            }
        })
    }
}

