package com.example.gudangumkm

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.adapter.BarangAdapter
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import kotlinx.coroutines.launch

class BarangListActivity : AppCompatActivity() {

    private val barangRepository = BarangRepository()
    private var userId: String = ""

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_barang_list)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("user_id", "") ?: ""

        findViewById<Button>(R.id.btnTambah).setOnClickListener {
            startActivity(Intent(this, TambahBarangActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
    
    private fun loadData() {
        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            when (val result = barangRepository.getAll(userId)) {
                is Result.Success -> {
                    rv.adapter = BarangAdapter(result.data, userId, barangRepository) { loadData() }
                }
                is Result.Error -> {
                    Toast.makeText(this@BarangListActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> { }
            }
        }
    }
}
