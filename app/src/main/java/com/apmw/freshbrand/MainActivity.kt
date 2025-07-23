package com.apmw.freshbrand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.runtime.*

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apmw.freshbrand.model.AuthState
import com.apmw.freshbrand.view.LoginScreen
import com.apmw.freshbrand.view.SignUpScreen
import com.apmw.freshbrand.view.dashboard.DashboardScreen
import com.apmw.freshbrand.viewmodel.AuthViewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
enableEdgeToEdge()
//        window.statusBarColor = android.graphics.Color.TRANSPARENT
//        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            AppTheme(dynamicColor = false){
            val navController = rememberNavController()
            val viewModel: AuthViewModel = viewModel()
            val authState by viewModel.authState.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            NavHost(
                navController, if (authState is AuthState.Authenticated) "dashboard" else "login"
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