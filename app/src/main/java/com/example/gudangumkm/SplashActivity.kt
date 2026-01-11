package com.example.gudangumkm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Animate the splash screen elements
        val logoContainer = findViewById<android.widget.LinearLayout>(R.id.logoContainer)
        val loadingContainer = findViewById<android.widget.LinearLayout>(R.id.loadingContainer)

        // Fade in animation
        logoContainer.alpha = 0f
        loadingContainer.alpha = 0f

        logoContainer.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(200)
            .start()

        loadingContainer.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(600)
            .start()

        // Navigate to Login after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}