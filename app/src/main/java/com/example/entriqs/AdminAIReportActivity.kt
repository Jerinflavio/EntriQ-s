package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.entriqs.databinding.ActivityAdminAiReportBinding

class AdminAIReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAiReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAiReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupRefreshButton()
        generateAIReport()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.admin_ai_report_title)
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
                    startActivity(Intent(this, AdminStaffListActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_ai_report -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_visitor_history -> {
                    // TODO: Implement Visitor History page
                    binding.drawerLayout.closeDrawers()
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
        binding.navView.setCheckedItem(R.id.nav_ai_report)
    }

    private fun setupRefreshButton() {
        binding.refreshFab.setOnClickListener {
            generateAIReport()
        }
    }

    private fun generateAIReport() {
        // Collect data from shared lists
        val securityList = AdminRoleManagementActivity.sharedSecurityList
        val staffList = AdminRoleManagementActivity.sharedStaffList

        // Calculate statistics
        val activeSecurity = securityList.count { it.status == "Active" }
        val inactiveSecurity = securityList.count { it.status == "Inactive" }
        val activeStaff = staffList.count { it.status == "Active" }
        val inactiveStaff = staffList.count { it.status == "Inactive" }

        // Calculate totals
        val totalRoles = securityList.size + staffList.size
        val totalSecurity = securityList.size
        val totalStaff = staffList.size

        // Populate the summary section
        binding.totalRolesValue.text = totalRoles.toString()
        binding.securityPersonnelValue.text = totalSecurity.toString()
        binding.securityActiveValue.text = activeSecurity.toString()
        binding.securityInactiveValue.text = inactiveSecurity.toString()
        binding.staffMembersValue.text = totalStaff.toString()
        binding.staffActiveValue.text = activeStaff.toString()
        binding.staffInactiveValue.text = inactiveStaff.toString()

        // Generate insight
        val insight = if (activeSecurity + activeStaff < (totalRoles / 2)) {
            "Less than 50% of roles are active. Consider reviewing inactive roles to improve operational capacity."
        } else {
            "More than 50% of roles are active, indicating good operational capacity."
        }
        binding.insightText.text = insight

        // Highlight insight based on status
        val insightColor = if (activeSecurity + activeStaff < (totalRoles / 2)) {
            R.color.red_500 // Highlight in red if less than 50% are active
        } else {
            R.color.green_500 // Highlight in green if more than 50% are active
        }
        binding.insightText.setTextColor(ContextCompat.getColor(this, insightColor))

        // Update empty state
        val isDataAvailable = totalRoles > 0
        binding.reportContent.visibility = if (isDataAvailable) View.VISIBLE else View.GONE
        binding.emptyStateText.visibility = if (isDataAvailable) View.GONE else View.VISIBLE

        // Populate the RecyclerView with status categories
        if (isDataAvailable) {
            setupStatusCards(activeSecurity, inactiveSecurity, activeStaff, inactiveStaff, totalRoles)
        }

        Log.d("AdminAIReportActivity", "Generated AI Report: Total Roles: $totalRoles, Security: $totalSecurity (Active: $activeSecurity, Inactive: $inactiveSecurity), Staff: $totalStaff (Active: $activeStaff, Inactive: $inactiveStaff), Insight: $insight")
    }

    private fun setupStatusCards(
        activeSecurity: Int,
        inactiveSecurity: Int,
        activeStaff: Int,
        inactiveStaff: Int,
        totalRoles: Int
    ) {
        // Calculate percentages
        val activeSecurityPercentage = if (totalRoles > 0) (activeSecurity.toFloat() / totalRoles * 100) else 0f
        val inactiveSecurityPercentage = if (totalRoles > 0) (inactiveSecurity.toFloat() / totalRoles * 100) else 0f
        val activeStaffPercentage = if (totalRoles > 0) (activeStaff.toFloat() / totalRoles * 100) else 0f
        val inactiveStaffPercentage = if (totalRoles > 0) (inactiveStaff.toFloat() / totalRoles * 100) else 0f

        // Create a list of status categories
        val categories = listOf(
            StatusCategory(
                name = "Active Security",
                count = activeSecurity,
                percentage = activeSecurityPercentage,
                progressColor = R.color.green_500,
                iconRes = R.drawable.ic_security
            ),
            StatusCategory(
                name = "Inactive Security",
                count = inactiveSecurity,
                percentage = inactiveSecurityPercentage,
                progressColor = R.color.red_500,
                iconRes = R.drawable.ic_security
            ),
            StatusCategory(
                name = "Active Staff",
                count = activeStaff,
                percentage = activeStaffPercentage,
                progressColor = R.color.green_500,
                iconRes = R.drawable.ic_staff
            ),
            StatusCategory(
                name = "Inactive Staff",
                count = inactiveStaff,
                percentage = inactiveStaffPercentage,
                progressColor = R.color.red_500,
                iconRes = R.drawable.ic_staff
            )
        )

        // Set up the RecyclerView
        val adapter = StatusCategoryAdapter(categories)
        binding.statusRecyclerView.adapter = adapter

        // Apply the fade-in animation
        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fade_in)
        binding.statusRecyclerView.layoutAnimation = animation
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}