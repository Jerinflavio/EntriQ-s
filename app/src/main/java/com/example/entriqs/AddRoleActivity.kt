package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entriqs.databinding.ActivityAddRoleBinding
import com.example.entriqs.model.Role
import java.util.UUID

class AddRoleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRoleBinding
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.requestLayout()

        type = intent.getStringExtra("selectedTab") ?: "Security"
        Log.d("AddRoleActivity", "Started with type: $type")
        setupToolbar()
        setupForm()
        setupCreateButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Add $type"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupForm() {
        binding.etName.setText("")
        binding.etPhone.setText("")
        binding.etEmail.setText("")
        binding.etPassword.setText("")
        binding.etName.requestFocus()
    }

    private fun setupCreateButton() {
        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty()) {
                Log.w("AddRoleActivity", "Validation failed: Name is empty")
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("AddRoleActivity", "Validation passed: Name = $name")
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                Log.w("AddRoleActivity", "Validation failed: Phone number invalid - length=${phone.length}, digits=${phone.all { it.isDigit() }}")
                Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("AddRoleActivity", "Validation passed: Phone = $phone")
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.w("AddRoleActivity", "Validation failed: Email invalid - email=$email")
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("AddRoleActivity", "Validation passed: Email = $email")
            if (password.isEmpty()) {
                Log.w("AddRoleActivity", "Validation failed: Password is empty")
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("AddRoleActivity", "Validation passed: Password = $password")

            val role = Role(
                roleId = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                email = email,
                password = password,
                status = "Active",
                type = type
            )
            val intent = Intent().apply {
                putExtra("role", role as android.os.Parcelable)
                putExtra("type", type)
                putExtra("operation", "add")
            }
            Log.d("AddRoleActivity", "Sending intent with role=$role, type=$type, operation=add")
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}