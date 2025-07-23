// OrderViewModel.kt
package com.apmw.freshbrand.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apmw.freshbrand.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val orders = firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                    .toObjects(Order::class.java)
                    .sortedByDescending { it.orderDate }

                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        customDesign: CustomDesigns?,
        paymentMethod: PaymentMethod,
        userAddress: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPlacingOrder = true)

                // Get user details
                val user = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                    .toObject(User::class.java)

                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "User not found",
                        isPlacingOrder = false
                    )
                    return@launch
                }

                val subtotal = cartItems.sumOf { it.totalPrice }
                val tax = subtotal * 0.18
                val totalAmount = subtotal + tax

                val orderId = firestore.collection("orders").document().id

                val order = Order(
                    id = orderId,
                    userId = userId,
                    userName = user.name,
                    userEmail = user.email,
                    userPhone = user.phone,
                    userAddress = userAddress,
                    items = cartItems,
                    customDesign = customDesign,
                    subtotal = subtotal,
                    tax = tax,
                    totalAmount = totalAmount,
                    status = OrderStatus.PENDING,
                    paymentMethod = paymentMethod,
                    paymentStatus = if (paymentMethod == PaymentMethod.PAY_LATER) PaymentStatus.PENDING else PaymentStatus.PENDING,
                    notes = notes
                )

                firestore.collection("orders")
                    .document(orderId)
                    .set(order)
                    .await()

                // Update product stock
                updateProductStock(cartItems)

                _uiState.value = _uiState.value.copy(
                    isPlacingOrder = false,
                    orderPlaced = true
                )

                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isPlacingOrder = false
                )
            }
        }
    }

    private suspend fun updateProductStock(cartItems: List<CartItem>) {
        val batch = firestore.batch()

        cartItems.forEach { cartItem ->
            val productRef = firestore.collection("products").document(cartItem.productId)
            val product = productRef.get().await().toObject(Product::class.java)

            product?.let {
                val newStock = it.availableStock - cartItem.quantity
                val updatedProduct = it.copy(
                    availableStock = newStock,
                    status = if (newStock <= 0) ProductStatus.OUT_OF_STOCK else ProductStatus.AVAILABLE,
                    updatedAt = System.currentTimeMillis()
                )
                batch.set(productRef, updatedProduct)
            }
        }

        batch.commit().await()
    }

    fun updatePaymentStatus(orderId: String, paymentId: String, razorpayOrderId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("orders")
                    .document(orderId)
                    .update(
                        mapOf(
                            "paymentStatus" to PaymentStatus.PAID,
                            "razorpayPaymentId" to paymentId,
                            "razorpayOrderId" to razorpayOrderId
                        )
                    )
                    .await()

                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun resetOrderPlaced() {
        _uiState.value = _uiState.value.copy(orderPlaced = false)
    }
}