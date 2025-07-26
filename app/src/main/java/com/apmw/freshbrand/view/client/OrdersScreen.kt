// OrdersScreen.kt
package com.apmw.freshbrand.view.client

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.MainActivity
import com.apmw.freshbrand.model.*
import com.apmw.freshbrand.viewmodel.OrderViewModel
import com.apmw.freshbrand.startRazorpayPayment
import com.apmw.freshbrand.view.admin.InvoiceDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    orderViewModel: OrderViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    currentUser: User? = null
) {
    val orderUiState by orderViewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        orderViewModel.loadOrders()
    }

    if (showPaymentDialog && selectedOrder != null) {
        PaymentMethodDialog(
            order = selectedOrder!!,
            onDismiss = {
                showPaymentDialog = false
                selectedOrder = null
            },
            onPaymentMethodSelected = { order, paymentMethod ->
                showPaymentDialog = false
                if (paymentMethod == PaymentMethod.RAZORPAY) {
                    // Payment will be handled in the dialog
                } else {
                    // Handle PAY_LATER
                }
                selectedOrder = null
            },
            orderViewModel = orderViewModel
        )
    }

    if (showInvoiceDialog && selectedOrder != null) {
        InvoiceDialog(
            order = selectedOrder!!,
            user = currentUser,
            onDismiss = {
                showInvoiceDialog = false
                selectedOrder = null
            }
        )
    }

    Scaffold { padding ->
        when {
            orderUiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            orderUiState.orders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No orders found",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Your order history will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orderUiState.orders) { order ->
                        OrderCard(
                            order = order,
                            onPayNow = {
                                selectedOrder = order
                                showPaymentDialog = true
                            },
                            onViewInvoice = {
                                selectedOrder = order
                                showInvoiceDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onPayNow: () -> Unit = {},
    onViewInvoice: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Invoice button for delivered orders
                    if (order.status == OrderStatus.DELIVERED) {
                        IconButton(
                            onClick = onViewInvoice,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = "View Invoice",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    OrderStatusChip(status = order.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order Date
            Text(
                text = "Ordered on ${formatTimestamp(order.orderDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Order Items Summary
            Text(
                text = "${order.items.size} items • ₹${order.totalAmount.toInt()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            // Show first few items
            order.items.take(3).forEach { item ->
                Text(
                    text = "• ${item.productName} (${item.quantity}g)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order.items.size > 3) {
                Text(
                    text = "... and ${order.items.size - 3} more items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    when (order.paymentStatus) {
                        PaymentStatus.PAID -> Icons.Default.CheckCircle
                        PaymentStatus.PENDING -> Icons.Default.Schedule
                        PaymentStatus.FAILED -> Icons.Default.Error
                        PaymentStatus.REFUNDED -> Icons.Default.Refresh
                    },
                    contentDescription = null,
                    tint = when (order.paymentStatus) {
                        PaymentStatus.PAID -> Color.Green
                        PaymentStatus.PENDING -> Color.Yellow
                        PaymentStatus.FAILED -> Color.Red
                        PaymentStatus.REFUNDED -> Color.Blue
                    },
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Payment: ${order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Delivery Timeline
            if (order.deliveryTimeline.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = order.deliveryTimeline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                // Track Order Button
//                OutlinedButton(
//                    onClick = { /* TODO: Track order */ },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Icon(Icons.Default.Visibility, contentDescription = null)
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Track")
//                }

                // Pay Now Button (only for delivered orders with pending payment)
                if (order.status == OrderStatus.DELIVERED && order.paymentStatus == PaymentStatus.PENDING) {
                    Button(
                        onClick = onPayNow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Now")
                    }
                }

                // Reorder Button (if delivered)
//                if (order.status == OrderStatus.DELIVERED) {
//                    Button(
//                        onClick = { /* TODO: Reorder */ },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Icon(Icons.Default.Refresh, contentDescription = null)
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text("Reorder")
//                    }
//                }
            }
        }
    }
}

@Composable
fun PaymentMethodDialog(
    order: Order,
    onDismiss: () -> Unit,
    onPaymentMethodSelected: (Order, PaymentMethod) -> Unit,
    orderViewModel: OrderViewModel
) {
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.RAZORPAY) }
    var isPaymentInProgress by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isPaymentInProgress) onDismiss() },
        title = {
            Text(
                "Choose Payment Method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Order Total: ₹${order.totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Payment method selection
                listOf(
                    PaymentMethod.RAZORPAY to Pair(Icons.Default.CreditCard, "Pay with Razorpay"),
                    PaymentMethod.PAY_LATER to Pair(Icons.Default.Schedule, "Continue with Pay Later")
                ).forEach { (method, iconTextPair) ->
                    val (icon, label) = iconTextPair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedPaymentMethod == method,
                                onClick = { if (!isPaymentInProgress) selectedPaymentMethod = method }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == method,
                            onClick = { if (!isPaymentInProgress) selectedPaymentMethod = method },
                            enabled = !isPaymentInProgress
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(label)
                            if (method == PaymentMethod.RAZORPAY) {
                                Text("UPI, Cards, Net Banking, Wallets", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Pay later with in due", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (isPaymentInProgress) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            "Processing payment...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPaymentMethod == PaymentMethod.RAZORPAY) {
                        isPaymentInProgress = true
                        (context as? MainActivity)?.initiateRazorpayPayment(
                            order = order,
                            onSuccess = { paymentId, razorpayOrderId ->
                                isPaymentInProgress = false
                                orderViewModel.updatePaymentStatus(order.id, paymentId, razorpayOrderId)
                                onDismiss()
                            },
                            onFailure = { error ->
                                isPaymentInProgress = false
                                // You can show a snackbar or toast here
                                // For now, just dismiss the dialog
                                onDismiss()
                            }
                        )
                    } else {
                        onPaymentMethodSelected(order, selectedPaymentMethod)
                    }
                },
                enabled = !isPaymentInProgress
            ) {
                if (isPaymentInProgress && selectedPaymentMethod == PaymentMethod.RAZORPAY) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        if (selectedPaymentMethod == PaymentMethod.RAZORPAY)
                            "Pay ₹${order.totalAmount.toInt()}"
                        else "Continue"
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPaymentInProgress
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        OrderStatus.SHIPPED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}