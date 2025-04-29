package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entriqs.databinding.ActivityEditRoleBinding
import com.example.entriqs.model.Role

class EditRoleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditRoleBinding
    private lateinit var role: Role
    private var position: Int = -1
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.requestLayout()

        @Suppress("DEPRECATION")
        role = intent.getParcelableExtra<Role>("role") ?: run {
            Log.e("EditRoleActivity", "Role data is null")
            Toast.makeText(this, "Failed to load role data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        position = intent.getIntExtra("position", -1)
        type = intent.getStringExtra("type") ?: "Security"
        if (position == -1) {
            Log.e("EditRoleActivity", "Position is invalid: $position")
            Toast.makeText(this, "Invalid position", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("EditRoleActivity", "Received role=$role, position=$position, type=$type")
        setupToolbar()
        populateFields()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Edit $type"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun populateFields() {
        binding.etName.setText(role.name)
        binding.etPhone.setText(role.phone)
        binding.etEmail.setText(role.email)
        binding.etPassword.setText(role.password)
        binding.etName.requestFocus()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedRole = Role(
                roleId = role.roleId,
                name = name,
                phone = phone,
                email = email,
                password = password,
                status = role.status,
                type = type
            )
            val intent = Intent().apply {
                putExtra("role", updatedRole as android.os.Parcelable)
                putExtra("position", position)
                putExtra("type", type)
                putExtra("operation", "update")
            }
            Log.d("EditRoleActivity", "Returning updated role=$updatedRole, position=$position, type=$type, operation=update")
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}