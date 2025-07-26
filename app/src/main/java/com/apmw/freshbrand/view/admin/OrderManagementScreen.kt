// OrderManagementScreen.kt
package com.apmw.freshbrand.view.admin

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.model.CustomDesigns
import com.apmw.freshbrand.model.Order
import com.apmw.freshbrand.model.OrderStatus
import com.apmw.freshbrand.view.client.Realistic3DSachetPreview
import com.apmw.freshbrand.viewmodel.AdminOrderViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: AdminOrderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) } // null means "All"
    var searchQuery by remember { mutableStateOf("") }
    var showOrderDetails by remember { mutableStateOf(false) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    val filteredOrders = uiState.orders.filter { order ->
        (selectedFilter == null || order.status == selectedFilter) &&
                (searchQuery.isEmpty() || listOf(
                    order.userName,
                    order.userEmail,
                    order.id
                ).any { it.contains(searchQuery, ignoreCase = true) })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orders (${filteredOrders.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(
                            onClick = { viewModel.loadOrders() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { isFilterExpanded = !isFilterExpanded },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(if (isFilterExpanded) 180f else 0f),
                                tint = if (selectedFilter != null)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by customer, email, or order ID...", fontSize = 10.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            null,
                            modifier = Modifier.size(10.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                AnimatedVisibility(
                    visible = isFilterExpanded,
                    enter = expandVertically(
                        animationSpec = tween(300, easing = EaseInOutCubic)
                    ) + fadeIn(),
                    exit = shrinkVertically(
                        animationSpec = tween(300, easing = EaseInOutCubic)
                    ) + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Filter by Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        CompactFilterChips(
                            selectedFilter = selectedFilter,
                            orders = uiState.orders,
                            onFilterSelected = { selectedFilter = it }
                        )
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }

        if (uiState.isLoading) {
            LoadingState()
        } else if (filteredOrders.isEmpty()) {
            EmptyState(
                hasOrders = uiState.orders.isNotEmpty(),
                isFiltered = searchQuery.isNotEmpty() || selectedFilter != null
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredOrders) { order ->
                    EnhancedOrderCard(
                        order = order,
                        user = uiState.orderUsers[order.userId],
                        onClick = {
                            viewModel.selectOrder(order)
                            showOrderDetails = true
                        },
                        onViewInvoice = {
                            viewModel.selectOrder(order)
                            viewModel.showInvoice(true)
                        }
                    )
                }
            }
        }

        if (showOrderDetails && uiState.selectedOrder != null) {
            OrderDetailsDialog(
                order = uiState.selectedOrder!!,
                user = uiState.orderUsers[uiState.selectedOrder!!.userId],
                isUpdating = uiState.isUpdating,
                onUpdateStatus = { status -> viewModel.updateOrderStatus(uiState.selectedOrder!!.id, status) },
                onUpdateDeliveryDate = { date -> viewModel.updateDeliveryDate(uiState.selectedOrder!!.id, date) },
                onDeleteOrder = {
                    viewModel.deleteOrder(uiState.selectedOrder!!.id)
                    showOrderDetails = false
                },
                onDismiss = {
                    showOrderDetails = false
                    viewModel.clearSelectedOrder()
                }
            )
        }

        if (uiState.showInvoice && uiState.selectedOrder != null) {
            InvoiceDialog(
                order = uiState.selectedOrder!!,
                user = uiState.orderUsers[uiState.selectedOrder!!.userId],
                onDismiss = { viewModel.showInvoice(false) }
            )
        }
    }
}


@Composable
fun CompactFilterChips(
    selectedFilter: OrderStatus?,
    orders: List<Order>,
    onFilterSelected: (OrderStatus?) -> Unit
) {
    val allStatuses: List<OrderStatus?> = listOf(null) + OrderStatus.values().toList()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(allStatuses) { status ->
            val count = if (status == null) {
                orders.size
            } else {
                orders.count { it.status == status }
            }

            val isSelected = selectedFilter == status
            val label = status?.name?.lowercase()?.replaceFirstChar { it.uppercaseChar() } ?: "All"

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(status) },
                label = {
                    Text(
                        text = "$label ($count)",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                },
                modifier = Modifier.height(32.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedOrderCard(
    order: Order,
    user: com.apmw.freshbrand.model.User?,
    onClick: () -> Unit,
    onViewInvoice: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val statusColor = getStatusColor(order.status)

    // State to hold custom design data
    var customDesign by remember { mutableStateOf<com.apmw.freshbrand.model.CustomDesigns?>(null) }
    var isLoadingDesign by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        CustomDesignPreviewDialog(
            design = customDesign,
            onDismiss = { showDialog = false }
        )
    }

    // Fetch custom design data based on order's userId
    LaunchedEffect(order.userId) {
        if (order.userId.isNotEmpty()) {
            isLoadingDesign = true
            try {
                FirebaseFirestore.getInstance()
                    .collection("designs")
                    .document(order.userId) // Directly access document by UID
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            customDesign = document.toObject(CustomDesigns::class.java)
                        } else {
                            println("No design found for user ${order.userId}")
                        }
                        isLoadingDesign = false
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching design: ${e.message}")
                        isLoadingDesign = false
                    }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                isLoadingDesign = false
            }
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Order ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${user?.companyName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateFormatter.format(Date(order.orderDate)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  if(order.status == OrderStatus.DELIVERED) {
                      IconButton(
                          onClick = { onViewInvoice() },
                          modifier = Modifier
                              .size(12.dp)
                              .background(
                                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                  //  shape = CircleShape.createOutline(2,3,4,5,4.0)
                              )
                      ) {
                          Icon(
                              Icons.Default.Receipt,
                              contentDescription = "Invoice",
                              tint = MaterialTheme.colorScheme.primary,
                              modifier = Modifier.size(10.dp)
                          )
                      }
                  }
                    Spacer(Modifier.width(7.dp))
                    // Compact status badge
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Compact action buttons

                            Icon(
                                getStatusIcon(order.status),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = order.status.name,
                                color = statusColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer and Design Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user?.name ?: order.userName.ifEmpty { "Unknown Customer" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.email ?: order.userEmail,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Show custom design badge if design exists
//                if (customDesign != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
//                        Row(
//                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
////                            Icon(
////                                Icons.Default.Palette,
////                                contentDescription = null,
////                                modifier = Modifier.size(10.dp),
////                                tint = MaterialTheme.colorScheme.tertiary
////                            )
////                            Spacer(modifier = Modifier.width(3.dp))
////                            Text(
////                                text = "Custom",
////                                fontSize = 10.sp,
////                                color = MaterialTheme.colorScheme.tertiary,
////                                fontWeight = FontWeight.Medium
////                            )
//                        }
                  //  }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "₹${order.totalAmount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${order.items.size} item${if (order.items.size != 1) "s" else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (order.deliveryDate > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Delivery",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = dateFormatter.format(Date(order.deliveryDate)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Progress Indicator
            if (order.status != OrderStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = getOrderProgress(order.status),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.2f)
                )
            }

            // Preview Design Button Section
//            if (customDesign != null) {
                Spacer(modifier = Modifier.height(12.dp))

                // Add a subtle divider
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Simplified outlined preview button
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Preview Design",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
              //  }
            }
        }
    }
}

// Simplified preview dialog that takes the design as parameter
@Composable
fun CustomDesignPreviewDialog(
    design: com.apmw.freshbrand.model.CustomDesigns?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Custom Design Preview")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (design != null) {
                    Realistic3DSachetPreview(
                        title = design.title,
                        colorHex = design.colorHex,
                        logoUrl = design.logoUrl
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Design Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Design Name: ${design.designName}")
                            Text("Title: ${design.title}")
                            Text("Color: ${design.colorHex}")
                            if (design.logoUrl.isNotEmpty()) {
                                Text("Custom Logo: Included")
                            }
                        }
                    }
                } else {
                    Text("No custom design found for this order.")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.CONFIRMED -> Color(0xFF2196F3)
        OrderStatus.SHIPPED -> Color(0xFF9C27B0)
        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> Color(0xFFF44336)
    }
}

fun getStatusIcon(status: OrderStatus): ImageVector {
    return when (status) {
        OrderStatus.PENDING -> Icons.Default.Schedule
        OrderStatus.CONFIRMED -> Icons.Default.CheckCircle
        OrderStatus.SHIPPED -> Icons.Default.LocalShipping
        OrderStatus.DELIVERED -> Icons.Default.Done
        OrderStatus.CANCELLED -> Icons.Default.Cancel
    }
}

fun getOrderProgress(status: OrderStatus): Float {
    return when (status) {
        OrderStatus.PENDING -> 0.25f
        OrderStatus.CONFIRMED -> 0.5f
        OrderStatus.SHIPPED -> 0.75f
        OrderStatus.DELIVERED -> 1.0f
        OrderStatus.CANCELLED -> 0.0f
    }
}

fun Double.formatCurrency(): String {
    return "₹${String.format("%.2f", this)}"
}

fun Long.formatDate(): String {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormatter.format(Date(this))
}

fun Long.formatDateTime(): String {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return dateFormatter.format(Date(this))
}

@Composable
fun EmptyState(
    hasOrders: Boolean,
    isFiltered: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                if (isFiltered) Icons.Default.SearchOff else Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isFiltered) "No matching orders" else "No orders yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isFiltered) {
                    "Try adjusting your filters or search terms"
                } else {
                    "Orders will appear here when customers place them"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Loading orders...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}