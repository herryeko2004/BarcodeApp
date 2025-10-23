package com.bmt_jatim.barcodeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bmt_jatim.barcodeapp.ui.OpnameAdapter
import com.google.android.material.appbar.MaterialToolbar
class OpnameListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OpnameAdapter
//    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opname_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish() // kembali ke MainActivity (menu scan)
        }


//        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewOpname)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // contoh ambil data dari static list / penyimpanan lokal
        adapter = OpnameAdapter(OpnameDataStore.getAll())
        recyclerView.adapter = adapter

        val opnameList = OpnameDataStore.getAll()
        adapter = OpnameAdapter(opnameList)
        recyclerView.adapter = adapter

//        val opnameList = dbHelper.getAllOpnameData() // ambil semua data dari SQLite
//        adapter = OpnameAdapter(opnameList)
//        recyclerView.adapter = adapter
    }
}
