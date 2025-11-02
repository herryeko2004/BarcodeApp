package com.bmt_jatim.barcodeapp.model

data class BarangResponse(
    val barang: Barang?,
    val alreadyRecorded: Boolean = false,
    val stockRecordId: Int? = null
)
