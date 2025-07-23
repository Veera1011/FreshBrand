// CartViewModel.kt
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

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadCartItems()
    }

    fun loadCartItems() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val cartItems = firestore.collection("carts")
                    .document(userId)
                    .collection("items")
                    .get()
                    .await()
                    .toObjects(CartItem::class.java)

                val subtotal = cartItems.sumOf { it.totalPrice }
                val tax = subtotal * 0.18 // 18% GST
                val totalAmount = subtotal + tax

                _uiState.value = _uiState.value.copy(
                    cartItems = cartItems,
                    subtotal = subtotal,
                    tax = tax,
                    totalAmount = totalAmount,
                    itemCount = cartItems.sumOf { it.quantity },
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

    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            try {
                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    pricePerUnit = product.pricePerUnit,
                    quantity = quantity,
                    totalPrice = product.pricePerUnit * quantity,
                    productImage = product.productImages.firstOrNull() ?: ""
                )

                firestore.collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(product.id)
                    .set(cartItem)
                    .await()

                loadCartItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    removeFromCart(productId)
                    return@launch
                }

                val cartRef = firestore.collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(productId)

                val cartItem = cartRef.get().await().toObject(CartItem::class.java)
                cartItem?.let {
                    val updatedItem = it.copy(
                        quantity = newQuantity,
                        totalPrice = it.pricePerUnit * newQuantity
                    )
                    cartRef.set(updatedItem).await()
                }

                loadCartItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(productId)
                    .delete()
                    .await()

                loadCartItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                val cartItems = firestore.collection("carts")
                    .document(userId)
                    .collection("items")
                    .get()
                    .await()

                val batch = firestore.batch()
                cartItems.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()

                loadCartItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}