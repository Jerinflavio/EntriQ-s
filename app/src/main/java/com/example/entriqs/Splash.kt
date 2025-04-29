package com.example.entriqs

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.example.entriqs.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L // 3 seconds
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "SplashActivity onCreate called")

        // Enable edge-to-edge display
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        supportActionBar?.hide()

        // Animate Logo
        binding.logo.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f).apply {
                duration = 1000
                start()
            }
            ObjectAnimator.ofFloat(this, View.SCALE_X, 0.5f, 1f).apply {
                duration = 1000
                start()
            }
            ObjectAnimator.ofFloat(this, View.SCALE_Y, 0.5f, 1f).apply {
                duration = 1000
                start()
            }
        }

        // Animate App Name
        binding.appName.apply {
            alpha = 0f
            translationY = 50f
            ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f).apply {
                duration = 1000
                startDelay = 500
                start()
            }
            ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 50f, 0f).apply {
                duration = 1000
                startDelay = 500
                start()
            }
        }

        // Navigate to LoginActivity after splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                Log.d(TAG, "Navigating to LoginActivity")
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the task stack
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                startActivity(intent, options.toBundle())
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to LoginActivity: ${e.message}")
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, splashDuration)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "SplashActivity onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SplashActivity onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "SplashActivity onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "SplashActivity onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SplashActivity onDestroy called")
    }
}