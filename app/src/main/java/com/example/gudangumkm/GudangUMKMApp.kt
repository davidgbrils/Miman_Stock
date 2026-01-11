package com.example.gudangumkm

import android.app.Application
import com.example.gudangumkm.util.ThemeHelper
import com.google.firebase.FirebaseApp

class GudangUMKMApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Apply saved theme
        ThemeHelper.applyTheme(this)
    }
}
