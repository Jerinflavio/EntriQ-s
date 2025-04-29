package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.databinding.ActivityStaffCheckOutVisitorBinding
import com.example.entriqs.model.Visitor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffCheckOutVisitorBinding
    private lateinit var adapter: VisitorCheckOutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffCheckOutVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupVisitorList()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
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
                R.id.nav_check_in -> {
                    startActivity(Intent(this, CheckInActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_check_out -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_visitor_details -> {
                    Toast.makeText(this, getString(R.string.toast_visitor_detail_clicked), Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, StaffDashboardActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logout -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        binding.navView.setCheckedItem(R.id.nav_check_out)
    }

    private fun setupVisitorList() {
        val allVisitors = AdminRoleManagementActivity.visitorHistoryList
        binding.visitorList.visibility = if (allVisitors.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyStateText.visibility = if (allVisitors.isEmpty()) View.VISIBLE else View.GONE

        adapter = VisitorCheckOutAdapter(allVisitors) { visitor ->
            checkOutVisitor(visitor)
        }
        binding.visitorList.layoutManager = LinearLayoutManager(this)
        binding.visitorList.adapter = adapter
    }

    private fun checkOutVisitor(visitor: Visitor) {
        if (visitor.visitorId.isEmpty()) {
            Log.e("CheckOutActivity", "Cannot check out visitor with empty visitorId: $visitor")
            Toast.makeText(this, "Error: Visitor ID is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val historyIndex = AdminRoleManagementActivity.visitorHistoryList.indexOfFirst { it.visitorId == visitor.visitorId }
        if (historyIndex != -1) {
            val updatedVisitor = visitor.copy(
                status = "Checked Out",
                checkOutTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            )
            AdminRoleManagementActivity.visitorHistoryList[historyIndex] = updatedVisitor
            AdminRoleManagementActivity.sharedVisitorList.removeIf { it.visitorId == visitor.visitorId }
            AdminRoleManagementActivity.saveLists(
                getSharedPreferences("StaffPrefs", MODE_PRIVATE),
                getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
                getSharedPreferences("VisitorPrefs", MODE_PRIVATE)
            )
            adapter.updateVisitors(AdminRoleManagementActivity.visitorHistoryList)
            Toast.makeText(this, "${visitor.name} has been checked out.", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("CheckOutActivity", "Visitor not found in history with visitorId: ${visitor.visitorId}")
            Toast.makeText(this, "Visitor ${visitor.name} not found.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}