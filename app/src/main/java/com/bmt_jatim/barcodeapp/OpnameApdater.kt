package com.bmt_jatim.barcodeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OpnameAdapter(private val dataList: MutableList<OpnameData>) :
    RecyclerView.Adapter<OpnameAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kode: TextView = view.findViewById(R.id.kodeTv)
        val nama: TextView = view.findViewById(R.id.namaTv)
        val qty: TextView = view.findViewById(R.id.qtyTv)
        val lokasi: TextView = view.findViewById(R.id.lokasiTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_opname, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.kode.text = item.kodeBarang
        holder.nama.text = item.namaBarang
        holder.qty.text = "OH: ${item.qtyOH} | OP: ${item.qtyOP}"
        holder.lokasi.text = item.lokasi
    }

    override fun getItemCount(): Int = dataList.size
}
