package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.databinding.ActivityStaffVisitorDetailsBinding
import com.example.entriqs.model.Visitor
import java.util.Calendar

class VisitorDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffVisitorDetailsBinding
    private lateinit var adapter: VisitorDetailsAdapter
    private val allVisitors = mutableListOf<Visitor>() // Original list of all visitors
    private val filteredVisitors = mutableListOf<Visitor>() // Filtered list for display
    private var currentStatusFilter = "All" // Track the current status filter
    private var currentDateFilter = "All Time" // Track the current date filter

    // Register the ActivityResultLauncher at the activity level
    private val editVisitorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            @Suppress("DEPRECATION")
            val updatedVisitor = result.data?.getParcelableExtra<Visitor>("visitor")
            updatedVisitor?.let {
                updateVisitor(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffVisitorDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set toolbar title from custom attribute
        val attrs = intArrayOf(R.attr.toolbarTitle)
        val typedArray = obtainStyledAttributes(attrs)
        val toolbarTitle = typedArray.getString(0) ?: "All Visitor Details"
        typedArray.recycle()

        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )

        setupToolbar(toolbarTitle)
        setupRecyclerView()
        setupStatusFilterSpinner()
        setupDateFilterSpinner()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_visitor_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_statistics -> {
                // Navigate to StatisticsActivity
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )
        updateRecyclerView()
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        filteredVisitors.clear()
        filteredVisitors.addAll(AdminRoleManagementActivity.visitorHistoryList.filter { it.isValid() })
        adapter = VisitorDetailsAdapter(filteredVisitors, editVisitorLauncher)
        binding.visitorDetailsList.layoutManager = LinearLayoutManager(this)
        binding.visitorDetailsList.adapter = adapter
        updateRecyclerView()
    }

    private fun setupStatusFilterSpinner() {
        // Set up the Spinner with status filter options
        val filterAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        )
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = filterAdapter

        // Handle status filter selection
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentStatusFilter = parent.getItemAtPosition(position).toString()
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                currentStatusFilter = "All"
                applyFilters()
            }
        }
    }

    private fun setupDateFilterSpinner() {
        // Set up the Spinner with date filter options
        val dateFilterAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.date_filter_options,
            android.R.layout.simple_spinner_item
        )
        dateFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateFilterSpinner.adapter = dateFilterAdapter

        // Handle date filter selection
        binding.dateFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentDateFilter = parent.getItemAtPosition(position).toString()
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                currentDateFilter = "All Time"
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val oldList = filteredVisitors.toList()
        filteredVisitors.clear()
        allVisitors.clear()
        allVisitors.addAll(AdminRoleManagementActivity.visitorHistoryList.filter { it.isValid() })

        // Apply status filter
        val statusFilteredList = when (currentStatusFilter) {
            "All" -> allVisitors
            "Checked In" -> allVisitors.filter { it.status == "Checked In" }
            "Checked Out" -> allVisitors.filter { it.status == "Checked Out" }
            else -> allVisitors
        }

        // Apply date filter
        filteredVisitors.addAll(statusFilteredList.filter { visitor ->
            val checkInTime = visitor.getCheckInTimeAsLong()
            if (checkInTime == 0L) return@filter false // Skip invalid dates

            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            when (currentDateFilter) {
                "All Time" -> true
                "Today" -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfToday = calendar.timeInMillis
                    checkInTime in startOfToday..now
                }
                "Yesterday" -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val endOfYesterday = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    val startOfYesterday = calendar.timeInMillis
                    checkInTime in startOfYesterday until endOfYesterday
                }
                "Last 7 Days" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    val startOfLast7Days = calendar.timeInMillis
                    checkInTime in startOfLast7Days..now
                }
                "Last 30 Days" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    val startOfLast30Days = calendar.timeInMillis
                    checkInTime in startOfLast30Days..now
                }
                else -> true
            }
        })

        // Use DiffUtil to efficiently update the RecyclerView
        val diffResult = DiffUtil.calculateDiff(VisitorDiffCallback(oldList, filteredVisitors))
        diffResult.dispatchUpdatesTo(adapter)

        // Show/hide empty state text
        binding.emptyStateText.visibility = if (filteredVisitors.isEmpty()) View.VISIBLE else View.GONE
        binding.visitorDetailsList.visibility = if (filteredVisitors.isEmpty()) View.GONE else View.VISIBLE
        Log.d("VisitorDetailsActivity", "Applied filters: Status=$currentStatusFilter, Date=$currentDateFilter, showing ${filteredVisitors.size} visitors")
    }

    private fun updateVisitor(updatedVisitor: Visitor) {
        val index = AdminRoleManagementActivity.visitorHistoryList.indexOfFirst {
            it.visitorId == updatedVisitor.visitorId
        }
        if (index >= 0) {
            AdminRoleManagementActivity.visitorHistoryList[index] = updatedVisitor.copy(
                visitorId = updatedVisitor.visitorId,
                checkInTime = AdminRoleManagementActivity.visitorHistoryList[index].checkInTime,
                checkOutTime = AdminRoleManagementActivity.visitorHistoryList[index].checkOutTime,
                status = AdminRoleManagementActivity.visitorHistoryList[index].status,
                isEdited = true
            )

            if (updatedVisitor.status == "Checked In") {
                val sharedIndex = AdminRoleManagementActivity.sharedVisitorList.indexOfFirst {
                    it.visitorId == updatedVisitor.visitorId
                }
                if (sharedIndex >= 0) {
                    AdminRoleManagementActivity.sharedVisitorList[sharedIndex] = updatedVisitor.copy(
                        visitorId = updatedVisitor.visitorId,
                        checkInTime = AdminRoleManagementActivity.sharedVisitorList[sharedIndex].checkInTime,
                        checkOutTime = AdminRoleManagementActivity.sharedVisitorList[sharedIndex].checkOutTime,
                        status = AdminRoleManagementActivity.sharedVisitorList[sharedIndex].status,
                        isEdited = true
                    )
                }
            }

            AdminRoleManagementActivity.saveLists(
                getSharedPreferences("StaffPrefs", MODE_PRIVATE),
                getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
                getSharedPreferences("VisitorPrefs", MODE_PRIVATE)
            )
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        // Update the allVisitors list with the latest data
        allVisitors.clear()
        allVisitors.addAll(AdminRoleManagementActivity.visitorHistoryList.filter { it.isValid() })
        // Reapply the current filters
        applyFilters()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

// DiffUtil callback to efficiently update the RecyclerView
class VisitorDiffCallback(
    private val oldList: List<Visitor>,
    private val newList: List<Visitor>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].visitorId == newList[newItemPosition].visitorId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}