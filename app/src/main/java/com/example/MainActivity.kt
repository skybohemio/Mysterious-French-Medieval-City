package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BourgesGuideApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GuideViewModel
import com.stripe.android.PaymentConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Stripe SDK with publishable key from BuildConfig
        val stripePublishableKey = com.example.BuildConfig.STRIPE_PUBLISHABLE_KEY
        if (stripePublishableKey.isNotBlank() && stripePublishableKey != "pk_test_placeholder_stripe_key_123") {
            PaymentConfiguration.init(applicationContext, stripePublishableKey)
        } else {
            // Initializing with a dummy key for testing/preview compilation
            PaymentConfiguration.init(applicationContext, "pk_test_51PlaceholderStripeKey000000")
        }

        // Initialize OsmDroid configuration with application package name before any MapView is created
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName
        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        // Pre-install bundled offline map tiles and assets from APK
        com.example.ui.util.OfflineMapBundleManager.installOfflineMapBundle(applicationContext)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val guideViewModel: GuideViewModel = viewModel()
                BourgesGuideApp(
                    viewModel = guideViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

