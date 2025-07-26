package com.apmw.freshbrand

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apmw.freshbrand.model.AuthState
import com.apmw.freshbrand.model.Order
import com.apmw.freshbrand.view.LoginScreen
import com.apmw.freshbrand.view.SignUpScreen
import com.apmw.freshbrand.view.dashboard.DashboardScreen
import com.apmw.freshbrand.viewmodel.AuthViewModel

import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val RAZORPAY_KEY_ID = "rzp_test_Yc3rMhnOkYRH28" // Replace with your actual key
        private var instance: MainActivity? = null

        fun getInstance(): MainActivity? = instance
    }

    private var paymentSuccessCallback: ((String, String) -> Unit)? = null
    private var paymentErrorCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        enableEdgeToEdge()

        // Initialize Razorpay with error handling
        try {
            Checkout.preload(applicationContext)
            Log.d(TAG, "Razorpay preloaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload Razorpay", e)
        }

        setContent {
            AppTheme(dynamicColor = false) {
                val navController = rememberNavController()
                val viewModel: AuthViewModel = viewModel()
                val authState by viewModel.authState.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = if (authState is AuthState.Authenticated) "dashboard" else "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onNavigateToSignUp = { navController.navigate("signup") },
                            viewModel = viewModel
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            viewModel = viewModel
                        )
                    }

                    composable("dashboard") {
                        currentUser?.let { user ->
                            DashboardScreen(
                                user = user,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Handle auth state changes
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Authenticated -> {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        clearPaymentCallbacks()
    }

    // Set payment callbacks before initiating payment
    private fun setPaymentCallbacks(
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        paymentSuccessCallback = onSuccess
        paymentErrorCallback = onError
    }

    // Razorpay payment success callback
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Log.d(TAG, "Payment success: $razorpayPaymentId")
        razorpayPaymentId?.let { paymentId ->
            runOnUiThread {
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show()
                paymentSuccessCallback?.invoke(paymentId, "Payment successful")
            }
        }
        clearPaymentCallbacks()
    }

    // Razorpay payment error callback
    override fun onPaymentError(code: Int, response: String?) {
        Log.e(TAG, "Payment error: Code=$code, Response=$response")
        val errorMessage = when (code) {
            Checkout.NETWORK_ERROR -> "Network error. Please check your internet connection."
            Checkout.INVALID_OPTIONS -> "Invalid payment options. Please try again."
            Checkout.PAYMENT_CANCELED -> "Payment was cancelled."
            Checkout.TLS_ERROR -> "TLS/SSL error. Please update your app."
            else -> response ?: "Payment failed with code: $code"
        }

        runOnUiThread {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            paymentErrorCallback?.invoke(errorMessage)
        }
        clearPaymentCallbacks()
    }

    private fun clearPaymentCallbacks() {
        paymentSuccessCallback = null
        paymentErrorCallback = null
    }

    // Function to initiate Razorpay payment
    fun initiateRazorpayPayment(
        order: Order,
        onSuccess: (String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d(TAG, "Initiating Razorpay payment for order: ${order.id}")

        // Set callbacks
        setPaymentCallbacks(onSuccess, onFailure)

        try {
            val checkout = Checkout()
            checkout.setKeyID(RAZORPAY_KEY_ID)

            // Validate required order fields
            if (order.totalAmount <= 0) {
                throw IllegalArgumentException("Invalid order amount: ${order.totalAmount}")
            }

            val options = JSONObject().apply {
                put("name", "Fresh Brand")
                put("description", "Order #${order.id.take(8)}")
                put("image", "https://your-logo-url.com/logo.png") // Replace with your actual logo URL
                put("theme", JSONObject().apply {
                    put("color", "#3399cc")
                })
                put("currency", "INR")
                put("amount", (order.totalAmount * 100).toInt()) // Amount in paise

                // Customer details - handle null values
                put("prefill", JSONObject().apply {
                    put("email", order.userEmail ?: "")
                    put("contact", order.userPhone ?: "")
                })

                // Order notes
                put("notes", JSONObject().apply {
                    put("order_id", order.id)
                    put("user_id", order.userId)
                })

                // Additional options
                put("retry", JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 3)
                })
                put("send_sms_hash", true)
                put("remember_customer", false)
                put("timeout", 300) // 5 minutes timeout

                // Add method configuration
                put("method", JSONObject().apply {
                    put("netbanking", true)
                    put("card", true)
                    put("upi", true)
                    put("wallet", true)
                })
            }

            Log.d(TAG, "Payment options: $options")
            checkout.open(this, options)

        } catch (e: Exception) {
            Log.e(TAG, "Error initiating payment", e)
            val errorMessage = when (e) {
                is IllegalArgumentException -> e.message ?: "Invalid payment parameters"
                is org.json.JSONException -> "Error creating payment options"
                else -> "Payment initialization failed: ${e.message}"
            }

            runOnUiThread {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                onFailure(errorMessage)
                clearPaymentCallbacks()
            }
        }
    }
}

// Extension function to easily access payment functionality from Composables
fun Activity.startRazorpayPayment(
    order: Order,
    onSuccess: (String, String) -> Unit,
    onFailure: (String) -> Unit
) {
    if (this is MainActivity) {
        this.initiateRazorpayPayment(order, onSuccess, onFailure)
    } else {
        onFailure("Activity is not MainActivity")
    }
}