package com.example.entriqs

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.adapter.RoleAdapter
import com.example.entriqs.databinding.ActivityAdminRoleManagementBinding
import com.example.entriqs.model.ListItem
import com.example.entriqs.model.Role
import com.example.entriqs.model.Visitor
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import java.util.UUID

class AdminRoleManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRoleManagementBinding
    private lateinit var staffPrefs: SharedPreferences
    private lateinit var securityPrefs: SharedPreferences
    private lateinit var visitorPrefs: SharedPreferences
    private val gson: Gson

    private val adapter = RoleAdapter(
        isReadOnly = false,
        onToggleClick = { position, roleItem, type -> toggleStatus(position, roleItem, type) },
        onEditClick = { position, roleItem, type -> editRole(position, roleItem, type) },
        onDeleteClick = { position, type -> deleteRole(position, type) }
    )

    private var selectedTab: String = "Staff"

    init {
        gson = GsonBuilder()
            .registerTypeAdapter(Role::class.java, JsonDeserializer { json, _, _ ->
                val obj = json.asJsonObject
                Role(
                    roleId = obj.get("roleId")?.asString?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString(),
                    name = obj.get("name")?.asString ?: "",
                    phone = obj.get("phone")?.asString ?: "",
                    email = obj.get("email")?.asString ?: "",
                    password = obj.get("password")?.asString ?: "",
                    status = obj.get("status")?.asString ?: "Inactive",
                    type = obj.get("type")?.asString ?: ""
                )
            })
            .registerTypeAdapter(Visitor::class.java, JsonDeserializer { json, _, _ ->
                val obj = json.asJsonObject
                val visitorId = obj.get("visitorId")?.asString?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
                val name = obj.get("name")?.asString?.takeIf { it.isNotEmpty() } ?: "Unknown Visitor"
                val checkInTime = obj.get("checkInTime")?.asString?.takeIf { it.isNotEmpty() } ?: "Unknown"
                Visitor(
                    visitorId = visitorId,
                    name = name,
                    phoneNumber = obj.get("phoneNumber")?.asString ?: "",
                    purpose = obj.get("purpose")?.asString ?: "",
                    destination = obj.get("destination")?.asString ?: "",
                    idNumber = obj.get("idNumber")?.asString ?: "",
                    checkInTime = checkInTime,
                    checkOutTime = obj.get("checkOutTime")?.asString,
                    status = obj.get("status")?.asString ?: "Checked In",
                    photoUri = obj.get("photoUri")?.asString,
                    isEdited = obj.get("isEdited")?.asBoolean ?: false,
                    editedFields = obj.get("editedFields")?.asJsonArray?.mapNotNull { it.asString }?.toSet() ?: emptySet(),
                    isVerified = obj.get("isVerified")?.asBoolean ?: false // Ensure isVerified is deserialized
                )
            })
            .create()
    }

    companion object {
        internal val sharedStaffList = mutableListOf<Role>()
        internal val sharedSecurityList = mutableListOf<Role>()
        internal val sharedVisitorList = mutableListOf<Visitor>()
        internal val visitorHistoryList = mutableListOf<Visitor>()

        private const val STAFF_PREFS_NAME = "StaffPrefs"
        private const val SECURITY_PREFS_NAME = "SecurityPrefs"
        private const val VISITOR_PREFS_NAME = "VisitorPrefs"
        private const val KEY_STAFF_LIST = "staffList"
        private const val KEY_SECURITY_LIST = "securityList"
        private const val KEY_VISITOR_LIST = "visitorList"
        private const val KEY_VISITOR_HISTORY_LIST = "visitorHistoryList"

        fun saveLists(staffPrefs: SharedPreferences, securityPrefs: SharedPreferences, visitorPrefs: SharedPreferences) {
            val staffEditor = staffPrefs.edit()
            val securityEditor = securityPrefs.edit()
            val visitorEditor = visitorPrefs.edit()
            val gson = Gson()

            staffEditor.putString(KEY_STAFF_LIST, gson.toJson(sharedStaffList))
            securityEditor.putString(KEY_SECURITY_LIST, gson.toJson(sharedSecurityList))
            visitorEditor.putString(KEY_VISITOR_LIST, gson.toJson(sharedVisitorList))
            visitorEditor.putString(KEY_VISITOR_HISTORY_LIST, gson.toJson(visitorHistoryList))

            staffEditor.apply()
            securityEditor.apply()
            visitorEditor.apply()

            Log.d("AdminRoleManagement", "Saved visitorHistoryList size: ${visitorHistoryList.size}")
        }

        fun loadLists(staffPrefs: SharedPreferences, securityPrefs: SharedPreferences, visitorPrefs: SharedPreferences, forceRefresh: Boolean = false) {
            val gson = GsonBuilder()
                .registerTypeAdapter(Role::class.java, JsonDeserializer { json, _, _ ->
                    val obj = json.asJsonObject
                    Role(
                        roleId = obj.get("roleId")?.asString?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString(),
                        name = obj.get("name")?.asString ?: "",
                        phone = obj.get("phone")?.asString ?: "",
                        email = obj.get("email")?.asString ?: "",
                        password = obj.get("password")?.asString ?: "",
                        status = obj.get("status")?.asString ?: "Inactive",
                        type = obj.get("type")?.asString ?: ""
                    )
                })
                .registerTypeAdapter(Visitor::class.java, JsonDeserializer { json, _, _ ->
                    val obj = json.asJsonObject
                    val visitorId = obj.get("visitorId")?.asString?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
                    val name = obj.get("name")?.asString?.takeIf { it.isNotEmpty() } ?: "Unknown Visitor"
                    val checkInTime = obj.get("checkInTime")?.asString?.takeIf { it.isNotEmpty() } ?: "Unknown"
                    Visitor(
                        visitorId = visitorId,
                        name = name,
                        phoneNumber = obj.get("phoneNumber")?.asString ?: "",
                        purpose = obj.get("purpose")?.asString ?: "",
                        destination = obj.get("destination")?.asString ?: "",
                        idNumber = obj.get("idNumber")?.asString ?: "",
                        checkInTime = checkInTime,
                        checkOutTime = obj.get("checkOutTime")?.asString,
                        status = obj.get("status")?.asString ?: "Checked In",
                        photoUri = obj.get("photoUri")?.asString,
                        isEdited = obj.get("isEdited")?.asBoolean ?: false,
                        editedFields = obj.get("editedFields")?.asJsonArray?.mapNotNull { it.asString }?.toSet() ?: emptySet(),
                        isVerified = obj.get("isVerified")?.asBoolean ?: false // Ensure isVerified is deserialized
                    )
                })
                .create()

            val staffJson = staffPrefs.getString(KEY_STAFF_LIST, null)
            val securityJson = securityPrefs.getString(KEY_SECURITY_LIST, null)
            val visitorJson = staffPrefs.getString(KEY_VISITOR_LIST, null)
            val visitorHistoryJson = visitorPrefs.getString(KEY_VISITOR_HISTORY_LIST, null)

            if (forceRefresh) {
                sharedStaffList.clear()
                sharedSecurityList.clear()
                sharedVisitorList.clear()
                visitorHistoryList.clear()
            }

            if (sharedStaffList.isEmpty() && staffJson != null) {
                val staffRoles = gson.fromJson<MutableList<Role>>(staffJson, object : TypeToken<MutableList<Role>>() {}.type)
                    .map { it.copy(type = "Staff", roleId = it.roleId.ifEmpty { UUID.randomUUID().toString() }) }
                sharedStaffList.addAll(staffRoles)
            }

            if (sharedSecurityList.isEmpty() && securityJson != null) {
                val securityRoles = gson.fromJson<MutableList<Role>>(securityJson, object : TypeToken<MutableList<Role>>() {}.type)
                    .map { it.copy(type = "Security", roleId = it.roleId.ifEmpty { UUID.randomUUID().toString() }) }
                sharedSecurityList.addAll(securityRoles)
            }

            if (sharedVisitorList.isEmpty() && visitorJson != null) {
                val visitorRoles = gson.fromJson<MutableList<Visitor>>(visitorJson, object : TypeToken<MutableList<Visitor>>() {}.type)
                sharedVisitorList.addAll(visitorRoles)
            }

            if (visitorHistoryList.isEmpty() || forceRefresh) {
                if (visitorHistoryJson != null) {
                    val visitorHistoryRoles = gson.fromJson<MutableList<Visitor>>(visitorHistoryJson, object : TypeToken<MutableList<Visitor>>() {}.type)
                    visitorHistoryList.clear()
                    visitorHistoryList.addAll(visitorHistoryRoles)
                }
            }

            Log.d("AdminRoleManagement", "Loaded visitorHistoryList size: ${visitorHistoryList.size}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRoleManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        staffPrefs = getSharedPreferences(STAFF_PREFS_NAME, MODE_PRIVATE)
        securityPrefs = getSharedPreferences(SECURITY_PREFS_NAME, MODE_PRIVATE)
        visitorPrefs = getSharedPreferences(VISITOR_PREFS_NAME, MODE_PRIVATE)

        loadLists(staffPrefs, securityPrefs, visitorPrefs, forceRefresh = true)

        setupRecyclerView()
        setupToolbar()
        setupTabLayout()
        setupAddButton()

        onBackPressedDispatcher.addCallback(this) { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadLists(staffPrefs, securityPrefs, visitorPrefs, forceRefresh = false)
        updateRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Role Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = if (tab?.position == 0) "Staff" else "Security"
                updateRecyclerView()
                updateEmptyState()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Staff"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Security"))
        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        updateRecyclerView()
    }

    private fun setupAddButton() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddRoleActivity::class.java).apply {
                putExtra("selectedTab", selectedTab)
            }
            addRoleLauncher.launch(intent)
        }
    }

    private val addRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            @Suppress("DEPRECATION")
            val role = data?.getParcelableExtra<Role>("role")
            val type = data?.getStringExtra("type") ?: selectedTab
            val position = data?.getIntExtra("position", -1) ?: -1

            if (role != null) {
                val sharedList = if (type == "Staff") sharedStaffList else sharedSecurityList
                val otherList = if (type == "Staff") sharedSecurityList else sharedStaffList
                val newRole = Role(
                    roleId = role.roleId,
                    name = role.name,
                    phone = role.phone,
                    email = role.email,
                    password = role.password,
                    status = role.status,
                    type = type
                )

                if (position == -1) {
                    if (sharedList.any { it.name == newRole.name } || otherList.any { it.name == newRole.name }) {
                        Toast.makeText(this, "Role '${newRole.name}' already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        sharedList.add(newRole)
                    }
                } else if (position in sharedList.indices) {
                    sharedList[position] = newRole
                }
                saveLists(staffPrefs, securityPrefs, visitorPrefs)
                updateRecyclerView()
            }
        }
    }

    private fun updateRecyclerView() {
        val newList = if (selectedTab == "Staff") {
            sharedStaffList.map { ListItem.RoleItem(it, "Staff") }
        } else {
            sharedSecurityList.map { ListItem.RoleItem(it, "Security") }
        }
        adapter.submitList(newList.toList())
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val isEmpty = if (selectedTab == "Staff") sharedStaffList.isEmpty() else sharedSecurityList.isEmpty()
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun toggleStatus(position: Int, roleItem: ListItem.RoleItem, type: String) {
        val sharedList = if (type == "Staff") sharedStaffList else sharedSecurityList
        if (position in sharedList.indices) {
            val currentRole = sharedList[position]
            val newStatus = if (currentRole.status == "Active") "Inactive" else "Active"
            sharedList[position] = currentRole.copy(status = newStatus)
            saveLists(staffPrefs, securityPrefs, visitorPrefs)
            updateRecyclerView()
        }
    }

    private fun editRole(position: Int, roleItem: ListItem.RoleItem, type: String) {
        val intent = Intent(this, EditRoleActivity::class.java).apply {
            putExtra("role", roleItem.role)
            putExtra("position", position)
            putExtra("type", type)
        }
        addRoleLauncher.launch(intent)
    }

    private fun deleteRole(position: Int, type: String) {
        val sharedList = if (type == "Staff") sharedStaffList else sharedSecurityList
        if (position in sharedList.indices) {
            sharedList.removeAt(position)
            saveLists(staffPrefs, securityPrefs, visitorPrefs)
            updateRecyclerView()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}