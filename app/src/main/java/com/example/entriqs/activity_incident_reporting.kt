package com.example.entriqs

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.entriqs.databinding.ActivityIncidentReportingBinding
import com.example.entriqs.model.Incident
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentReportingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidentReportingBinding
    private val incidentTypes = arrayOf("Theft", "Vandalism", "Unauthorized Access", "Physical Altercation", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentReportingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Set up the incident type spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, incidentTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.incidentTypeSpinner.adapter = adapter

        // Pre-fill date and time with current timestamp
        val currentDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        binding.dateTimeInput.setText(currentDateTime)

        // Handle submit button click
        binding.submitIncidentButton.setOnClickListener {
            submitIncident()
        }
    }

    private fun submitIncident() {
        // Collect input data
        val dateTime = binding.dateTimeInput.text.toString().trim()
        val location = binding.locationInput.text.toString().trim()
        val incidentType = binding.incidentTypeSpinner.selectedItem.toString()
        val description = binding.descriptionInput.text.toString().trim()
        val partiesInvolved = binding.partiesInvolvedInput.text.toString().trim()
        val actionsTaken = binding.actionsTakenInput.text.toString().trim()
        val reportedBy = binding.reportedByInput.text.toString().trim()

        // Validate input
        if (dateTime.isEmpty() || location.isEmpty() || description.isEmpty() || reportedBy.isEmpty()) {
            Toast.makeText(this, getString(R.string.incident_submission_failed), Toast.LENGTH_SHORT).show()
            return
        }

        // Create Incident object
        val incident = Incident(
            dateTime = dateTime,
            location = location,
            description = description,
            type = incidentType,
            partiesInvolved = partiesInvolved,
            actionsTaken = actionsTaken,
            reportedBy = reportedBy
        )

        // Save to SharedPreferences
        saveIncident(incident)

        // Show success message and finish activity
        Toast.makeText(this, getString(R.string.incident_submitted), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveIncident(incident: Incident) {
        val prefs = getSharedPreferences("IncidentPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        val incidentSet = prefs.getStringSet("incidents", emptySet())?.toMutableSet() ?: mutableSetOf()

        // Add the new incident
        incidentSet.add(incident.toString())
        editor.putStringSet("incidents", incidentSet)
        editor.apply()

        // Optionally, update a global list in AdminRoleManagementActivity if needed
        // For example: AdminRoleManagementActivity.incidentList.add(incident)
    }
}