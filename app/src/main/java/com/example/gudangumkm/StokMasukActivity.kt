package com.example.gudangumkm

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gudangumkm.data.model.Barang
import com.example.gudangumkm.data.model.Transaksi
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.TransaksiRepository
import com.example.gudangumkm.data.repository.Result
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StokMasukActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var actvBarang: AutoCompleteTextView
    private lateinit var layoutStokInfo: LinearLayout
    private lateinit var tvStokSaatIni: TextView
    private lateinit var etJumlah: TextInputEditText
    private lateinit var etTanggal: TextInputEditText
    private lateinit var etKeterangan: TextInputEditText
    private lateinit var btnSimpan: MaterialButton
    private lateinit var tilTanggal: TextInputLayout

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    private var barangList: List<Barang> = emptyList()
    private var selectedBarang: Barang? = null
    private var userId: String = ""
    
    private val barangRepository = BarangRepository()
    private val transaksiRepository = TransaksiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stok_masuk)

        // Get userId from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("user_id", "") ?: ""

        initViews()
        loadBarangList()
        setupDatePicker()
        setupListeners()
        setDefaultDate()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        actvBarang = findViewById(R.id.actvBarang)
        layoutStokInfo = findViewById(R.id.layoutStokInfo)
        tvStokSaatIni = findViewById(R.id.tvStokSaatIni)
        etJumlah = findViewById(R.id.etJumlah)
        etTanggal = findViewById(R.id.etTanggal)
        etKeterangan = findViewById(R.id.etKeterangan)
        btnSimpan = findViewById(R.id.btnSimpan)
        tilTanggal = findViewById(R.id.tilTanggal)
    }

    private fun loadBarangList() {
        lifecycleScope.launch {
            when (val result = barangRepository.getAll(userId)) {
                is Result.Success -> {
                    barangList = result.data
                    val barangNames = barangList.map { it.nama }
                    val adapter = ArrayAdapter(this@StokMasukActivity, android.R.layout.simple_dropdown_item_1line, barangNames)
                    actvBarang.setAdapter(adapter)

                    actvBarang.setOnItemClickListener { _, _, position, _ ->
                        selectedBarang = barangList[position]
                        layoutStokInfo.visibility = View.VISIBLE
                        tvStokSaatIni.text = selectedBarang?.stok.toString()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this@StokMasukActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> { }
            }
        }
    }

    private fun setDefaultDate() {
        val today = Calendar.getInstance()
        etTanggal.setText(displayFormat.format(today.time))
        etTanggal.tag = dateFormat.format(today.time)
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        etTanggal.setOnClickListener {
            showDatePicker(calendar) { selectedDate ->
                etTanggal.setText(displayFormat.format(selectedDate.time))
                etTanggal.tag = dateFormat.format(selectedDate.time)
            }
        }

        tilTanggal.setEndIconOnClickListener {
            showDatePicker(calendar) { selectedDate ->
                etTanggal.setText(displayFormat.format(selectedDate.time))
                etTanggal.tag = dateFormat.format(selectedDate.time)
            }
        }
    }

    private fun showDatePicker(calendar: Calendar, onDateSelected: (Calendar) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val jumlahStr = etJumlah.text.toString().trim()
            val tanggal = etTanggal.tag?.toString() ?: ""
            val keterangan = etKeterangan.text.toString().trim()

            // Validation
            if (selectedBarang == null) {
                Toast.makeText(this, "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jumlahStr.isEmpty()) {
                etJumlah.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }

            val jumlah = jumlahStr.toIntOrNull() ?: 0
            if (jumlah <= 0) {
                etJumlah.error = "Jumlah harus lebih dari 0"
                return@setOnClickListener
            }

            btnSimpan.isEnabled = false
            lifecycleScope.launch {
                // Add stock using Firestore transaction
                when (val result = barangRepository.tambahStok(userId, selectedBarang!!.id, jumlah)) {
                    is Result.Success -> {
                        // Insert transaction record
                        val transaksi = Transaksi(
                            userId = userId,
                            barangId = selectedBarang!!.id,
                            tipe = "MASUK",
                            jumlah = jumlah,
                            tanggal = tanggal,
                            keterangan = keterangan
                        )
                        
                        when (val transaksiResult = transaksiRepository.insert(userId, transaksi)) {
                            is Result.Success -> {
                                Toast.makeText(this@StokMasukActivity, getString(R.string.msg_simpan_sukses), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            is Result.Error -> {
                                Toast.makeText(this@StokMasukActivity, "Error: ${transaksiResult.exception?.message}", Toast.LENGTH_SHORT).show()
                                btnSimpan.isEnabled = true
                            }
                            is Result.Loading -> { }
                        }
                    }
                    is Result.Error -> {
                        Toast.makeText(this@StokMasukActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                    }
                    is Result.Loading -> { }
                }
            }
        }
    }
}
