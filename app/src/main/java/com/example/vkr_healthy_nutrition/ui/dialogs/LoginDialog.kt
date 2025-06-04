package com.example.vkr_healthy_nutrition.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.vkr_healthy_nutrition.R

class LoginDialog(
    context: Context,
    private val onLogin: (email: String, password: String) -> Unit
) : Dialog(context) {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        cancelButton = findViewById(R.id.cancelButton)

        setupListeners()
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text?.toString() ?: ""
            val password = passwordEditText.text?.toString() ?: ""
            if (email.isNotEmpty() && password.isNotEmpty()) {
                onLogin(email, password)
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
} 