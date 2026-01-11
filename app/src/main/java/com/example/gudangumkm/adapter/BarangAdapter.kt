package com.example.gudangumkm.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.EditBarangActivity
import com.example.gudangumkm.R
import com.example.gudangumkm.data.model.Barang
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BarangAdapter(
    private val list: List<Barang>,
    private val userId: String,
    private val barangRepository: BarangRepository,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<BarangAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvKategori: TextView = view.findViewById(R.id.tvKategori)
        val tvStok: TextView = view.findViewById(R.id.tvStok)
        val tvExpired: TextView = view.findViewById(R.id.tvExpired)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barang, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val barang = list[position]
        val context = holder.itemView.context

        // Set data
        holder.tvNama.text = barang.nama
        holder.tvKategori.text = barang.kategori.ifEmpty { "Umum" }
        holder.tvStok.text = "Stok: ${barang.stok}"

        // Set stock badge color based on level
        when {
            barang.stok <= barang.stokMinimum -> {
                holder.tvStok.setBackgroundResource(R.drawable.bg_badge_danger)
            }
            barang.stok <= barang.stokMinimum * 2 -> {
                holder.tvStok.setBackgroundResource(R.drawable.bg_badge_warning)
            }
            else -> {
                holder.tvStok.setBackgroundResource(R.drawable.bg_badge_success)
            }
        }

        // Check expired date
        if (barang.tanggalExpired.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displayFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                val expiredDate = dateFormat.parse(barang.tanggalExpired)
                val today = Calendar.getInstance().time

                if (expiredDate != null) {
                    val daysUntilExpired = ((expiredDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                    
                    holder.tvExpired.visibility = View.VISIBLE
                    holder.tvExpired.text = "Exp: ${displayFormat.format(expiredDate)}"
                    
                    when {
                        daysUntilExpired < 0 -> {
                            holder.tvExpired.setBackgroundResource(R.drawable.bg_badge_danger)
                        }
                        daysUntilExpired <= 30 -> {
                            holder.tvExpired.setBackgroundResource(R.drawable.bg_badge_warning)
                        }
                        else -> {
                            holder.tvExpired.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                holder.tvExpired.visibility = View.GONE
            }
        } else {
            holder.tvExpired.visibility = View.GONE
        }

        // Click to edit
        holder.itemView.setOnClickListener {
            val intent = Intent(context, EditBarangActivity::class.java)
            intent.putExtra("id", barang.id)
            context.startActivity(intent)
        }

        // Long click to delete
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_hapus_title))
                .setMessage("Apakah Anda yakin ingin menghapus ${barang.nama}?")
                .setPositiveButton(context.getString(R.string.dialog_ya)) { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = barangRepository.delete(userId, barang.id)
                        withContext(Dispatchers.Main) {
                            when (result) {
                                is Result.Success -> {
                                    onDataChanged()
                                }
                                is Result.Error -> {
                                    Toast.makeText(context, "Gagal menghapus: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                                is Result.Loading -> { }
                            }
                        }
                    }
                }
                .setNegativeButton(context.getString(R.string.dialog_tidak), null)
                .show()
            true
        }
    }
}
