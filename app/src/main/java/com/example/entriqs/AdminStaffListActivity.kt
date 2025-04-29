package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.adapter.RoleAdapter
import com.example.entriqs.databinding.ActivityAdminStaffListBinding
import com.example.entriqs.model.ListItem

class AdminStaffListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminStaffListBinding
    private lateinit var roleAdapter: RoleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminStaffListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView()
        Log.d("AdminStaffListActivity", "onResume: staffList size=${AdminRoleManagementActivity.sharedStaffList.size}")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.admin_staff_list_title)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(binding.navView)
        }
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_security -> {
                    startActivity(Intent(this, AdminSecurityListActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_staff -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_ai_report -> {
                    startActivity(Intent(this, AdminAIReportActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_visitor_history -> {
                    startActivity(Intent(this, AdminAIReportActivity::class.java))
                    true
                }
                R.id.nav_role_management -> {
                    startActivity(Intent(this, AdminRoleManagementActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
        binding.navView.setCheckedItem(R.id.nav_staff)
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleAdapter(
            isReadOnly = true,
            showAllDetails = true // Show all details (name, phone, email, password) on this page
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminStaffListActivity)
            adapter = roleAdapter
        }
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        val staffList = AdminRoleManagementActivity.sharedStaffList.map { ListItem.RoleItem(it, "Staff") }
        roleAdapter.submitList(staffList.toList())
        updateEmptyState(staffList.isEmpty())
        Log.d("AdminStaffListActivity", "Updated RecyclerView with staff list: size=${staffList.size}")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}