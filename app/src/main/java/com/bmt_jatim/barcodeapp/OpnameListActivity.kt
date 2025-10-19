package com.bmt_jatim.barcodeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OpnameListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OpnameAdapter
//    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opname_list)

//        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewOpname)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

//        val opnameList = dbHelper.getAllOpnameData() // ambil semua data dari SQLite
//        adapter = OpnameAdapter(opnameList)
//        recyclerView.adapter = adapter
    }
}
