package com.apmw.freshbrand.view.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.model.*
import com.apmw.freshbrand.viewmodel.CartViewModel
import com.apmw.freshbrand.viewmodel.OrderViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.text.font.FontWeight

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

    var deliveryAddress by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }
    var showOrderSummary by remember { mutableStateOf(false) }

    // 🔄 Fetch current user's address from Firestore
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val addressFromFirestore = document.getString("address") ?: ""
                    deliveryAddress = addressFromFirestore
                }
                .addOnFailureListener {
                    // Optional: Handle error (e.g., log, toast)
                }
        }
    }

    // ✅ Handle order placed success
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
        // Order Items Summary
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

        // Delivery Address (Read-only)
        item {
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = {}, // Read-only
                        readOnly = true,
                        label = { Text("Delivery Address") },
                        placeholder = { Text("Street, City, State, PIN Code") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        }

        // Order Notes
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

        // Order Summary
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

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Note: You can choose payment method after placing the order",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
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
                        paymentMethod = PaymentMethod.PAY_LATER, // Default to pay later
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
                    Text("Place Order")
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