package com.example.gudangumkm

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gudangumkm.data.model.Barang
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahBarangActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etNama: TextInputEditText
    private lateinit var actvKategori: AutoCompleteTextView
    private lateinit var etStok: TextInputEditText
    private lateinit var etStokMin: TextInputEditText
    private lateinit var etTglProduksi: TextInputEditText
    private lateinit var etTglExpired: TextInputEditText
    private lateinit var etDeskripsi: TextInputEditText
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBatal: MaterialButton
    private lateinit var tilTglProduksi: TextInputLayout
    private lateinit var tilTglExpired: TextInputLayout

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    private val kategoriList = listOf(
        "Elektronik", "Makanan", "Minuman", "Pakaian", "Alat Tulis", "Lainnya"
    )

    private var userId: String = ""
    private val barangRepository = BarangRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_barang)

        // Get userId from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("user_id", "") ?: ""

        initViews()
        setupKategoriDropdown()
        setupDatePickers()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etNama = findViewById(R.id.etNama)
        actvKategori = findViewById(R.id.actvKategori)
        etStok = findViewById(R.id.etStok)
        etStokMin = findViewById(R.id.etStokMin)
        etTglProduksi = findViewById(R.id.etTglProduksi)
        etTglExpired = findViewById(R.id.etTglExpired)
        etDeskripsi = findViewById(R.id.etDeskripsi)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBatal = findViewById(R.id.btnBatal)
        tilTglProduksi = findViewById(R.id.tilTglProduksi)
        tilTglExpired = findViewById(R.id.tilTglExpired)
    }

    private fun setupKategoriDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriList)
        actvKategori.setAdapter(adapter)
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        etTglProduksi.setOnClickListener {
            showDatePicker(calendar) { selectedDate ->
                etTglProduksi.setText(displayFormat.format(selectedDate.time))
                etTglProduksi.tag = dateFormat.format(selectedDate.time)
            }
        }

        tilTglProduksi.setEndIconOnClickListener {
            showDatePicker(calendar) { selectedDate ->
                etTglProduksi.setText(displayFormat.format(selectedDate.time))
                etTglProduksi.tag = dateFormat.format(selectedDate.time)
            }
        }

        etTglExpired.setOnClickListener {
            showDatePicker(calendar) { selectedDate ->
                etTglExpired.setText(displayFormat.format(selectedDate.time))
                etTglExpired.tag = dateFormat.format(selectedDate.time)
            }
        }

        tilTglExpired.setEndIconOnClickListener {
            showDatePicker(calendar) { selectedDate ->
                etTglExpired.setText(displayFormat.format(selectedDate.time))
                etTglExpired.tag = dateFormat.format(selectedDate.time)
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
        btnBatal.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val kategori = actvKategori.text.toString().trim()
            val stokStr = etStok.text.toString().trim()
            val stokMinStr = etStokMin.text.toString().trim()
            val tglProduksi = etTglProduksi.tag?.toString() ?: ""
            val tglExpired = etTglExpired.tag?.toString() ?: ""
            val deskripsi = etDeskripsi.text.toString().trim()

            // Validation
            if (nama.isEmpty()) {
                etNama.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }
            if (stokStr.isEmpty()) {
                etStok.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }

            val stok = stokStr.toIntOrNull() ?: 0
            val stokMin = stokMinStr.toIntOrNull() ?: 5

            // Insert to Firestore
            val barang = Barang(
                userId = userId,
                nama = nama,
                kategori = kategori,
                stok = stok,
                stokMinimum = stokMin,
                tanggalProduksi = tglProduksi,
                tanggalExpired = tglExpired,
                deskripsi = deskripsi
            )
            
            btnSimpan.isEnabled = false
            lifecycleScope.launch {
                when (val result = barangRepository.insert(userId, barang)) {
                    is Result.Success -> {
                        Toast.makeText(this@TambahBarangActivity, getString(R.string.msg_simpan_sukses), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@TambahBarangActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                    }
                    is Result.Loading -> { }
                }
            }
        }
    }
}
