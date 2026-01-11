package com.example.gudangumkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gudangumkm.data.repository.Result
import com.example.gudangumkm.data.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUser: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegister: TextView

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if already logged in
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val loggedInUserId = prefs.getString("user_id", null)
        if (loggedInUserId != null) {
            navigateToMain()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etUser = findViewById(R.id.etUser)
        etPass = findViewById(R.id.etPass)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString().trim()

            // Validation
            if (username.isEmpty()) {
                etUser.error = "Username harus diisi"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPass.error = "Password harus diisi"
                return@setOnClickListener
            }

            // Disable button while loading
            btnLogin.isEnabled = false
            btnLogin.text = "Loading..."

            // Login with coroutine
            lifecycleScope.launch {
                when (val result = userRepository.login(username, password)) {
                    is Result.Success -> {
                        val user = result.data
                        // Save user session
                        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("user_id", user.id)
                            .putString("user_name", user.nama.ifEmpty { user.username })
                            .putString("user_username", user.username)
                            .putString("user_email", user.email)
                            .putString("user_phone", user.phone)
                            .apply()

                        Toast.makeText(this@LoginActivity, getString(R.string.msg_login_sukses), Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                        btnLogin.isEnabled = true
                        btnLogin.text = getString(R.string.btn_login)
                    }
                    is Result.Loading -> {
                        // Already handled with button state
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}