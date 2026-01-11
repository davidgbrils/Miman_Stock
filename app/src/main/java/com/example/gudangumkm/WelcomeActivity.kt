package com.example.gudangumkm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.btnStok)
            .setOnClickListener {
                startActivity(Intent(this, BarangListActivity::class.java))
            }
    }
}
