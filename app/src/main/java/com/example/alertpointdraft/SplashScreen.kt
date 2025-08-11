package com.example.alertpointdraft // Make sure this matches your package name

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
// Remove Toast import if not explicitly used for other debugging
// import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SplashScreen : AppCompatActivity() {

    private lateinit var mainSplashContent: RelativeLayout
    private lateinit var noInternetView: View
    private lateinit var progressBar: ProgressBar
    private lateinit var tryAgainButton: Button
    private lateinit var logo: ImageView
    private lateinit var appName: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var progressRunnable: Runnable // Declare here

    private var currentProgress = 0
    private val SIMULATED_PROGRESS_INCREMENT = 10
    private val SIMULATED_DELAY_MS = 1500L

    private lateinit var floodContainer: View
    private lateinit var wave1: ImageView
    private lateinit var wave2: ImageView
    private lateinit var wave3: ImageView

    @SuppressLint("MissingInflatedId") // Ensure all IDs used below are correct in your XML
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Initialize Views
        mainSplashContent = findViewById(R.id.mainSplashContent)
        noInternetView = findViewById(R.id.noInternetView)
        progressBar = findViewById(R.id.progressBar)
        logo = findViewById(R.id.alertpoint_logo)
        floodContainer = findViewById(R.id.floodContainer) // Ensure this ID is correct
        tryAgainButton = findViewById(R.id.tryAgainButton)

        // Initialize progressRunnable HERE, before it can be used by checkInternetAndProceed()
        // which might call showNoInternetView() or startLoadingProcess()
        progressRunnable = object : Runnable {
            override fun run() {
                if (isInternetAvailable()) { // Double-check internet before progressing/navigating
                    if (currentProgress < 100) {
                        currentProgress += SIMULATED_PROGRESS_INCREMENT
                        progressBar.progress = currentProgress

                        // Your animation calls if needed
                        startWaveMotion()
                        animateRisingWater()

                        handler.postDelayed(this, SIMULATED_DELAY_MS)
                    } else {
                        navigateToNextScreen()
                    }
                } else {
                    // Internet was lost during the loading process
                    showNoInternetView()
                }
            }
        }

        tryAgainButton.setOnClickListener {
            noInternetView.visibility = View.GONE
            mainSplashContent.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true
            Log.d("SplashScreen", "Try Again: ProgressBar visible and indeterminate")

            handler.postDelayed({
                Log.d("SplashScreen", "Try Again Handler: Executing checkInternetAndProceed")
                progressBar.progress = 0 // Setting progress makes it determinate
                currentProgress = 0
                // progressBar.isIndeterminate = false; // Explicitly if needed

                checkInternetAndProceed()
            }, 300)
        }

        // Now it's safe to call checkInternetAndProceed
        checkInternetAndProceed()
    }


    private fun checkInternetAndProceed() {
        if (isInternetAvailable()) {
            showMainSplashContent()
            startLoadingProcess()
        } else {
            showNoInternetView()
        }
    }

    private fun showMainSplashContent() {
        mainSplashContent.visibility = View.VISIBLE
        logo.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        floodContainer.visibility = View.VISIBLE

        noInternetView.visibility = View.GONE
        progressBar.progress = 0
        currentProgress = 0
    }

    private fun showNoInternetView() {
        mainSplashContent.visibility = View.GONE
        // Ensure child views of mainSplashContent are also hidden
        logo.visibility = View.GONE

        progressBar.visibility = View.GONE
        floodContainer.visibility = View.GONE

        noInternetView.visibility = View.VISIBLE
        handler.removeCallbacks(progressRunnable) // Stop any ongoing progress simulation
    }

    private fun startLoadingProcess() {
        // Reset progress
        currentProgress = 0
        progressBar.progress = 0
        progressBar.visibility = View.VISIBLE // Make sure progress bar is visible

        // --- REPLACE THIS WITH YOUR ACTUAL LOADING LOGIC ---
        // For example, network calls, database initialization, etc.
        // Update progressBar.progress based on the actual progress of these tasks.
        // If your tasks are asynchronous, update progress in their callbacks/completion listeners.

        // Start the simulated progress
        handler.post(progressRunnable)
    }

    private fun navigateToNextScreen() {
        // Ensure this is only called once
        if (!isFinishing && !isChangingConfigurations) {
            val intent = Intent(this, SignIn::class.java) // Replace with your actual main activity
            startActivity(intent)
            finish() // Finish splash screen so user can't navigate back to it
        }
    }


    private fun startWaveMotion() {
        animateWave(findViewById(R.id.wave1), -600f, 800L)
        animateWave(findViewById(R.id.wave2), -800f, 1150L)
        animateWave(findViewById(R.id.wave3), -1000f, 1500L)
    }

    private fun animateWave(wave: ImageView, translationX: Float, duration: Long) {
        val animator = ObjectAnimator.ofFloat(wave, "translationX", 0f, translationX)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun animateRisingWater() {
        handler.post(object : Runnable {
            override fun run() {
                if (currentProgress <= 100) {
                    currentProgress += 2
                    val maxHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
                    val currentHeight = (maxHeight * currentProgress / 100)

                    floodContainer.layoutParams.height = currentHeight
                    floodContainer.requestLayout()
                    handler.postDelayed(this, 50)
                }
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                // Check for VPN transport if applicable
                // activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Always remove callbacks from the handler to prevent memory leaks
        // and unexpected behavior if the activity is destroyed.
        handler.removeCallbacks(progressRunnable)
    }
}

