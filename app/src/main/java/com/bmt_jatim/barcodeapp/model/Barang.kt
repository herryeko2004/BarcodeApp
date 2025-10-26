package com.bmt_jatim.barcodeapp.model

//berdasarkan api resonse
data class Barang(
    val kdbrg: String,
    val barcode: String,
    val nama: String,
    val satuan: String,
    val stock_oh: Int
)
