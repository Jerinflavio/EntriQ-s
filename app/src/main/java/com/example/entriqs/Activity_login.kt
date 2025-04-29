package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.entriqs.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var selectedRole: String? = null
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "LoginActivity onCreate called")

        binding.profileIcon.setOnClickListener {
            showRoleSelectionSection()
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (selectedRole == null) {
                Toast.makeText(this, R.string.toast_login_failed, Toast.LENGTH_SHORT).show()
            } else {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_login_success, selectedRole), Toast.LENGTH_SHORT).show()
                    when (selectedRole) {
                        "Admin" -> {
                            Log.d(TAG, "Navigating to AdminDashboardActivity")
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "Staff" -> {
                            Log.d(TAG, "Navigating to StaffDashboardActivity")
                            val intent = Intent(this, StaffDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "Security" -> {
                            Log.d(TAG, "Navigating to SecurityDashboardActivity")
                            val intent = Intent(this, SecurityDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.toast_login_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.adminButton.setOnClickListener {
            updateRole("Admin", R.color.purple_500)
            hideRoleSelectionSection()
        }
        binding.staffButton.setOnClickListener {
            updateRole("Staff", R.color.purple_500)
            hideRoleSelectionSection()
        }
        binding.securityButton.setOnClickListener {
            updateRole("Security", R.color.purple_500)
            hideRoleSelectionSection()
        }
    }

    private fun showRoleSelectionSection() {
        binding.roleSelectionSection.visibility = View.VISIBLE
        binding.loginCard.alpha = 0.3f
        binding.loginCard.isEnabled = false
    }

    private fun hideRoleSelectionSection() {
        binding.roleSelectionSection.visibility = View.GONE
        binding.loginCard.alpha = 1.0f
        binding.loginCard.isEnabled = true
    }

    private fun updateRole(role: String, colorResId: Int) {
        selectedRole = role
        when (selectedRole) {
            "Admin" -> binding.usernameEditText.hint = getString(R.string.admin_login_title)
            "Staff" -> binding.usernameEditText.hint = getString(R.string.staff_login_title)
            "Security" -> binding.usernameEditText.hint = getString(R.string.security_login_title)
            else -> binding.usernameEditText.hint = getString(R.string.hint_name)
        }
        binding.profileIcon.backgroundTintList = ContextCompat.getColorStateList(this, colorResId)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "LoginActivity onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "LoginActivity onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "LoginActivity onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "LoginActivity onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LoginActivity onDestroy called")
    }
}