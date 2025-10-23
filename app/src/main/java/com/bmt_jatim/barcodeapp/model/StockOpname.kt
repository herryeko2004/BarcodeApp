package com.bmt_jatim.barcodeapp.model

import android.nfc.tech.NfcBarcode

data class StockOpname(
    val kdbrg: String,
    val barcode: String,
    val nama: String,
    val satuan: String,
    val warehouse: Int,
    val nama_operator: String,
    val stock_oh: Int,
    val stock_op: Int
)
