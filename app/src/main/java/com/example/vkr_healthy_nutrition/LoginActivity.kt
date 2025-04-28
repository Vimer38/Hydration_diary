package com.example.vkr_healthy_nutrition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.example.vkr_healthy_nutrition.data.network.LoginRequest
import com.example.vkr_healthy_nutrition.data.repository.AuthRepository
import com.example.vkr_healthy_nutrition.ui.auth.AuthResult
import com.example.vkr_healthy_nutrition.ui.auth.AuthViewModel
import com.example.vkr_healthy_nutrition.ui.auth.AuthViewModelFactory
import com.example.vkr_healthy_nutrition.ThemeManager

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar: Toolbar = findViewById(R.id.toolbarLog)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Авторизация"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.login_progress_bar)

        loginButton.setOnClickListener {
            loginUser()
        }

        viewModel.loginResult.observe(this, Observer { result ->
            when (result) {
                is AuthResult.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    loginButton.isEnabled = false
                }
                is AuthResult.Success -> {
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(this, "Вход успешен!", Toast.LENGTH_SHORT).show()

                    saveAuthToken(result.data.token)

                    // Загружаем цветовую схему пользователя
                    ThemeManager.loadColorSchemeFromServer(this)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> {
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(this, "Ошибка входа: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите email и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.loginUser(LoginRequest(email, password))
    }

    private fun saveAuthToken(token: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}