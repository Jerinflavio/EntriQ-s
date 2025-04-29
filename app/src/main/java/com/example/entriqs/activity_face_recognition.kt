package com.example.entriqs

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.entriqs.model.Visitor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.net.Uri
import android.graphics.ImageDecoder
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class FaceRecognitionActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var interpreter: Interpreter

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            try {
                val bitmap = loadBitmapFromUri(Uri.fromFile(File(externalCacheDir, "temp_photo.jpg")))
                if (bitmap == null) {
                    Log.e(TAG, "Failed to load captured photo: Bitmap is null")
                    resultText.text = getString(R.string.error_processing_image)
                    return@registerForActivityResult
                }
                imageView.setImageBitmap(bitmap)
                recognizeFace(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load captured photo: ${e.message}", e)
                resultText.text = getString(R.string.error_processing_image)
            }
        } else {
            Log.e(TAG, "Failed to capture photo")
            resultText.text = getString(R.string.error_processing_image)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)

        imageView = findViewById(R.id.capturedImage)
        resultText = findViewById(R.id.resultText)
        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)

        // Initialize TensorFlow Lite interpreter
        try {
            val mappedFile = FileUtil.loadMappedFile(this, "mobileFaceNet.tflite")
            interpreter = Interpreter(mappedFile)
            Log.d(TAG, "Model loaded successfully: mobileFaceNet.tflite")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}", e)
            resultText.text = getString(R.string.error_loading_model)
            return
        }

        takePhotoButton.setOnClickListener {
            if (checkCameraPermission()) {
                val photoFile = File(externalCacheDir, "temp_photo.jpg")
                val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileProvider", photoFile)
                takePictureLauncher.launch(photoUri)
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val photoFile = File(externalCacheDir, "temp_photo.jpg")
            val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileProvider", photoFile)
            takePictureLauncher.launch(photoUri)
        } else {
            resultText.text = getString(R.string.camera_permission_denied)
        }
    }

    private fun recognizeFace(capturedBitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.Default) {
            // Validate the captured bitmap
            if (capturedBitmap.isRecycled || capturedBitmap.width == 0 || capturedBitmap.height == 0) {
                Log.e(TAG, "Invalid captured bitmap: isRecycled=${capturedBitmap.isRecycled}, width=${capturedBitmap.width}, height=${capturedBitmap.height}")
                withContext(Dispatchers.Main) {
                    resultText.text = getString(R.string.error_processing_image)
                }
                return@launch
            }

            // Generate embedding for the captured image
            val capturedEmbedding = generateEmbedding(capturedBitmap, "captured image")
            if (capturedEmbedding == null) {
                Log.e(TAG, "Failed to generate embedding for captured image")
                withContext(Dispatchers.Main) {
                    resultText.text = getString(R.string.error_processing_image)
                }
                return@launch
            }
            Log.d(TAG, "Captured embedding size: ${capturedEmbedding.size}")

            // Load visitor history
            AdminRoleManagementActivity.loadLists(
                getSharedPreferences("StaffPrefs", MODE_PRIVATE),
                getSharedPreferences("SecurityPrefs", MODE_PRIVATE),
                getSharedPreferences("VisitorPrefs", MODE_PRIVATE),
                forceRefresh = true
            )

            Log.d(TAG, "visitorHistoryList size: ${AdminRoleManagementActivity.visitorHistoryList.size}")
            AdminRoleManagementActivity.visitorHistoryList.forEach { visitor ->
                Log.d(TAG, "Visitor: ${visitor.name}, photoUri: ${visitor.photoUri}")
            }

            // Find the best match
            var bestMatch: Pair<Visitor, Float>? = null
            for (visitor in AdminRoleManagementActivity.visitorHistoryList) {
                if (visitor.photoUri != null) {
                    try {
                        val photoUri = Uri.parse(visitor.photoUri)
                        val storedBitmap = loadBitmapFromUri(photoUri)
                        if (storedBitmap == null) {
                            Log.e(TAG, "Failed to load bitmap for ${visitor.name}: Bitmap is null")
                            continue
                        }
                        if (storedBitmap.isRecycled || storedBitmap.width == 0 || storedBitmap.height == 0) {
                            Log.e(TAG, "Invalid stored bitmap for ${visitor.name}: isRecycled=${storedBitmap.isRecycled}, width=${storedBitmap.width}, height=${storedBitmap.height}")
                            continue
                        }
                        val storedEmbedding = generateEmbedding(storedBitmap, "stored image for ${visitor.name}")
                        if (storedEmbedding == null) {
                            Log.e(TAG, "Failed to generate embedding for ${visitor.name}")
                            continue
                        }
                        Log.d(TAG, "Stored embedding size for ${visitor.name}: ${storedEmbedding.size}")

                        val distance = cosineDistance(capturedEmbedding, storedEmbedding)
                        Log.d(TAG, "Distance with ${visitor.name}: $distance")

                        if (distance < 0.6 && (bestMatch == null || distance < bestMatch.second)) {
                            bestMatch = Pair(visitor, distance)
                            Log.d(TAG, "New best match: ${visitor.name}, distance: $distance")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing photo for ${visitor.name}: ${e.message}", e)
                    }
                } else {
                    Log.e(TAG, "Photo URI is null for ${visitor.name}")
                }
            }

            withContext(Dispatchers.Main) {
                if (bestMatch != null) {
                    resultText.text = getString(
                        R.string.visitor_details_format,
                        getString(R.string.visitor_details),
                        bestMatch.first.name,
                        bestMatch.first.visitorId,
                        bestMatch.first.checkInTime,
                        bestMatch.first.status
                    )
                } else {
                    resultText.text = getString(R.string.no_match)
                }
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            Log.d(TAG, "Loading bitmap from URI: $uri")
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            Log.d(TAG, "Successfully loaded bitmap from URI: $uri")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from URI $uri: ${e.message}", e)
            null
        }
    }

    private fun generateEmbedding(bitmap: Bitmap, context: String): FloatArray? {
        try {
            Log.d(TAG, "Starting embedding generation for $context")
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 112, 112, true)
            Log.d(TAG, "Bitmap resized successfully for $context")

            val inputArray = FloatArray(112 * 112 * 3)
            val intValues = IntArray(112 * 112)
            resizedBitmap.getPixels(intValues, 0, 112, 0, 0, 112, 112)
            Log.d(TAG, "Bitmap converted to int array for $context: size=${intValues.size}")

            for (i in intValues.indices) {
                val pixel = intValues[i]
                inputArray[i * 3] = ((pixel shr 16 and 0xFF) / 255.0f) // R
                inputArray[i * 3 + 1] = ((pixel shr 8 and 0xFF) / 255.0f) // G
                inputArray[i * 3 + 2] = ((pixel and 0xFF) / 255.0f) // B
            }
            Log.d(TAG, "Pixel values normalized for $context: inputArray size=${inputArray.size}")

            val inputBuffer = ByteBuffer.allocateDirect(112 * 112 * 3 * 4).apply {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().put(inputArray)
                rewind()
            }
            Log.d(TAG, "Input buffer prepared for $context: capacity=${inputBuffer.capacity()}")

            val outputArray = FloatArray(128) // MobileFaceNet outputs a 128D embedding
            interpreter.run(inputBuffer, outputArray)
            Log.d(TAG, "Inference completed for $context: outputArray size=${outputArray.size}")

            return outputArray
        } catch (e: Exception) {
            Log.e(TAG, "Error generating embedding for $context: ${e.message}", e)
            return null
        }
    }

    private fun cosineDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in embedding1.indices) {
            val v1 = embedding1[i]
            val v2 = embedding2[i]
            dotProduct += v1 * v2
            norm1 += v1 * v1
            norm2 += v2 * v2
        }

        norm1 = sqrt(norm1)
        norm2 = sqrt(norm2)
        return if (norm1 == 0f || norm2 == 0f) Float.MAX_VALUE else 1f - (dotProduct / (norm1 * norm2))
    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }

    companion object {
        private const val TAG = "FaceRecognitionActivity"
        private const val CAMERA_PERMISSION_CODE = 100
    }
}