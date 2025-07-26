
package com.apmw.freshbrand.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userAddress: String = "",
    val items: List<CartItem> = emptyList(),
    val customDesign: CustomDesigns? = null,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.NOT_SET,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val razorpayPaymentId: String = "",
    val razorpayOrderId: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryTimeline: String = "",
    val deliveryDate: Long = 0L,
    val invoiceUrl: String = "",
    val notes: String = ""
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

enum class PaymentMethod {
    RAZORPAY,
    PAY_LATER,
    NOT_SET
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

// Cart UI State
data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    val itemCount: Int = 0
)

// Order UI State
data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPlacingOrder: Boolean = false,
    val orderPlaced: Boolean = false,
    val selectedOrder: Order? = null
)