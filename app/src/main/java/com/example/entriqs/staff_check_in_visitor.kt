package com.example.entriqs

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.entriqs.databinding.ActivityStaffCheckInVisitorBinding
import com.example.entriqs.model.Visitor
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CheckInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffCheckInVisitorBinding
    private var photoUri: Uri? = null
    private lateinit var currentPhotoPath: String

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.visitorPhoto.setImageURI(photoUri)
        } else {
            Toast.makeText(this, getString(R.string.toast_photo_capture_cancelled), Toast.LENGTH_SHORT).show()
            photoUri = null
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: true
        if (cameraGranted && storageGranted) {
            capturePhoto()
        } else {
            if (!cameraGranted && !shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera permission required. Enable it in settings.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            } else {
                Toast.makeText(this, getString(R.string.toast_camera_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffCheckInVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdminRoleManagementActivity.loadLists(
            getSharedPreferences("StaffPrefs", MODE_PRIVATE),
            getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
            getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
            forceRefresh = false
        )

        setupToolbar()
        setupNavigationDrawer()
        setupPhotoCapture()
        setupCheckInButton()
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
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_check_out -> {
                    Toast.makeText(this, getString(R.string.toast_visitor_detail_clicked), Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_visitor_details -> {
                    Toast.makeText(this, getString(R.string.toast_visitor_detail_clicked), Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_statistics -> {
                    startActivity(Intent(this, StaffDashboardActivity::class.java))
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
        binding.navView.setCheckedItem(R.id.nav_check_in)
    }

    private fun setupPhotoCapture() {
        binding.capturePhotoButton.setOnClickListener {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissionsToRequest.isEmpty()) {
                capturePhoto()
            } else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "Camera permission required to capture photo.", Toast.LENGTH_LONG).show()
                }
                requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    private fun capturePhoto() {
        val photoFile: File? = createImageFile()
        photoFile?.let {
            photoUri = FileProvider.getUriForFile(this, "com.example.entriqs.fileprovider", it)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            takePictureLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            val image = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            currentPhotoPath = image.absolutePath
            image
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_failed_to_create_image), Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun setupCheckInButton() {
        binding.checkInButton.setOnClickListener {
            val name = binding.visitorNameInput.text.toString().trim()
            val phoneNumber = binding.phoneNumberInput.text.toString().trim()
            val purpose = binding.purposeInput.text.toString().trim()
            val destination = binding.destinationInput.text.toString().trim()
            val idNumber = binding.idNumberInput.text.toString().trim()

            if (name.isEmpty()) {
                binding.visitorNameInputLayout.error = getString(R.string.error_visitor_name_required)
                return@setOnClickListener
            } else {
                binding.visitorNameInputLayout.error = null
            }
            if (phoneNumber.isEmpty()) {
                binding.phoneNumberInputLayout.error = getString(R.string.error_phone_number_required)
                return@setOnClickListener
            } else {
                binding.phoneNumberInputLayout.error = null
            }
            if (purpose.isEmpty()) {
                binding.purposeInputLayout.error = getString(R.string.error_purpose_required)
                return@setOnClickListener
            } else {
                binding.purposeInputLayout.error = null
            }
            if (destination.isEmpty()) {
                binding.destinationInputLayout.error = getString(R.string.error_destination_required)
                return@setOnClickListener
            } else {
                binding.destinationInputLayout.error = null
            }

            val checkInTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val visitorId = UUID.randomUUID().toString()
            val visitor = Visitor(
                visitorId = visitorId,
                name = name,
                checkInTime = checkInTime,
                status = "Checked In",
                photoUri = photoUri?.toString(),
                phoneNumber = phoneNumber,
                purpose = purpose,
                destination = destination,
                idNumber = idNumber
            )

            Log.d("CheckInActivity", "Created visitor: $visitor")
            AdminRoleManagementActivity.sharedVisitorList.add(visitor)
            AdminRoleManagementActivity.visitorHistoryList.add(visitor)
            AdminRoleManagementActivity.saveLists(
                getSharedPreferences("StaffPrefs", MODE_PRIVATE),
                getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
                getSharedPreferences("VisitorPrefs", MODE_PRIVATE)
            )

            Toast.makeText(this, getString(R.string.visitor_added, name), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}