package com.apmw.freshbrand.view.admin


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apmw.freshbrand.model.User


import com.apmw.freshbrand.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    user: User,
    viewModel: AuthViewModel,
    onNavigateToProducts: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showUsersScreen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val tabs = listOf(
        DashboardTab("Dashboard", Icons.Default.Dashboard),
        DashboardTab("Products", Icons.Default.Inventory),
        DashboardTab("Orders", Icons.Default.ShoppingCart),
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Handle different screens
    when {
        showSettings -> {
            SettingsScreen(
                user = user,
                viewModel = viewModel,
                onNavigateBack = { showSettings = false }
            )
        }
        showUsersScreen -> {
            AdminUsersScreen(
                onNavigateBack = { showUsersScreen = false }
            )
        }
        else -> {
            // Main Dashboard
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Admin Panel",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        actions = {
                            IconButton(onClick = { showUsersScreen = true }) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Manage Users"
                                )
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 8.dp
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title, fontSize = 7.sp) },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTabIndex) {
                        0 -> DashboardHomeScreen(user = user)
                        1 -> AdminProductsScreen(
                            onNavigateBack = { selectedTabIndex = 0 }
                        )
                        2 -> OrderManagementScreen()
                    }
                }
            }
        }
    }
}

data class DashboardTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)