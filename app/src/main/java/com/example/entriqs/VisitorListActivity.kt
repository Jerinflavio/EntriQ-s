package com.example.entriqs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat // Added for ViewCompat.generateViewId()
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entriqs.adapter.VisitorLogAdapter
import com.example.entriqs.databinding.LayoutVisitorListContainerBinding
import com.example.entriqs.model.ListItem

class VisitorListActivity : AppCompatActivity() {

    private lateinit var binding: LayoutVisitorListContainerBinding
    private lateinit var recyclerView: RecyclerView
    private val adapter = VisitorLogAdapter { visitor ->
        val visitorObj = visitor.visitor
        if (!visitorObj.isValid()) {
            Log.e("VisitorListActivity", "Invalid visitor clicked: $visitorObj")
            Toast.makeText(this, "Error: Invalid visitor data", Toast.LENGTH_SHORT).show()
            return@VisitorLogAdapter
        }
        val intent = Intent(this, VisitorDetailsActivity::class.java)
        Log.d("VisitorListActivity", "Launching VisitorDetailsActivity to show all visitors")
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutVisitorListContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dynamically add RecyclerView to content_container
        recyclerView = RecyclerView(this).apply {
            id = ViewCompat.generateViewId() // Use ViewCompat.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(8, 8, 8, 8)
        }
        binding.contentContainer.addView(recyclerView)

        // Set toolbar title from custom attribute
        val attrs = intArrayOf(R.attr.toolbarTitle)
        val typedArray = obtainStyledAttributes(attrs)
        val toolbarTitle = typedArray.getString(0) ?: "Visitor List"
        typedArray.recycle()

        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = true
        )

        setupRecyclerView()
        setupToolbar(toolbarTitle)
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

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        updateRecyclerView()
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun updateRecyclerView() {
        val visitorList = AdminRoleManagementActivity.visitorHistoryList
            .filter { it.isValid() }
            .map { ListItem.VisitorItem(it) }
        adapter.submitList(visitorList)
        binding.emptyStateText.visibility = if (visitorList.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (visitorList.isEmpty()) View.GONE else View.VISIBLE
        Log.d("VisitorListActivity", "Updated RecyclerView with ${visitorList.size} valid visitors")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}