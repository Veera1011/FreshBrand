package com.apmw.freshbrand.model




data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val pricePerUnit: Double = 0.0,
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val productImage: String = ""
)