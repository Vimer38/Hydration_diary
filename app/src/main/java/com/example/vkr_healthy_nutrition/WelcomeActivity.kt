package com.example.vkr_healthy_nutrition

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_button)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Проверяем, есть ли сохраненные данные о пользователе
        val savedUsername = sharedPreferences.getString("username", null)


        if (savedUsername != null) {
            // Если пользователь уже зарегистрирован, переходим на главный экран
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

}