package com.bmt_jatim.barcodeapp.ui

import android.util.Log
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.bmt_jatim.barcodeapp.ui.LoginActivity
import com.bmt_jatim.barcodeapp.OpnameData
import com.bmt_jatim.barcodeapp.OpnameListActivity
import com.bmt_jatim.barcodeapp.R
import com.bmt_jatim.barcodeapp.utils.SessionManager
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

import com.bmt_jatim.barcodeapp.repository.BarangRepository
import com.bmt_jatim.barcodeapp.repository.StockOpnameRepository
import com.bmt_jatim.barcodeapp.model.Barang
import com.bmt_jatim.barcodeapp.model.StockOpname
import android.view.WindowManager
import androidx.annotation.Nullable
import com.bmt_jatim.barcodeapp.WebViewActivity

class MainActivity : AppCompatActivity() {
    private lateinit var barangRepo: BarangRepository
    private lateinit var stockOpnameRepo: StockOpnameRepository
    private var currentBarang: Barang? = null

    private var currentStockId: Int?=null

    // Deklarasi semua view
    private lateinit var scanBtn: Button
    private lateinit var cariBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var scanResultTv: TextView
    private lateinit var productKodeTv: EditText
    private lateinit var productKodeBarangTv: TextView
    private lateinit var productNameTv: TextView
    private lateinit var productStockOHTv: TextView
    private lateinit var productSatuanTv: TextView
    private lateinit var productStockOPTv: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var spinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var opnameList: MutableList<OpnameData>
    private lateinit var adapter: OpnameAdapter
    private lateinit var session: SessionManager


    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinner()
        setupRecycler()

        session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🔥 Ambil API key dari session
        val apiKey = session.getApiKey()
        if (apiKey.isNullOrEmpty()) {
            Toast.makeText(this, "API key hilang, silakan login ulang", Toast.LENGTH_SHORT).show()
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        barangRepo = BarangRepository(apiKey)
        stockOpnameRepo = StockOpnameRepository(apiKey)

        setupSession()
        setupButtonActions()

    }

