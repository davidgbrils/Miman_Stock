package com.example.gudangumkm

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gudangumkm.data.model.User
import com.example.gudangumkm.data.repository.Result
import com.example.gudangumkm.data.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etUser: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var etConfirmPass: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnLogin: TextView
    private lateinit var btnBack: ImageButton

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etUser = findViewById(R.id.etUser)
        etPass = findViewById(R.id.etPass)
        etConfirmPass = findViewById(R.id.etConfirmPass)
        etPhone = findViewById(R.id.etPhone)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnLogin = findViewById(R.id.btnLogin)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString().trim()
            val confirmPassword = etConfirmPass.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            // Validation
            if (nama.isEmpty()) {
                etNama.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                etUser.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPass.error = getString(R.string.msg_field_required)
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPass.error = getString(R.string.msg_password_mismatch)
                return@setOnClickListener
            }

            // Disable button while loading
            btnSimpan.isEnabled = false
            btnSimpan.text = "Loading..."

            // Register with coroutine
            lifecycleScope.launch {
                val newUser = User(
                    nama = nama,
                    email = email,
                    username = username,
                    password = password,
                    phone = phone
                )

                when (val result = userRepository.register(newUser)) {
                    is Result.Success -> {
                        Toast.makeText(this@RegisterActivity, getString(R.string.msg_register_sukses), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_SHORT).show()
                        btnSimpan.isEnabled = true
                        btnSimpan.text = getString(R.string.btn_register)
                    }
                    is Result.Loading -> {
                        // Already handled with button state
                    }
                }
            }
        }
    }
}
