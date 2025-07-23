// CheckoutScreen.kt
package com.apmw.freshbrand.view.client

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.model.*
import com.apmw.freshbrand.viewmodel.CartViewModel
import com.apmw.freshbrand.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onOrderSuccess: () -> Unit = {}
) {
    val cartUiState by cartViewModel.uiState.collectAsState()
    val orderUiState by orderViewModel.uiState.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.PAY_LATER) }
    var deliveryAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }
    var showOrderSummary by remember { mutableStateOf(false) }

    // Handle order placement success
    LaunchedEffect(orderUiState.orderPlaced) {
        if (orderUiState.orderPlaced) {
            cartViewModel.clearCart()
            orderViewModel.resetOrderPlaced()
            onOrderSuccess()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Items
        item {
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order Items (${cartUiState.itemCount})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showOrderSummary = !showOrderSummary }) {
                            Text(if (showOrderSummary) "Hide" else "Show")
                        }
                    }

                    if (showOrderSummary) {
                        Spacer(Modifier.height(8.dp))
                        cartUiState.cartItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.productName} (${item.quantity}g)")
                                Text("₹${item.totalPrice.toInt()}")
                            }
                        }
                    }
                }
            }
        }

        // Delivery Address
        item {
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Enter delivery address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Street, City, State, PIN Code") }
                    )
                }
            }
        }

        // Payment Methods
        item {
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    listOf(
                        PaymentMethod.RAZORPAY to Pair(Icons.Default.CreditCard, "Pay with Razorpay"),
                        PaymentMethod.PAY_LATER to Pair(Icons.Default.Schedule, "Pay Later")
                    ).forEach { (method, iconTextPair) ->
                        val (icon, label) = iconTextPair
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedPaymentMethod == method,
                                    onClick = { selectedPaymentMethod = method }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method }
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(label)
                                if (method == PaymentMethod.RAZORPAY) {
                                    Text("UPI, Cards, Net Banking", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    Text("Pay within 40 days after delivery", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notes
        item {
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Notes (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = orderNotes,
                        onValueChange = { orderNotes = it },
                        label = { Text("Any special instructions") },
                        placeholder = { Text("e.g., Fragile items, specific delivery time") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }

        // Summary
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:")
                        Text("₹${cartUiState.subtotal.toInt()}")
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax (18%):")
                        Text("₹${cartUiState.tax.toInt()}")
                    }

                    Divider(Modifier.padding(vertical = 8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("₹${cartUiState.totalAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Place Order Button
        item {
            Button(
                onClick = {
                    orderViewModel.placeOrder(
                        cartItems = cartUiState.cartItems,
                        customDesign = null,
                        paymentMethod = selectedPaymentMethod,
                        userAddress = deliveryAddress,
                        notes = orderNotes
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = deliveryAddress.isNotBlank() && !orderUiState.isPlacingOrder
            ) {
                if (orderUiState.isPlacingOrder) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Placing Order...")
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedPaymentMethod == PaymentMethod.RAZORPAY)
                            "Pay ₹${cartUiState.totalAmount.toInt()}"
                        else "Place Order"
                    )
                }
            }
        }

        // Error message
        if (orderUiState.error != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        orderUiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
