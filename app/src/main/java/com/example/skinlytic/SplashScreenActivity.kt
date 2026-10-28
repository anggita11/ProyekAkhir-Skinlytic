package com.example.skinlytic

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Memulai coroutine untuk menjalankan tugas secara asynchronous.
        lifecycleScope.launch {
            // delay selama 1.2 detik untuk menampilkan Splash Screen sebelum lanjut.
            delay(1500)

            val i = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(i)

            // Menutup Splash Screen Activity setelah membuka LoginActivity.
            finish()
        }
    }
}