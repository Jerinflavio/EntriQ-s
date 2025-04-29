package com.example.entriqs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.entriqs.model.Security
import com.example.entriqs.model.Staff
import com.example.entriqs.model.Visitor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val _staffCount = MutableLiveData<Int>()
    val staffCount: LiveData<Int> get() = _staffCount

    private val _securityCount = MutableLiveData<Int>()
    val securityCount: LiveData<Int> get() = _securityCount

    private val _checkIns = MutableLiveData<Int>()
    val checkIns: LiveData<Int> get() = _checkIns

    private val _checkOuts = MutableLiveData<Int>()
    val checkOuts: LiveData<Int> get() = _checkOuts

    private val _totalVisitors = MutableLiveData<Int>()
    val totalVisitors: LiveData<Int> get() = _totalVisitors

    private val _staffList = MutableLiveData<List<Staff>>()
    val staffList: LiveData<List<Staff>> get() = _staffList

    private val _securityList = MutableLiveData<List<Security>>()
    val securityList: LiveData<List<Security>> get() = _securityList

    private val _visitorList = MutableLiveData<List<Visitor>>()
    val visitorList: LiveData<List<Visitor>> get() = _visitorList

    init {
        loadData()
    }

    fun loadData() {
        val activeStaffCount = AdminRoleManagementActivity.sharedStaffList.count { it.status == "Active" }
        val activeSecurityCount = AdminRoleManagementActivity.sharedSecurityList.count { it.status == "Active" }

        val staffList = AdminRoleManagementActivity.sharedStaffList.map { Staff(it.name, it.status) }
        val securityList = AdminRoleManagementActivity.sharedSecurityList.map { Security(it.name, it.status) }
        val visitorList = AdminRoleManagementActivity.visitorHistoryList // Use visitorHistoryList instead

        _staffCount.value = activeStaffCount
        _securityCount.value = activeSecurityCount

        _staffList.value = staffList
        _securityList.value = securityList
        _visitorList.value = visitorList

        updateVisitorStats("Today")

        Log.d("DashboardViewModel", "loadData: activeStaffCount=$activeStaffCount, activeSecurityCount=$activeSecurityCount, visitorCount=${visitorList.size}")
    }

    fun updateVisitorStats(filter: String) {
        // Use visitorHistoryList to include all visitors (checked-in and checked-out)
        val visitors = AdminRoleManagementActivity.visitorHistoryList

        Log.d("DashboardViewModel", "updateVisitorStats: Total visitors in visitorHistoryList: ${visitors.size}")
        visitors.forEach { visitor ->
            Log.d("DashboardViewModel", "Visitor: name=${visitor.name}, checkInTime=${visitor.checkInTime}, checkOutTime=${visitor.checkOutTime}, status=${visitor.status}")
        }

        val filteredVisitors = when (filter) {
            "Today" -> {
                val sdfInput = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val currentDateString = sdfOutput.format(Calendar.getInstance().time)
                Log.d("DashboardViewModel", "Current date (yyyyMMdd): $currentDateString")
                visitors.filter { visitor ->
                    if (visitor.checkInTime.isNullOrEmpty()) {
                        Log.w("DashboardViewModel", "Visitor ${visitor.name} has empty checkInTime, excluding from filter")
                        false
                    } else {
                        try {
                            val visitorDate = sdfInput.parse(visitor.checkInTime)
                            val visitorDateString = sdfOutput.format(visitorDate)
                            Log.d("DashboardViewModel", "Visitor date (yyyyMMdd): $visitorDateString for visitor: ${visitor.name}")
                            visitorDateString == currentDateString
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "Error parsing checkInTime for visitor: ${visitor.name}, checkInTime: ${visitor.checkInTime}", e)
                            false
                        }
                    }
                }
            }
            else -> {
                // Calculate the time range for filtering
                val calendar = Calendar.getInstance()
                val currentTime = calendar.timeInMillis
                val timeRangeDays = when (filter) {
                    "Last 7 Days" -> 7
                    "Last 30 Days" -> 30
                    else -> Int.MAX_VALUE // Show all visitors if filter is unknown
                }

                visitors.filter { visitor ->
                    if (visitor.checkInTime.isNullOrEmpty()) {
                        Log.w("DashboardViewModel", "Visitor ${visitor.name} has empty checkInTime, excluding from filter")
                        false
                    } else {
                        try {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val checkInDate = sdf.parse(visitor.checkInTime) ?: return@filter false
                            val checkInTimeMillis = checkInDate.time
                            val diffMillis = currentTime - checkInTimeMillis
                            val daysDiff = diffMillis / (1000 * 60 * 60 * 24)
                            Log.d("DashboardViewModel", "Visitor: ${visitor.name}, checkInTime=$checkInTimeMillis, currentTime=$currentTime, daysDiff=$daysDiff, timeRangeDays=$timeRangeDays")
                            daysDiff <= timeRangeDays
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "Error parsing checkInTime for visitor: ${visitor.name}, checkInTime: ${visitor.checkInTime}", e)
                            false
                        }
                    }
                }
            }
        }

        // Calculate statistics using the filtered visitors
        val checkIns = filteredVisitors.count { it.status == "Checked In" }
        val checkOuts = filteredVisitors.count { it.status == "Checked Out" }
        val total = filteredVisitors.size

        // Update LiveData with filtered data
        _checkIns.value = checkIns
        _checkOuts.value = checkOuts
        _totalVisitors.value = total
        _visitorList.value = filteredVisitors // Update with filtered list

        Log.d("DashboardViewModel", "updateVisitorStats: filter=$filter, checkIns=$checkIns, checkOuts=$checkOuts, totalVisitors=$total, filteredVisitors=${filteredVisitors.size}")
    }
}