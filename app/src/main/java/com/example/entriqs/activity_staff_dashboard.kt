package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.adapter.VisitorLogAdapter
import com.example.entriqs.databinding.ActivityStaffDashboardBinding
import com.example.entriqs.model.ListItem

class StaffDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private var selectedFilter: String = "Today"
    private lateinit var visitorLogAdapter: VisitorLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE)
        )

        setupToolbar()
        setupNavigationDrawer()
        setupFilterSpinner()
        setupVisitorLogRecyclerView()
        observeViewModel()
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
                    startActivity(Intent(this, CheckOutActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_visitor_details -> {
                    startActivity(Intent(this, VisitorDetailsActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
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
        binding.navView.setCheckedItem(R.id.nav_statistics)
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf("Today", "Last 7 Days", "Last 30 Days")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = adapter

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedFilter = filterOptions[position]
                viewModel.updateVisitorStats(selectedFilter)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupVisitorLogRecyclerView() {
        visitorLogAdapter = VisitorLogAdapter()
        binding.visitorLogRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.visitorLogRecyclerView.adapter = visitorLogAdapter
    }

    private fun observeViewModel() {
        viewModel.checkIns.observe(this) { checkIns ->
            binding.checkinValue.text = checkIns.toString()
            Log.d("StaffDashboardActivity", "Check-Ins updated: $checkIns")
        }
        viewModel.checkOuts.observe(this) { checkOuts ->
            binding.checkoutValue.text = checkOuts.toString()
            Log.d("StaffDashboardActivity", "Check-Outs updated: $checkOuts")
        }
        viewModel.totalVisitors.observe(this) { total ->
            binding.totalValue.text = total.toString()
            Log.d("StaffDashboardActivity", "Total Visitors updated: $total")
        }
        viewModel.visitorList.observe(this) { filteredVisitors ->
            val visitorItems = filteredVisitors.map { ListItem.VisitorItem(it) }
            visitorLogAdapter.submitList(visitorItems)
            Log.d("StaffDashboardActivity", "Visitor list updated: ${visitorItems.size} items after filter $selectedFilter")
        }
    }

    override fun onResume() {
        super.onResume()
        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE)
        )
        viewModel.updateVisitorStats(selectedFilter)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}