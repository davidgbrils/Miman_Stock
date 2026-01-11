package com.example.gudangumkm

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.adapter.BarangAdapter
import com.example.gudangumkm.data.model.Barang
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonitoringActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var chipSemua: Chip
    private lateinit var chipStokMenipis: Chip
    private lateinit var chipExpired: Chip
    private lateinit var tvCountMenipis: TextView
    private lateinit var tvCountExpired: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout

    private var currentFilter = "SEMUA"
    private var userId: String = ""
    private val barangRepository = BarangRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring)

        // Get userId from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("user_id", "") ?: ""

        initViews()
        setupListeners()
        loadSummary()

        // Check if filter was passed
        val filter = intent.getStringExtra("filter")
        when (filter) {
            "menipis" -> {
                currentFilter = "MENIPIS"
                chipStokMenipis.isChecked = true
            }
            "expired" -> {
                currentFilter = "EXPIRED"
                chipExpired.isChecked = true
            }
        }

        loadData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        chipSemua = findViewById(R.id.chipSemua)
        chipStokMenipis = findViewById(R.id.chipStokMenipis)
        chipExpired = findViewById(R.id.chipExpired)
        tvCountMenipis = findViewById(R.id.tvCountMenipis)
        tvCountExpired = findViewById(R.id.tvCountExpired)
        recyclerView = findViewById(R.id.recyclerView)
        emptyState = findViewById(R.id.emptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        chipSemua.setOnClickListener {
            currentFilter = "SEMUA"
            loadData()
        }

        chipStokMenipis.setOnClickListener {
            currentFilter = "MENIPIS"
            loadData()
        }

        chipExpired.setOnClickListener {
            currentFilter = "EXPIRED"
            loadData()
        }
    }

    private fun loadSummary() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        val thirtyDaysLater = dateFormat.format(calendar.time)
        
        lifecycleScope.launch {
            // Stok menipis count
            when (val result = barangRepository.getStokMenipis(userId)) {
                is Result.Success -> {
                    tvCountMenipis.text = result.data.size.toString()
                }
                is Result.Error -> {
                    tvCountMenipis.text = "0"
                }
                is Result.Loading -> { }
            }

            // Mendekati expired count
            when (val result = barangRepository.getMendekatExpired(userId, thirtyDaysLater)) {
                is Result.Success -> {
                    tvCountExpired.text = result.data.size.toString()
                }
                is Result.Error -> {
                    tvCountExpired.text = "0"
                }
                is Result.Loading -> { }
            }
        }
    }

    private fun loadData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        val thirtyDaysLater = dateFormat.format(calendar.time)

        lifecycleScope.launch {
            val data: List<Barang> = when (currentFilter) {
                "MENIPIS" -> {
                    when (val result = barangRepository.getStokMenipis(userId)) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            Toast.makeText(this@MonitoringActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                            emptyList()
                        }
                        is Result.Loading -> emptyList()
                    }
                }
                "EXPIRED" -> {
                    when (val result = barangRepository.getMendekatExpired(userId, thirtyDaysLater)) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            Toast.makeText(this@MonitoringActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                            emptyList()
                        }
                        is Result.Loading -> emptyList()
                    }
                }
                else -> {
                    // Combine both lists
                    val menipis = when (val result = barangRepository.getStokMenipis(userId)) {
                        is Result.Success -> result.data
                        else -> emptyList()
                    }
                    val expired = when (val result = barangRepository.getMendekatExpired(userId, thirtyDaysLater)) {
                        is Result.Success -> result.data
                        else -> emptyList()
                    }
                    (menipis + expired).distinctBy { it.id }
                }
            }

            if (data.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyState.visibility = View.GONE
                recyclerView.adapter = BarangAdapter(data, userId, barangRepository) { loadData() }
            }
        }
    }
}
