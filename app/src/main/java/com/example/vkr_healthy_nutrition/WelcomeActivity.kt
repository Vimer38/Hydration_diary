package com.example.vkr_healthy_nutrition

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.dialogs.LoginDialog
import com.example.vkr_healthy_nutrition.dialogs.RegisterDialog
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private val userRepository by lazy { (application as HealthyNutritionApp).userRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Проверяем, авторизован ли пользователь
        if (userRepository.isUserLoggedIn) {
            startMainActivity()
            return
        }

        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_button)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            showLoginDialog()
        }

        registerButton.setOnClickListener {
            showRegisterDialog()
        }
    }

    private fun showLoginDialog() {
        LoginDialog(this) { email, password ->
            lifecycleScope.launch {
                userRepository.signIn(email, password)
                    .onSuccess {
                        startMainActivity()
                    }
                    .onFailure {
                        val errorMessage = when (it) {
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Ошибка входа: Пользователь не найден."
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Ошибка входа: Неверный пароль."
                            else -> "Ошибка входа: ${it.message ?: "Неизвестная ошибка"}"
                        }
                        Toast.makeText(this@WelcomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        }.show()
    }

    private fun showRegisterDialog() {
        RegisterDialog(this) { email, password ->
            lifecycleScope.launch {
                userRepository.signUp(email, password)
                    .onSuccess {
                        startMainActivity()
                    }
                    .onFailure {
                         val errorMessage = when (it) {
                            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Ошибка регистрации: Пользователь с таким email уже существует."
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Ошибка регистрации: Некорректный email или пароль."
                            else -> "Ошибка регистрации: ${it.message ?: "Неизвестная ошибка"}"
                        }
                        Toast.makeText(this@WelcomeActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        }.show()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}