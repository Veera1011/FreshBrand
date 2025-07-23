// AdminOrderViewModel.kt
package com.apmw.freshbrand.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apmw.freshbrand.model.Order
import com.apmw.freshbrand.model.OrderStatus
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.model.CustomDesigns
import com.apmw.freshbrand.model.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminOrderUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedOrder: Order? = null,
    val orderUsers: Map<String, User> = emptyMap(),
    val customDesigns: Map<String, CustomDesigns> = emptyMap(),
    val isUpdating: Boolean = false,
    val showInvoice: Boolean = false
)

class AdminOrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.getAllOrders().collect { orders ->
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        isLoading = false
                    )

                    // Load user details for each order
                    loadOrderUsers(orders)
                    loadCustomDesigns(orders)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load orders"
                )
            }
        }
    }

    private fun loadOrderUsers(orders: List<Order>) {
        viewModelScope.launch {
            val userMap = mutableMapOf<String, User>()
            orders.forEach { order ->
                if (order.userId.isNotEmpty() && !userMap.containsKey(order.userId)) {
                    repository.getUserById(order.userId)?.let { user ->
                        userMap[order.userId] = user
                    }
                }
            }
            _uiState.value = _uiState.value.copy(orderUsers = userMap)
        }
    }

    private fun loadCustomDesigns(orders: List<Order>) {
        viewModelScope.launch {
            val designMap = mutableMapOf<String, CustomDesigns>()
            orders.forEach { order ->
                order.customDesign?.let { design ->
                    if (design.id.isNotEmpty() && !designMap.containsKey(design.id)) {
                        repository.getCustomDesignById(design.id)?.let { customDesign ->
                            designMap[design.id] = customDesign
                        }
                    }
                }
            }
            _uiState.value = _uiState.value.copy(customDesigns = designMap)
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val success = repository.updateOrderStatus(orderId, status)
                if (success) {
                    loadOrders() // Refresh orders
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update order status",
                        isUpdating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update order status",
                    isUpdating = false
                )
            }
        }
    }

    fun updateDeliveryDate(orderId: String, deliveryDate: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val success = repository.updateDeliveryDate(orderId, deliveryDate)
                if (success) {
                    loadOrders() // Refresh orders
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update delivery date",
                        isUpdating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update delivery date",
                    isUpdating = false
                )
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val success = repository.deleteOrder(orderId)
                if (success) {
                    loadOrders() // Refresh orders
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete order",
                        isUpdating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete order",
                    isUpdating = false
                )
            }
        }
    }

    fun selectOrder(order: Order) {
        _uiState.value = _uiState.value.copy(selectedOrder = order)
    }

    fun clearSelectedOrder() {
        _uiState.value = _uiState.value.copy(selectedOrder = null)
    }

    fun showInvoice(show: Boolean) {
        _uiState.value = _uiState.value.copy(showInvoice = show)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}