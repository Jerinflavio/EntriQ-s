package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.adapter.RoleAdapter
import com.example.entriqs.databinding.ActivityAdminDashboardBinding
import com.example.entriqs.model.ListItem
import com.example.entriqs.model.Role
import com.example.entriqs.model.Visitor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import java.util.UUID

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var roleAdapter: RoleAdapter
    private var selectedTab: String = "Security"
    private val gson: Gson

    init {
        gson = GsonBuilder()
            .registerTypeAdapter(Role::class.java, JsonDeserializer { json, _, _ ->
                val obj = json.asJsonObject
                Role(
                    roleId = obj.get("roleId")?.asString ?: UUID.randomUUID().toString(),
                    name = obj.get("name")?.asString ?: "",
                    phone = obj.get("phone")?.asString ?: "",
                    email = obj.get("email")?.asString ?: "",
                    password = obj.get("password")?.asString ?: "",
                    status = obj.get("status")?.asString ?: "Inactive",
                    type = obj.get("type")?.asString ?: ""
                )
            })
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadListsFromPrefs()
        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        updateDashboardCounts()
        setupHeroSectionClicks()
    }

    override fun onResume() {
        super.onResume()
        updateDashboardCounts()
        updateRecyclerView()
        Log.d("AdminDashboard", "onResume: Refreshed data for selectedTab=$selectedTab")
        Log.d("AdminDashboard", "onResume lists: staffList size=${AdminRoleManagementActivity.sharedStaffList.size}, securityList size=${AdminRoleManagementActivity.sharedSecurityList.size}, visitorList size=${AdminRoleManagementActivity.sharedVisitorList.size}")
    }

    private fun loadListsFromPrefs() {
        val staffPrefs = getSharedPreferences("StaffPrefs", MODE_PRIVATE)
        val securityPrefs = getSharedPreferences("SecurityPrefs", MODE_PRIVATE)
        val visitorPrefs = getSharedPreferences("VisitorPrefs", MODE_PRIVATE)

        val staffListJson = staffPrefs.getString("staffList", null)
        val securityListJson = securityPrefs.getString("securityList", null)
        val visitorListJson = visitorPrefs.getString("visitorList", null)

        AdminRoleManagementActivity.sharedStaffList.clear()
        AdminRoleManagementActivity.sharedSecurityList.clear()
        AdminRoleManagementActivity.sharedVisitorList.clear()

        if (staffListJson != null) {
            val type = object : TypeToken<MutableList<Role>>() {}.type
            val staffRoles = gson.fromJson<MutableList<Role>>(staffListJson, type)
                .map { it.copy(type = "Staff", roleId = it.roleId.ifEmpty { UUID.randomUUID().toString() }) }
                .filter { it.isValid() }
            AdminRoleManagementActivity.sharedStaffList.addAll(staffRoles)
            Log.d("AdminDashboard", "Loaded staffList: size=${staffRoles.size}")
        }
        if (securityListJson != null) {
            val type = object : TypeToken<MutableList<Role>>() {}.type
            val securityRoles = gson.fromJson<MutableList<Role>>(securityListJson, type)
                .map { it.copy(type = "Security", roleId = it.roleId.ifEmpty { UUID.randomUUID().toString() }) }
                .filter { it.isValid() }
            AdminRoleManagementActivity.sharedSecurityList.addAll(securityRoles)
            Log.d("AdminDashboard", "Loaded securityList: size=${securityRoles.size}")
        }
        if (visitorListJson != null) {
            val type = object : TypeToken<MutableList<Visitor>>() {}.type
            val visitorRoles = gson.fromJson<MutableList<Visitor>>(visitorListJson, type)
            AdminRoleManagementActivity.sharedVisitorList.addAll(visitorRoles)
            Log.d("AdminDashboard", "Loaded visitorList: size=${visitorRoles.size}")
        }
        Log.d("AdminDashboard", "Loaded lists: staffList size=${AdminRoleManagementActivity.sharedStaffList.size}, securityList size=${AdminRoleManagementActivity.sharedSecurityList.size}, visitorList size=${AdminRoleManagementActivity.sharedVisitorList.size}")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.admin_dashboard_title)
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
                    startActivity(Intent(this, AdminAIReportActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_visitor_history -> {
                    startActivity(Intent(this, VisitorDetailsActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_incident_reports -> {
                    startActivity(Intent(this, IncidentListActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_role_management -> {
                    startActivity(Intent(this, AdminRoleManagementActivity::class.java))
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
    }

    private fun setupRecyclerView() {
        roleAdapter = RoleAdapter(isReadOnly = true)
        binding.listRecycler.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = roleAdapter
        }
        updateRecyclerView()
    }

    private fun setupHeroSectionClicks() {
        updateCardStyles()
        binding.staffCard.setOnClickListener {
            selectedTab = "Staff"
            updateCardStyles()
            updateRecyclerView()
            updateListTitle()
        }
        binding.securityCard.setOnClickListener {
            selectedTab = "Security"
            updateCardStyles()
            updateRecyclerView()
            updateListTitle()
        }
        binding.visitorCard.setOnClickListener {
            selectedTab = "Visitor"
            updateCardStyles()
            updateRecyclerView()
            updateListTitle()
        }
    }

    private fun updateCardStyles() {
        when (selectedTab) {
            "Staff" -> {
                binding.staffCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purple_200))
                binding.securityCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.visitorCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.staffText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.staffCount.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.securityText.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
                binding.securityCount.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.visitorText.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
                binding.visitorCount.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "Visitor" -> {
                binding.visitorCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purple_200))
                binding.staffCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.securityCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.visitorText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.visitorCount.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.staffText.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
                binding.staffCount.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.securityText.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
                binding.securityCount.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            else -> {
                binding.securityCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.purple_200))
                binding.staffCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.visitorCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.securityText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.securityCount.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.staffText.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
                binding.staffCount.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.visitorText.setTextColor(ContextCompat.getColor(this, R.color.dark_purple))
                binding.visitorCount.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

    private fun updateDashboardCounts() {
        val staffCount = AdminRoleManagementActivity.sharedStaffList.size
        val activeStaffCount = AdminRoleManagementActivity.sharedStaffList.count { it.status == "Active" }
        val securityCount = AdminRoleManagementActivity.sharedSecurityList.size
        val activeSecurityCount = AdminRoleManagementActivity.sharedSecurityList.count { it.status == "Active" }
        val activeVisitorCount = AdminRoleManagementActivity.sharedVisitorList.size

        Log.d("AdminDashboard", "Staff count: $staffCount, Active Staff: $activeStaffCount, Security count: $securityCount, Active Security: $activeSecurityCount")
        binding.staffCount.text = getString(R.string.active_staff_count, activeStaffCount, staffCount)
        binding.securityCount.text = getString(R.string.active_security_count, activeSecurityCount, securityCount)
        binding.visitorCount.text = getString(R.string.active_visitor_count, activeVisitorCount, activeVisitorCount)
    }

    private fun updateRecyclerView() {
        val list = when (selectedTab) {
            "Staff" -> AdminRoleManagementActivity.sharedStaffList.map { ListItem.RoleItem(it, "Staff") }
            "Visitor" -> AdminRoleManagementActivity.sharedVisitorList.map { ListItem.VisitorItem(it) }
            else -> AdminRoleManagementActivity.sharedSecurityList.map { ListItem.RoleItem(it, "Security") }
        }
        roleAdapter.submitList(list.toList())
        updateListTitle()
        Log.d("AdminDashboard", "Updated RecyclerView with $selectedTab list: size=${list.size}")
    }

    private fun updateListTitle() {
        binding.listTitle.text = when (selectedTab) {
            "Staff" -> getString(R.string.staff_list_header)
            "Visitor" -> getString(R.string.visitor_list_header)
            else -> getString(R.string.admin_security_list)
        }
    }
}