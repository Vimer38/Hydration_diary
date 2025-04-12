package com.example.vkr_healthy_nutrition

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Настройка Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarReg)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Регистрация" // Установка заголовка
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        usernameEditText = findViewById(R.id.username_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        registerButton = findViewById(R.id.register_button)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        registerButton.setOnClickListener {
            registerUser ()
        }
    }

    private fun registerUser () {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                val editor = sharedPreferences.edit()
                editor.putString("username", username) // Сохраняем с ключом "username"
                editor.putString("password", password) // Сохраняем с ключом "password"
                editor.apply()

                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                finish() // Закрываем экран регистрации
            } else {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
        }
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