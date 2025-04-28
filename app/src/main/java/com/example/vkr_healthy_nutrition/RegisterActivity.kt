package com.example.vkr_healthy_nutrition

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
import com.example.vkr_healthy_nutrition.data.network.RegisterRequest
import com.example.vkr_healthy_nutrition.data.repository.AuthRepository
import com.example.vkr_healthy_nutrition.ui.auth.AuthResult
import com.example.vkr_healthy_nutrition.ui.auth.AuthViewModel
import com.example.vkr_healthy_nutrition.ui.auth.AuthViewModelFactory
import com.example.vkr_healthy_nutrition.ThemeManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbarReg)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Регистрация"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        usernameEditText = findViewById(R.id.username_edit_text)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        registerButton = findViewById(R.id.register_button)
        progressBar = findViewById(R.id.register_progress_bar)

        registerButton.setOnClickListener {
            registerUser()
        }

        viewModel.registerResult.observe(this, Observer { result ->
            when (result) {
                is AuthResult.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    registerButton.isEnabled = false
                }
                is AuthResult.Success -> {
                    progressBar.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()

                    // Устанавливаем цветовую схему по умолчанию для нового пользователя
                    ThemeManager.setColorScheme(this, ThemeManager.COLOR_SCHEME_DEFAULT)

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> {
                    progressBar.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Ошибка регистрации: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Пожалуйста, введите корректный email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.registerUser(RegisterRequest(username, email, password))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}