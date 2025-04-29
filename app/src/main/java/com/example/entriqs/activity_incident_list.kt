package com.example.entriqs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entriqs.databinding.ActivityIncidentListBinding
import com.example.entriqs.databinding.DialogIncidentDetailsBinding
import com.example.entriqs.databinding.ItemIncidentBinding
import com.example.entriqs.model.Incident

class IncidentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidentListBinding
    private val incidents = mutableListOf<Incident>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Load incidents
        loadIncidents()

        // Set up the RecyclerView
        binding.incidentListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.incidentListRecyclerView.adapter = IncidentAdapter(incidents) { incident ->
            showIncidentDetails(incident)
        }
    }

    private fun loadIncidents() {
        // Load incidents from SharedPreferences
        val prefs = getSharedPreferences("IncidentPrefs", MODE_PRIVATE)
        val incidentSet = prefs.getStringSet("incidents", emptySet())?.toMutableSet() ?: mutableSetOf()

        incidents.clear()
        incidentSet.forEach { incidentString ->
            Incident.fromString(incidentString)?.let { incidents.add(it) }
        }

        // Sort incidents by date (most recent first)
        incidents.sortByDescending { it.dateTime }
    }

    private fun showIncidentDetails(incident: Incident) {
        val dialogBinding = DialogIncidentDetailsBinding.inflate(LayoutInflater.from(this))
        dialogBinding.detailDateTime.text = "Date and Time: ${incident.dateTime}"
        dialogBinding.detailType.text = "Type: ${incident.type}"
        dialogBinding.detailLocation.text = "Location: ${incident.location}"
        dialogBinding.detailDescription.text = "Description: ${incident.description}"
        dialogBinding.detailPartiesInvolved.text = "Parties Involved: ${incident.partiesInvolved}"
        dialogBinding.detailActionsTaken.text = "Actions Taken: ${incident.actionsTaken}"
        dialogBinding.detailReportedBy.text = "Reported By: ${incident.reportedBy}"

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.incident_details_title))
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null)
            .show()
    }
}

class IncidentAdapter(
    private val incidents: List<Incident>,
    private val onItemClick: (Incident) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<IncidentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = ItemIncidentBinding.bind(itemView)

        fun bind(incident: Incident, onItemClick: (Incident) -> Unit) {
            binding.incidentDate.text = incident.dateTime
            binding.incidentType.text = incident.type
            binding.incidentLocation.text = "Location: ${incident.location}"
            binding.incidentDescription.text = "Description: ${incident.description}"

            binding.moreDetailsButton.setOnClickListener {
                onItemClick(incident)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(incidents[position], onItemClick)
    }

    override fun getItemCount(): Int = incidents.size
}