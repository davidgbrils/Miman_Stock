package com.example.gudangumkm.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gudangumkm.LoginActivity
import com.example.gudangumkm.MonitoringActivity
import com.example.gudangumkm.R
import com.example.gudangumkm.StokKeluarActivity
import com.example.gudangumkm.StokMasukActivity
import com.example.gudangumkm.data.repository.BarangRepository
import com.example.gudangumkm.data.repository.Result
import com.example.gudangumkm.util.ThemeHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvTotalJenis: TextView
    private lateinit var tvTotalStok: TextView
    private lateinit var tvStokMenipis: TextView
    private lateinit var tvMendekatExpired: TextView
    private lateinit var btnLogout: ImageButton
    private lateinit var btnTheme: ImageButton
    private lateinit var cardManajemenBarang: CardView
    private lateinit var cardStokMasuk: CardView
    private lateinit var cardStokKeluar: CardView
    private lateinit var cardMonitoring: CardView
    private lateinit var cardStokMenipisClick: CardView
    private lateinit var cardMendekatExpiredClick: CardView

    private var userId: String = ""
    private val barangRepository = BarangRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun initViews(view: View) {
        tvUsername = view.findViewById(R.id.tvUsername)
        tvTotalJenis = view.findViewById(R.id.tvTotalJenis)
        tvTotalStok = view.findViewById(R.id.tvTotalStok)
        tvStokMenipis = view.findViewById(R.id.tvStokMenipis)
        tvMendekatExpired = view.findViewById(R.id.tvMendekatExpired)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnTheme = view.findViewById(R.id.btnTheme)
        cardManajemenBarang = view.findViewById(R.id.cardManajemenBarang)
        cardStokMasuk = view.findViewById(R.id.cardStokMasuk)
        cardStokKeluar = view.findViewById(R.id.cardStokKeluar)
        cardMonitoring = view.findViewById(R.id.cardMonitoring)
        cardStokMenipisClick = view.findViewById(R.id.cardStokMenipis)
        cardMendekatExpiredClick = view.findViewById(R.id.cardMendekatExpired)
        
        // Set initial theme icon
        updateThemeIcon()
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logout()
        }

        btnTheme.setOnClickListener {
            ThemeHelper.toggleTheme(requireContext())
            // Activity will be recreated automatically
        }

        cardManajemenBarang.setOnClickListener {
            // Navigate to Barang tab
            (activity as? com.example.gudangumkm.MainActivity)?.navigateToTab(R.id.nav_barang)
        }

        cardStokMasuk.setOnClickListener {
            startActivity(Intent(requireContext(), StokMasukActivity::class.java))
        }

        cardStokKeluar.setOnClickListener {
            startActivity(Intent(requireContext(), StokKeluarActivity::class.java))
        }

        cardMonitoring.setOnClickListener {
            startActivity(Intent(requireContext(), MonitoringActivity::class.java))
        }

        cardStokMenipisClick.setOnClickListener {
            val intent = Intent(requireContext(), MonitoringActivity::class.java)
            intent.putExtra("filter", "menipis")
            startActivity(intent)
        }

        cardMendekatExpiredClick.setOnClickListener {
            val intent = Intent(requireContext(), MonitoringActivity::class.java)
            intent.putExtra("filter", "expired")
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Pengguna") ?: "Pengguna"
        userId = prefs.getString("user_id", "") ?: ""
        tvUsername.text = userName
    }

    private fun loadStats() {
        if (userId.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            // Total jenis barang
            when (val result = barangRepository.getCount(userId)) {
                is Result.Success -> tvTotalJenis.text = result.data.toString()
                is Result.Error -> tvTotalJenis.text = "0"
                is Result.Loading -> {}
            }

            // Total stok
            when (val result = barangRepository.getTotalStok(userId)) {
                is Result.Success -> tvTotalStok.text = result.data.toString()
                is Result.Error -> tvTotalStok.text = "0"
                is Result.Loading -> {}
            }

            // Stok menipis
            when (val result = barangRepository.getCountStokMenipis(userId)) {
                is Result.Success -> tvStokMenipis.text = result.data.toString()
                is Result.Error -> tvStokMenipis.text = "0"
                is Result.Loading -> {}
            }

            // Mendekati expired (within 30 days)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 30)
            val thirtyDaysLater = dateFormat.format(calendar.time)
            
            when (val result = barangRepository.getMendekatExpired(userId, thirtyDaysLater)) {
                is Result.Success -> tvMendekatExpired.text = result.data.size.toString()
                is Result.Error -> tvMendekatExpired.text = "0"
                is Result.Loading -> {}
            }
        }
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    
    private fun updateThemeIcon() {
        if (ThemeHelper.isDarkMode(requireContext())) {
            btnTheme.setImageResource(R.drawable.ic_light_mode)
        } else {
            btnTheme.setImageResource(R.drawable.ic_dark_mode)
        }
    }
}
