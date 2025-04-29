package com.example.entriqs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.databinding.ActivityVisitorSearchBinding
import com.example.entriqs.databinding.ActivityItemCurrentVisitorSecurityBinding
import com.example.entriqs.model.Visitor
import android.view.View

class VisitorSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitorSearchBinding
    private val allVisitors = mutableListOf<Visitor>()
    private val filteredVisitors = mutableListOf<Visitor>()
    private lateinit var adapter: VisitorSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitorSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Set up the RecyclerView and adapter first
        adapter = VisitorSearchAdapter(filteredVisitors)
        binding.visitorSearchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.visitorSearchResultsRecyclerView.adapter = adapter

        // Now load visitors
        loadVisitors()

        // Set up the SearchView
        binding.visitorSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterVisitors(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterVisitors(newText)
                return true
            }
        })
    }

    private fun loadVisitors() {
        // Load visitor data from SharedPreferences
        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )

        // Get all visitors (current and historical)
        allVisitors.clear()
        allVisitors.addAll(AdminRoleManagementActivity.visitorHistoryList.filter { it.isValid() })
        filteredVisitors.clear()
        filteredVisitors.addAll(allVisitors)
        adapter.notifyDataSetChanged()
    }

    private fun filterVisitors(query: String?) {
        filteredVisitors.clear()
        if (query.isNullOrEmpty()) {
            filteredVisitors.addAll(allVisitors)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredVisitors.addAll(allVisitors.filter {
                it.name.lowercase().contains(lowerCaseQuery) ||
                        it.visitorId.lowercase().contains(lowerCaseQuery) ||
                        it.phoneNumber.lowercase().contains(lowerCaseQuery) ||
                        it.purpose.lowercase().contains(lowerCaseQuery) ||
                        it.destination.lowercase().contains(lowerCaseQuery) ||
                        it.idNumber.lowercase().contains(lowerCaseQuery)
            })
        }
        adapter.notifyDataSetChanged()
    }
}

class VisitorSearchAdapter(
    private val visitors: List<Visitor>
) : androidx.recyclerview.widget.RecyclerView.Adapter<VisitorSearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = ActivityItemCurrentVisitorSecurityBinding.bind(itemView)

        fun bind(visitor: Visitor) {
            binding.visitorIdValue.text = visitor.visitorId
            binding.visitorName.text = visitor.name
            binding.checkInValue.text = visitor.checkInTime
            binding.checkOutValue.text = visitor.checkOutTime ?: "N/A"
            binding.statusValue.text = visitor.status
            binding.phoneValue.text = visitor.phoneNumber
            binding.purposeValue.text = visitor.purpose
            binding.destinationValue.text = visitor.destination
            binding.idNumberValue.text = visitor.idNumber

            // Show verification status in search results
            binding.verificationStatusLabel.visibility = View.VISIBLE
            binding.verificationStatusValue.visibility = View.VISIBLE
            if (visitor.isVerified) {
                binding.verificationStatusValue.text = itemView.context.getString(R.string.verified)
                binding.verificationStatusValue.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            } else {
                binding.verificationStatusValue.text = itemView.context.getString(R.string.pending_verification)
                binding.verificationStatusValue.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            }

            // Keep the verify button hidden since we don't want to allow verification from search
            binding.verifyCheckInButton.visibility = View.GONE

            // Load photo if available
            if (!visitor.photoUri.isNullOrEmpty()) {
                try {
                    binding.visitorPhoto.setImageURI(android.net.Uri.parse(visitor.photoUri))
                } catch (e: Exception) {
                    binding.visitorPhoto.setImageResource(R.drawable.placeholder_image)
                }
            } else {
                binding.visitorPhoto.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_current_visitor_security, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(visitors[position])
    }

    override fun getItemCount(): Int = visitors.size
}