package com.example.gudangumkm.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.R
import com.example.gudangumkm.adapter.TransaksiAdapter
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.TransaksiRepository
import com.example.gudangumkm.data.repository.Result
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class TransaksiFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var chipSemua: Chip
    private lateinit var chipMasuk: Chip
    private lateinit var chipKeluar: Chip

    private var currentFilter = "SEMUA"
    private var userId: String = ""
    
    private val transaksiRepository = TransaksiRepository()
    private val barangRepository = BarangRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaksi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get userId from SharedPreferences
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("user_id", "") ?: ""
        
        initViews(view)
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyState = view.findViewById(R.id.emptyState)
        chipSemua = view.findViewById(R.id.chipSemua)
        chipMasuk = view.findViewById(R.id.chipMasuk)
        chipKeluar = view.findViewById(R.id.chipKeluar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        chipSemua.setOnClickListener {
            currentFilter = "SEMUA"
            loadData()
        }

        chipMasuk.setOnClickListener {
            currentFilter = "MASUK"
            loadData()
        }

        chipKeluar.setOnClickListener {
            currentFilter = "KELUAR"
            loadData()
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = when (currentFilter) {
                "MASUK" -> transaksiRepository.getByTipe(userId, "MASUK")
                "KELUAR" -> transaksiRepository.getByTipe(userId, "KELUAR")
                else -> transaksiRepository.getAll(userId)
            }
            
            when (result) {
                is Result.Success -> {
                    val data = result.data
                    if (data.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE
                        recyclerView.adapter = TransaksiAdapter(data, userId, barangRepository) { loadData() }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                }
                is Result.Loading -> { }
            }
        }
    }
}
