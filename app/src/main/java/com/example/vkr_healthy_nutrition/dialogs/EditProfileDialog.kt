package com.example.vkr_healthy_nutrition.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import com.example.vkr_healthy_nutrition.R

class EditProfileDialog(
    context: Context,
    private val currentName: String?,
    private val onSave: (String) -> Unit
) : Dialog(context) {

    private lateinit var nameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_edit_profile)

        nameEditText = findViewById(R.id.name_edit_text)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)

        // Устанавливаем текущее имя
        nameEditText.setText(currentName)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                onSave(newName)
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
} 