package com.bmt_jatim.barcodeapp

object OpnameDataStore {
    private val opnameList = mutableListOf<OpnameData>()

    fun add(item: OpnameData) {
        opnameList.add(item)
    }

    fun getAll(): MutableList<OpnameData> {
        return opnameList
    }

    fun clear() {
        opnameList.clear()
    }
}
