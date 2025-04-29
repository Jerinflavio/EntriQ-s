package com.example.entriqs

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.entriqs.databinding.ActivityStaisticsBinding
import com.example.entriqs.model.Visitor
import java.util.Calendar

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setupToolbar()

        // Load the visitor data
        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )

        // Calculate and display statistics
        displayStatistics()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Statistics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun displayStatistics() {
        val visitors = AdminRoleManagementActivity.visitorHistoryList.filter { it.isValid() }

        // Total Visitors
        val totalVisitors = visitors.size
        binding.totalVisitors.text = "Total Visitors: $totalVisitors"

        // Checked In Visitors
        val checkedInVisitors = visitors.count { it.status == "Checked In" }
        binding.checkedInVisitors.text = "Checked In Visitors: $checkedInVisitors"

        // Checked Out Visitors
        val checkedOutVisitors = visitors.count { it.status == "Checked Out" }
        binding.checkedOutVisitors.text = "Checked Out Visitors: $checkedOutVisitors"

        // Visitors by Date Range
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        val visitorsToday = visitors.count { visitor ->
            val checkInTime = visitor.getCheckInTimeAsLong()
            checkInTime >= startOfToday && checkInTime <= now
        }
        binding.visitorsToday.text = "Visitors Today: $visitorsToday"

        // Yesterday
        val endOfYesterday = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = calendar.timeInMillis
        val visitorsYesterday = visitors.count { visitor ->
            val checkInTime = visitor.getCheckInTimeAsLong()
            checkInTime >= startOfYesterday && checkInTime < endOfYesterday
        }
        binding.visitorsYesterday.text = "Visitors Yesterday: $visitorsYesterday"

        // Last 7 Days
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startOfLast7Days = calendar.timeInMillis
        val visitorsLast7Days = visitors.count { visitor ->
            val checkInTime = visitor.getCheckInTimeAsLong()
            checkInTime >= startOfLast7Days && checkInTime <= now
        }
        binding.visitorsLast7Days.text = "Visitors Last 7 Days: $visitorsLast7Days"

        // Last 30 Days
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startOfLast30Days = calendar.timeInMillis
        val visitorsLast30Days = visitors.count { visitor ->
            val checkInTime = visitor.getCheckInTimeAsLong()
            checkInTime >= startOfLast30Days && checkInTime <= now
        }
        binding.visitorsLast30Days.text = "Visitors Last 30 Days: $visitorsLast30Days"

        Log.d("StatisticsActivity", "Displayed statistics: Total=$totalVisitors, CheckedIn=$checkedInVisitors, CheckedOut=$checkedOutVisitors")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}