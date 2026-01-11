package com.example.gudangumkm.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.R
import com.example.gudangumkm.TambahBarangActivity
import com.example.gudangumkm.adapter.BarangAdapter
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class BarangFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var emptyState: LinearLayout

    private var userId: String = ""
    private val barangRepository = BarangRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_barang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        fabAdd = view.findViewById(R.id.fabAdd)
        etSearch = view.findViewById(R.id.etSearch)
        emptyState = view.findViewById(R.id.emptyState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Get userId from SharedPreferences
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("user_id", "") ?: ""
    }

    private fun setupListeners() {
        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), TambahBarangActivity::class.java))
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchData(s.toString())
            }
        })
    }

    private fun loadData() {
        if (userId.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = barangRepository.getAll(userId)) {
                is Result.Success -> {
                    val data = result.data
                    if (data.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE
                        recyclerView.adapter = BarangAdapter(data, userId, barangRepository) {
                            loadData() // Refresh on delete
                        }
                    }
                }
                is Result.Error -> {
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun searchData(query: String) {
        if (userId.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            val result = if (query.isEmpty()) {
                barangRepository.getAll(userId)
            } else {
                barangRepository.search(userId, query)
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
                        recyclerView.adapter = BarangAdapter(data, userId, barangRepository) {
                            loadData()
                        }
                    }
                }
                is Result.Error -> {
                    recyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                }
                is Result.Loading -> {}
            }
        }
    }
}
