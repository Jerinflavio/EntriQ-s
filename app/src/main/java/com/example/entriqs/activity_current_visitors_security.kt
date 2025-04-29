package com.example.entriqs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.databinding.ActivityCurrentVisitorsSecurityBinding
import com.example.entriqs.databinding.ActivityItemCurrentVisitorSecurityBinding
import com.example.entriqs.model.Visitor
import com.google.gson.Gson

class CurrentVisitorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrentVisitorsSecurityBinding
    private val visitors = mutableListOf<Visitor>()
    private val STORAGE_PERMISSION_CODE = 100
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentVisitorsSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            loadVisitors()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVisitors()
            } else {
                Log.w("CurrentVisitorsActivity", "Storage permission denied, photos may not load")
                loadVisitors()
            }
        }
    }

    private fun loadVisitors() {
        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Load visitor data
        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )

        // Filter for current visitors (status "Checked In")
        visitors.clear()
        visitors.addAll(AdminRoleManagementActivity.visitorHistoryList.filter {
            it.isValid() && it.status == "Checked In"
        })

        // Log the loaded visitors to check isVerified
        visitors.forEach { visitor ->
            Log.d("CurrentVisitorsActivity", "Loaded visitor: ${visitor.name}, isVerified: ${visitor.isVerified}")
        }

        // Set up the RecyclerView
        val adapter = CurrentVisitorsAdapter(visitors) { visitor ->
            // Callback to update the visitor when verified
            val updatedVisitor = visitor.copy(isVerified = true)
            val index = AdminRoleManagementActivity.visitorHistoryList.indexOfFirst { it.visitorId == updatedVisitor.visitorId }
            if (index != -1) {
                AdminRoleManagementActivity.visitorHistoryList[index] = updatedVisitor
                AdminRoleManagementActivity.saveLists(
                    getSharedPreferences("StaffPrefs", MODE_PRIVATE),
                    getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
                    getSharedPreferences("VisitorPrefs", MODE_PRIVATE)
                )
                // Log the updated visitor to confirm isVerified is true
                Log.d("CurrentVisitorsActivity", "Updated visitor: ${updatedVisitor.name}, isVerified: ${updatedVisitor.isVerified}")
                loadVisitors() // Refresh the list
            }
        }
        binding.currentVisitorsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.currentVisitorsRecyclerView.adapter = adapter
    }
}

class CurrentVisitorsAdapter(
    private val visitors: List<Visitor>,
    private val onVisitorVerified: (Visitor) -> Unit // Callback to notify when a visitor is verified
) : androidx.recyclerview.widget.RecyclerView.Adapter<CurrentVisitorsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = ActivityItemCurrentVisitorSecurityBinding.bind(itemView)

        fun bind(visitor: Visitor, onVisitorVerified: (Visitor) -> Unit) {
            binding.visitorIdValue.text = visitor.visitorId
            binding.visitorName.text = visitor.name
            binding.checkInValue.text = visitor.checkInTime
            binding.checkOutValue.text = visitor.checkOutTime ?: "N/A"
            binding.statusValue.text = visitor.status
            binding.phoneValue.text = visitor.phoneNumber
            binding.purposeValue.text = visitor.purpose
            binding.destinationValue.text = visitor.destination
            binding.idNumberValue.text = visitor.idNumber

            // Load photo if available
            if (!visitor.photoUri.isNullOrEmpty()) {
                Log.d("CurrentVisitorsAdapter", "Loading photo for ${visitor.name}: ${visitor.photoUri}")
                try {
                    binding.visitorPhoto.setImageURI(android.net.Uri.parse(visitor.photoUri))
                } catch (e: Exception) {
                    Log.e("CurrentVisitorsAdapter", "Failed to load photo for ${visitor.name}: ${e.message}")
                    binding.visitorPhoto.setImageResource(R.drawable.placeholder_image)
                }
            } else {
                Log.w("CurrentVisitorsAdapter", "No photo URI for ${visitor.name}")
                binding.visitorPhoto.setImageResource(R.drawable.placeholder_image)
            }

            // Display verification status
            if (visitor.isVerified) {
                binding.verificationStatusValue.text = itemView.context.getString(R.string.verified)
                binding.verificationStatusValue.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                binding.verifyCheckInButton.visibility = View.GONE
            } else {
                binding.verificationStatusValue.text = itemView.context.getString(R.string.pending_verification)
                binding.verificationStatusValue.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                binding.verifyCheckInButton.visibility = View.VISIBLE
            }

            // Handle verify button click
            binding.verifyCheckInButton.setOnClickListener {
                val updatedVisitor = visitor.copy(isVerified = true)
                onVisitorVerified(updatedVisitor)
            }
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_current_visitor_security, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(visitors[position], onVisitorVerified)
    }

    override fun getItemCount(): Int = visitors.size
}