    private fun initViews() {
        scanBtn = findViewById(R.id.scanBtn)
        cariBtn = findViewById(R.id.cariBtn)
        saveBtn = findViewById(R.id.saveBtn)
        scanResultTv = findViewById(R.id.scanResultTv)
        productKodeTv = findViewById(R.id.productkodeTv)
        productKodeBarangTv = findViewById(R.id.productkodebarangTv)
        productNameTv = findViewById(R.id.productnameTv)
        productStockOHTv = findViewById(R.id.productstockohTv)
        productSatuanTv = findViewById(R.id.productsatuanTv)
        productStockOPTv = findViewById(R.id.productstockopTv)
        progressBar = findViewById(R.id.progressBar)
        spinner = findViewById(R.id.spinnerCombo)
    }
    private fun setupRecycler() {
        recyclerView = findViewById(R.id.recyclerViewOpname)
        opnameList = mutableListOf()
        adapter = OpnameAdapter(opnameList)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun setupSpinner() {
        val items = listOf("Pilih Lokasi", "Rak", "Gudang")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupSession() {
        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val username = session.getUsername()
        findViewById<TextView>(R.id.userLabelTv)?.text = "Halo, $username!"

        findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.viewListBtn).setOnClickListener {
            //startActivity(Intent(this, OpnameListActivity::class.java))
            startActivity(Intent(this, WebViewActivity::class.java))

        }
    }

    private fun setupButtonActions() {

        scanBtn.setOnClickListener {
            val lokasi = spinner.selectedItem.toString()
            if (lokasi == "Pilih Lokasi") {
                Toast.makeText(this, "Isi lokasi dulu om", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val options = ScanOptions()
            options.setPrompt("Arahkan kamera ke barcode")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            options.setCaptureActivity(CaptureActivity::class.java)
            barcodeLauncher.launch(options)
        }

        cariBtn.setOnClickListener {
            val lokasi = spinner.selectedItem.toString()
            if (lokasi == "Pilih Lokasi") {
                Toast.makeText(this, "Isi lokasi dulu om", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kdbrg = productKodeTv.text.toString().trim()
            if (kdbrg.isEmpty()) {
                Toast.makeText(this, "Masukkan kode barang dulu om 😅", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchBarangKdbrg(kdbrg)
        }

        saveBtn.setOnClickListener {
            saveStockOpname()
        }
    }

    // Ambil barang dari API
    private fun fetchBarang(kode: String) {
        progressBar.visibility = View.VISIBLE
        val lokasi = spinner.selectedItem.toString()
        val warehouse : Int
        warehouse = if (lokasi == "Rak") 1 else 2

        barangRepo.fetchBarang(kode,warehouse,"") { result ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                if (result == null||result.barang==null) {
                    Toast.makeText(this, "Barang tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val barang = result.barang

                Log.d("API_DEBUG", "Result body: $result") // 👈 tampil di Logcat

                if(result.alreadyRecorded){
                    AlertDialog.Builder(this)
                        .setTitle("Barang sudah direkam stok")
                        .setMessage("Apakah ingin update data stok ini?")
                        .setPositiveButton("Update") { _, _ ->
                            // Lanjut ke mode update
                            //updateStok(result.stockRecordId!!, barang!!)
                            currentStockId=result.stockRecordId
                            currentBarang = barang
                            productKodeBarangTv.text = "Kode Barang: ${barang.kdbrg}"
                            productNameTv.text = "Nama Barang: ${barang.nama}"
                            productStockOHTv.text = "Stock OH: ${barang.stock_oh}"
                            productSatuanTv.text = "Satuan: ${barang.satuan}"
                        }
                        .setNegativeButton("Cancel"){_,_ ->
                            blankBarang()
                        }
                        .show()
                }else{
                    if (barang != null) {
                        currentBarang = barang
                        productKodeBarangTv.text = "Kode Barang: ${barang.kdbrg}"
                        productNameTv.text = "Nama Barang: ${barang.nama}"
                        productStockOHTv.text = "Stock OH: ${barang.stock_oh}"
                        productSatuanTv.text = "Satuan: ${barang.satuan}"
                    } else {
                        Toast.makeText(this, "Barang tidak ditemukan", Toast.LENGTH_SHORT).show()
                        //productKodeBarangTv.text = "Kode Barang: -"
                        //productNameTv.text = "Nama Barang: -"
                        //productStockOHTv.text = "Stock OH: -"
                        //productSatuanTv.text = "Satuan: -"
                        blankBarang()
                    }
                }
            }
        }
    }

    private fun blankBarang(){
        //productKodeTv.text =""
        productKodeBarangTv.text = "Kode Barang: -"
        productNameTv.text = "Nama Barang: -"
        productStockOHTv.text = "Stock OH: -"
        productSatuanTv.text = "Satuan: -"
        productKodeTv.setText("")
        productStockOPTv.setText("")

        currentStockId=null
    }


    private fun fetchBarangKdbrg(kdbrg: String) {
        progressBar.visibility = View.VISIBLE
        val lokasi = spinner.selectedItem.toString()
        val warehouse : Int
        warehouse = if (lokasi == "Rak") 1 else 2

        barangRepo.fetchBarangKdbrg(kdbrg,warehouse,"") { result ->
            runOnUiThread {
                progressBar.visibility = View.GONE


                if (result == null||result.barang==null) {
                    Toast.makeText(this, "Barang tidak ditemukan1", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val barang = result.barang
                Log.d("API_DEBUG", "Result body: $result") // 👈 tampil di Logcat

                if(result.alreadyRecorded){
                    AlertDialog.Builder(this)
                        .setTitle("Barang sudah direkam stok")
                        .setMessage("Apakah ingin update data stok ini?")
                        .setPositiveButton("Update") { _, _ ->
                            // Lanjut ke mode update
                            //updateStok(result.stockRecordId!!, barang!!)
                            currentStockId=result.stockRecordId

                            currentBarang = barang
                            productKodeBarangTv.text = "Kode Barang: ${barang.kdbrg}"
                            productNameTv.text = "Nama Barang: ${barang.nama}"
                            productStockOHTv.text = "Stock OH: ${barang.stock_oh}"
                            productSatuanTv.text = "Satuan: ${barang.satuan}"
                            scanResultTv.text = "Hasil Pencarian: ${barang.barcode}"
                        }
                        .setNegativeButton("Cancel"){_,_ ->
                            blankBarang()
                        }
                        .show()
                }else{
                    val barang = result.barang
                    if (barang != null) {

                        currentBarang = barang
                        productKodeBarangTv.text = "Kode Barang: ${barang.kdbrg}"
                        productNameTv.text = "Nama Barang: ${barang.nama}"
                        productStockOHTv.text = "Stock OH: ${barang.stock_oh}"
                        productSatuanTv.text = "Satuan: ${barang.satuan}"
                        scanResultTv.text = "Hasil Pencarian: ${barang.barcode}"
                        currentStockId=null
                    } else {
                        Toast.makeText(this, "Barang tidak ditemukan", Toast.LENGTH_SHORT).show()
                        //productKodeBarangTv.text = "Kode Barang: -"
                        //productNameTv.text = "Nama Barang: -"
                        //productStockOHTv.text = "Stock OH: -"
                        //productSatuanTv.text = "Satuan: -"
                        blankBarang()
                    }
                }
            }
        }
    }

    // Simpan hasil opname
    private fun saveStockOpname() {
        val barang = currentBarang
        if (barang == null) {
            Toast.makeText(this, "Scan barang dulu om 😅", Toast.LENGTH_SHORT).show()
            return
        }

        val qtyOpText = productStockOPTv.text.toString().trim()
        val lokasi = spinner.selectedItem.toString()
        if (qtyOpText.isEmpty() || lokasi == "Pilih Lokasi") {
            Toast.makeText(this, "Isi jumlah opname dan lokasi dulu om", Toast.LENGTH_SHORT).show()
            return
        }

        val opname = StockOpname(
            kdbrg = barang.kdbrg,
            barcode = barang.barcode,
            nama = barang.nama,
            satuan = barang.satuan,
            warehouse = if (lokasi == "Rak") 1 else 2,
            nama_operator = session.getUsername() ?: "Anonim",
            stock_oh = barang.stock_oh,
            stock_op = qtyOpText.toIntOrNull() ?: 0
        )

        progressBar.visibility = View.VISIBLE
        // 👇 kalau currentStockId TIDAK null → update
        if (currentStockId!=null) {
            stockOpnameRepo.updateOpname(currentStockId!!, opname) { success ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (success) {
                        Toast.makeText(this, "♻️ Data opname berhasil diupdate!", Toast.LENGTH_SHORT).show()
                        productStockOPTv.setText("")
                        blankBarang()
                        currentStockId = 0 // reset biar gak nyangkut
                    } else {
                        Toast.makeText(this, "❌ Gagal update opname", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // 👇 kalau currentStockId null → simpan baru
            stockOpnameRepo.saveOpname(opname) { success ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (success) {
                        Toast.makeText(this, "✅ Data opname tersimpan!", Toast.LENGTH_SHORT).show()
                        productStockOPTv.setText("")
                        blankBarang()
                    } else {
                        Toast.makeText(this, "❌ Gagal menyimpan opname", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 📸 Hasil scan barcode
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val barcode = result.contents
            scanResultTv.text = "Hasil scan: $barcode"
            fetchBarang(barcode)
        } else {
            Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

}