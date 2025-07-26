package com.apmw.freshbrand.view.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.view.client.*
import com.apmw.freshbrand.viewmodel.AuthViewModel
import com.apmw.freshbrand.viewmodel.ProductViewModel
import com.apmw.freshbrand.viewmodel.CartViewModel
import com.apmw.freshbrand.viewmodel.OrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    user: User,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var openDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf<ClientBottomNavTab>(ClientBottomNavTab.Dashboard) }
    var selectedDrawerItem by remember { mutableStateOf<String?>(null) }

    val drawerItems = listOf(
        "Sachet Design" to Icons.Default.Palette,
        "Profile" to Icons.Default.Person
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Hello, ${user.name.split(" ").firstOrNull() ?: "User"} 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    drawerItems.forEach { (label, icon) ->
                        val isSelected = selectedDrawerItem == label
                        val background = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(background)
                                .clickable {
                                    selectedDrawerItem = label
                                    scope.launch { drawerState.close() }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }


        }

        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            selectedDrawerItem ?: selectedTab.title,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
//                        IconButton(onClick = {
//                            selectedTab = ClientBottomNavTab.Shop
//                            selectedDrawerItem = null
//                        }) {
//                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
//                        }
                        IconButton(onClick = { openDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        if (openDialog) {
                            AlertDialog(
                                onDismissRequest = { openDialog = false },
                                title = { Text("Sign Out") },
                                text = { Text("Are you sure you want to sign out?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        authViewModel.signOut()
                                        openDialog = false
                                    }) {
                                        Text("Yes", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { openDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    val tabs = listOf(
                        ClientBottomNavTab.Dashboard,
                        ClientBottomNavTab.Orders,
                        ClientBottomNavTab.Shop
                    )
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab && selectedDrawerItem == null,
                            onClick = {
                                selectedTab = tab
                                selectedDrawerItem = null
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
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
                when (selectedDrawerItem) {
                    "Sachet Design" -> CustomDesignScreen()
                    "Profile" -> ProfileScreen(user)
                    else -> when (selectedTab) {
                        is ClientBottomNavTab.Dashboard -> ClientDashboardContent(
                            user = user,
                            onNavigateToProducts = { selectedTab = ClientBottomNavTab.Shop },
                            onNavigateToCart = { selectedTab = ClientBottomNavTab.Shop },
                            onNavigateToOrders = { selectedTab = ClientBottomNavTab.Orders },
                            onNavigateToDesign = { selectedDrawerItem = "Sachet Design" },
                            orderViewModel = orderViewModel
                        )
                       // is ClientBottomNavTab.Orders -> Orders()
                        is ClientBottomNavTab.Orders -> OrdersScreen(orderViewModel)
                        is ClientBottomNavTab.Shop -> ShopTabScreen(productViewModel, cartViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopTabScreen(
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Products", "Cart")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> OrderProductsScreen(productViewModel)
            //1 -> Cart()
           1 -> CartScreen(cartViewModel)
        }
    }
}


@Composable
fun ClientDashboardContent(
    user: User,
    onNavigateToProducts: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToDesign: () -> Unit,
    orderViewModel: OrderViewModel
) {
    // Get real data from viewmodels

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Welcome back, ${user.name}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        user.companyName,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Manage your orders, browse products, and create custom designs.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Quick Actions Section


        // Recent Orders Card


        // Statistics Cards


        // Recent Activity Card

    }
}


@Composable
fun ProfileScreen(user: User) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = user.companyName,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Account Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow("Email", user.email, Icons.Default.Email)
                    ProfileInfoRow("Phone", user.phone, Icons.Default.Phone)
                    ProfileInfoRow("Address", user.address, Icons.Default.LocationOn)
                    ProfileInfoRow("GST Number", user.gstNumber, Icons.Default.ConfirmationNumber)
                    ProfileInfoRow("User Type", user.userType.name, Icons.Default.Badge)
                    ProfileInfoRow("Status", user.status.name, Icons.Default.CheckCircle)
                    ProfileInfoRow("Created On", formatTimestamp(user.createdDate), Icons.Default.CalendarToday)
                    ProfileInfoRow("Last Updated", formatTimestamp(user.updatedAt), Icons.Default.Update)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun Orders()
{
    Scaffold {paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                Text(text = "order screen")
            }
        }

    }

}

@Composable
fun Cart()
{
    Scaffold {paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                Text(text = "cart screen")
            }
        }

    }

}