package com.apmw.freshbrand.viewmodel

// ProductViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import com.apmw.freshbrand.model.Product
import com.apmw.freshbrand.model.ProductsUiState
import com.apmw.freshbrand.model.repository.ProductRepository

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val products = repository.getAllProducts()
                _uiState.value = _uiState.value.copy(
                    products = products,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load products",
                    isLoading = false
                )
            }
        }
    }

    fun addProduct(product: Product, imageUris: List<Uri> = emptyList()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingProduct = true, error = null)
            try {
                // Upload images first
                val imageUrls = mutableListOf<String>()
                for (uri in imageUris) {
                    val imageUrl = repository.uploadProductImage(uri)
                    imageUrls.add(imageUrl)
                }

                // Create product with image URLs
                val productWithImages = product.copy(
                    productImages = imageUrls,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                repository.addProduct(productWithImages)
                loadProducts() // Refresh the list
                _uiState.value = _uiState.value.copy(isAddingProduct = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add product",
                    isAddingProduct = false
                )
            }
        }
    }

    fun updateProduct(productId: String, product: Product, newImageUris: List<Uri> = emptyList()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingProduct = true, error = null)
            try {
                // Upload new images
                val newImageUrls = mutableListOf<String>()
                for (uri in newImageUris) {
                    val imageUrl = repository.uploadProductImage(uri)
                    newImageUrls.add(imageUrl)
                }

                // Combine existing and new image URLs
                val allImageUrls = product.productImages + newImageUrls

                val updatedProduct = product.copy(
                    productImages = allImageUrls,
                    updatedAt = System.currentTimeMillis()
                )

                repository.updateProduct(productId, updatedProduct)
                loadProducts()
                _uiState.value = _uiState.value.copy(isUpdatingProduct = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update product",
                    isUpdatingProduct = false
                )
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                // Get product to delete images
                val product = repository.getProductById(productId)

                // Delete product images from storage
                product?.productImages?.forEach { imageUrl ->
                    repository.deleteProductImage(imageUrl)
                }

                repository.deleteProduct(productId)
                loadProducts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete product"
                )
            }
        }
    }

    fun updateStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            try {
                repository.updateStock(productId, newStock)
                loadProducts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update stock"
                )
            }
        }
    }

    fun selectProduct(product: Product?) {
        _uiState.value = _uiState.value.copy(selectedProduct = product)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun deleteProductImage(productId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                val currentProduct = repository.getProductById(productId)
                if (currentProduct != null) {
                    val updatedImages = currentProduct.productImages.filter { it != imageUrl }
                    val updatedProduct = currentProduct.copy(
                        productImages = updatedImages,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateProduct(productId, updatedProduct)
                    repository.deleteProductImage(imageUrl)
                    loadProducts()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete image"
                )
            }
        }
    }
}