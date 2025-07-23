package com.apmw.freshbrand.model

// pro.kt
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val pricePerUnit: Double = 0.0,
    val minimumOrderQuantity: Int = 1,
    val maximumOrderQuantity: Int = 1000,
    val availableStock: Int = 0,
    val productImages: List<String> = emptyList(),
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DISCONTINUED
}

// For UI state management
data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingProduct: Boolean = false,
    val isUpdatingProduct: Boolean = false,
    val selectedProduct: Product? = null
)
