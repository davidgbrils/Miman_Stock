package com.example.gudangumkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.R
import com.example.gudangumkm.data.model.Transaksi
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TransaksiAdapter(
    private val list: List<Transaksi>,
    private val userId: String,
    private val barangRepository: BarangRepository,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconContainer: FrameLayout = view.findViewById(R.id.iconContainer)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvNamaBarang: TextView = view.findViewById(R.id.tvNamaBarang)
        val tvKeterangan: TextView = view.findViewById(R.id.tvKeterangan)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val tvTipe: TextView = view.findViewById(R.id.tvTipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = list[position]
        val context = holder.itemView.context

        // Get barang name asynchronously
        holder.tvNamaBarang.text = "Loading..."
        CoroutineScope(Dispatchers.IO).launch {
            val result = barangRepository.getById(userId, transaksi.barangId)
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        holder.tvNamaBarang.text = result.data?.nama ?: "Unknown"
                    }
                    is Result.Error -> {
                        holder.tvNamaBarang.text = "Unknown"
                    }
                    is Result.Loading -> { }
                }
            }
        }

        // Keterangan
        holder.tvKeterangan.text = transaksi.keterangan.ifEmpty { "-" }

        // Date formatting
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(transaksi.tanggal)
            holder.tvTanggal.text = if (date != null) outputFormat.format(date) else transaksi.tanggal
        } catch (e: Exception) {
            holder.tvTanggal.text = transaksi.tanggal
        }

        // Set tipe and styling
        if (transaksi.tipe == "MASUK") {
            holder.tvJumlah.text = "+${transaksi.jumlah}"
            holder.tvJumlah.setTextColor(ContextCompat.getColor(context, R.color.success))
            holder.tvTipe.text = "MASUK"
            holder.iconContainer.setBackgroundResource(R.drawable.bg_menu_icon_blue)
            holder.ivIcon.setImageResource(R.drawable.ic_stok_masuk)
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.info))
        } else {
            holder.tvJumlah.text = "-${transaksi.jumlah}"
            holder.tvJumlah.setTextColor(ContextCompat.getColor(context, R.color.error))
            holder.tvTipe.text = "KELUAR"
            holder.iconContainer.setBackgroundResource(R.drawable.bg_menu_icon_orange)
            holder.ivIcon.setImageResource(R.drawable.ic_stok_keluar)
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondary))
        }
    }
}
