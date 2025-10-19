package com.bmt_jatim.barcodeapp

data class OpnameData(
    val kodeBarang: String,
    val barcode: String,
    val namaBarang: String,
    val qtyOH: Number,
    val qtyOP: Number,
    val lokasi: String
)