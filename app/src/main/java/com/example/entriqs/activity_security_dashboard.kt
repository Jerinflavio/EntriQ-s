package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.databinding.ActivitySecurityDashboardBinding
import com.example.entriqs.databinding.ItemRecentIncidentBinding
import com.example.entriqs.model.Incident
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SecurityDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityDashboardBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val incidents = mutableListOf<Incident>()
    private var currentFilter = "All Time"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.security_dashboard)

        // Set up the navigation drawer
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation drawer item clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    // Already on the dashboard, do nothing
                }
                R.id.nav_current_visitors -> {
                    startActivity(Intent(this, CurrentVisitorsActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                }
                R.id.nav_incident_reporting -> {
                    startActivity(Intent(this, IncidentReportingActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                }
                R.id.nav_visitor_search -> {
                    startActivity(Intent(this, VisitorSearchActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                }
                R.id.nav_face_recognition -> {
                    startActivity(Intent(this, FaceRecognitionActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                }
                R.id.nav_logout -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        // Load data
        loadData()

        // Display dashboard data
        displayDashboardData()

        // Set up the filter spinner
        setupFilterSpinner()

        // Set up the RecyclerView with recent incidents
        setupRecentIncidentsRecyclerView()
    }

    private fun loadData() {
        // Load visitor and incident data from SharedPreferences
        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )

        // Load incidents
        val incidentPrefs = getSharedPreferences("IncidentPrefs", MODE_PRIVATE)
        val incidentSet = incidentPrefs.getStringSet("incidents", emptySet())?.toMutableSet() ?: mutableSetOf()
        incidents.clear()
        incidentSet.forEach { incidentString ->
            Incident.fromString(incidentString)?.let { incidents.add(it) }
        }
        // Sort incidents by date (most recent first)
        incidents.sortByDescending { it.dateTime }
    }

    private fun displayDashboardData() {
        // Total Current Visitors
        val currentVisitors = AdminRoleManagementActivity.visitorHistoryList
            .filter { it.isValid() && it.status == "Checked In" }
            .size
        binding.currentVisitorsValue.text = String.format(Locale.getDefault(), "%d", currentVisitors)

        // Pending Check-In Verifications
        val pendingVerifications = AdminRoleManagementActivity.visitorHistoryList
            .filter { it.isValid() && it.status == "Checked In" && !it.isVerified }
            .size
        binding.pendingVerificationsValue.text = String.format(Locale.getDefault(), "%d", pendingVerifications)

        // Total Incidents Reported (filtered by current filter)
        val totalIncidents = getFilteredIncidents().size
        binding.totalIncidentsValue.text = String.format(Locale.getDefault(), "%d", totalIncidents)
    }

    private fun setupFilterSpinner() {
        // Set up the Spinner with "All Time" and "Today" options
        val filterOptions = arrayOf(
            getString(R.string.filter_all_time),
            getString(R.string.filter_today)
        )
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = adapter

        // Set up listener to update data when filter changes
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentFilter = filterOptions[position]
                displayDashboardData()
                setupRecentIncidentsRecyclerView()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun getFilteredIncidents(): List<Incident> {
        return if (currentFilter == getString(R.string.filter_today)) {
            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
            incidents.filter { it.dateTime.startsWith(today) }
        } else {
            incidents
        }
    }

    private fun setupRecentIncidentsRecyclerView() {
        // Take the 5 most recent incidents based on the current filter
        val recentIncidents = getFilteredIncidents().take(5)
        val adapter = RecentIncidentAdapter(recentIncidents) {
            // On click, navigate to IncidentListActivity
            startActivity(Intent(this, IncidentListActivity::class.java))
        }
        binding.recentIncidentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentIncidentsRecyclerView.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the activity
        loadData()
        displayDashboardData()
        setupRecentIncidentsRecyclerView()
    }
}

class RecentIncidentAdapter(
    private val incidents: List<Incident>,
    private val onItemClick: () -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<RecentIncidentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = ItemRecentIncidentBinding.bind(itemView)

        fun bind(incident: Incident, onItemClick: () -> Unit) {
            binding.incidentDate.text = incident.dateTime
            binding.incidentType.text = incident.type
            binding.incidentDescription.text = incident.description
            itemView.setOnClickListener { onItemClick() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_incident, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(incidents[position], onItemClick)
    }

    override fun getItemCount(): Int = incidents.size
}