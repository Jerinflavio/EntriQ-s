package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.entriqs.databinding.ActivityStaffVisitorEditBinding
import com.example.entriqs.model.Visitor

class EditVisitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffVisitorEditBinding
    private lateinit var visitor: Visitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffVisitorEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        visitor = intent.getParcelableExtra("visitor") ?: return

        setupToolbar()
        setupForm()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupForm() {
        binding.nameInput.setText(visitor.name)
        binding.phoneInput.setText(visitor.phoneNumber)
        binding.purposeInput.setText(visitor.purpose)
        binding.destinationInput.setText(visitor.destination)
        binding.idNumberInput.setText(visitor.idNumber)
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val editedFields = mutableSetOf<String>().apply {
                if (binding.nameInput.text.toString() != visitor.name) add("name")
                if (binding.phoneInput.text.toString() != visitor.phoneNumber) add("phone")
                if (binding.purposeInput.text.toString() != visitor.purpose) add("purpose")
                if (binding.destinationInput.text.toString() != visitor.destination) add("destination")
                if (binding.idNumberInput.text.toString() != visitor.idNumber) add("idNumber")
            }

            val updatedVisitor = visitor.copy(
                name = binding.nameInput.text.toString(),
                phoneNumber = binding.phoneInput.text.toString(),
                purpose = binding.purposeInput.text.toString(),
                destination = binding.destinationInput.text.toString(),
                idNumber = binding.idNumberInput.text.toString(),
                isEdited = editedFields.isNotEmpty(),
                editedFields = editedFields
            )

            val resultIntent = Intent().apply {
                putExtra("visitor", updatedVisitor)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}