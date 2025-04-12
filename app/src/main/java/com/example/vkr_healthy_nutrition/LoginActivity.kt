package com.example.vkr_healthy_nutrition

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar: Toolbar = findViewById(R.id.toolbarLog)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Авторизация"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        usernameEditText = findViewById(R.id.username_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById(R.id.login_button)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        loginButton.setOnClickListener {
            loginUser ()
        }
    }

    private fun loginUser () {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Получаем сохраненные данные с правильными ключами
        val savedUsername = sharedPreferences.getString("username", null)
        val savedPassword = sharedPreferences.getString("password", null)

        // Проверяем введенные данные
        if (username == savedUsername && password == savedPassword) {
            Toast.makeText(this, "Вход успешен!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Закрываем экран входа
        } else {
            Toast.makeText(this, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
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