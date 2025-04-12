package com.example.vkr_healthy_nutrition

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val appBar: Toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(appBar)

        supportActionBar?.title = "Напоминания"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Обработка нажатия на кнопку "Назад" в AppBar
        if (item.itemId == android.R.id.home) {
            // Возврат на главную активность
            finish() // Закрываем текущую активность
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}