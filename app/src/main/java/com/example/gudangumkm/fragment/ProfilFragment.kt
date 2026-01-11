package com.example.gudangumkm.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gudangumkm.LoginActivity
import com.example.gudangumkm.R
import com.google.android.material.button.MaterialButton

class ProfilFragment : Fragment() {

    private lateinit var tvNama: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvPhone: TextView
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        loadUserData()
    }

    private fun initViews(view: View) {
        tvNama = view.findViewById(R.id.tvNama)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvPhone = view.findViewById(R.id.tvPhone)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        val nama = prefs.getString("user_name", "Pengguna") ?: "Pengguna"
        val email = prefs.getString("user_email", "-") ?: "-"
        val username = prefs.getString("user_username", "-") ?: "-"
        val phone = prefs.getString("user_phone", "-") ?: "-"

        tvNama.text = nama
        tvEmail.text = if (email.isNotEmpty()) email else "-"
        tvUsername.text = username
        tvPhone.text = if (phone.isNotEmpty()) phone else "-"
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
