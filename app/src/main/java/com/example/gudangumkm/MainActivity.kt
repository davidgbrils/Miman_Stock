package com.example.gudangumkm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gudangumkm.fragment.BarangFragment
import com.example.gudangumkm.fragment.HomeFragment
import com.example.gudangumkm.fragment.ProfilFragment
import com.example.gudangumkm.fragment.TransaksiFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Bottom navigation listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_barang -> {
                    loadFragment(BarangFragment())
                    true
                }
                R.id.nav_transaksi -> {
                    loadFragment(TransaksiFragment())
                    true
                }
                R.id.nav_profil -> {
                    loadFragment(ProfilFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Method to navigate to specific tab from fragments
    fun navigateToTab(tabId: Int) {
        bottomNavigation.selectedItemId = tabId
    }
}
