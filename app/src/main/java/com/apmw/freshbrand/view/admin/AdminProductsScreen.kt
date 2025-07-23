package com.apmw.freshbrand.view.admin

// AdminProductsScreen.kt
import com.apmw.freshbrand.model.Product
import com.apmw.freshbrand.model.UserUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

import com.apmw.freshbrand.model.ProductStatus
import com.apmw.freshbrand.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    viewModel: ProductViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStockUpdateDialog by remember { mutableStateOf(false) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle success and error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.clearError()
        }
    }

    // Handle success messages for different actions
    LaunchedEffect(uiState.isAddingProduct) {
        if (!uiState.isAddingProduct && uiState.error == null) {
            // Check if we just finished adding a product successfully
            if (showAddProductDialog) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Product added successfully!",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.isUpdatingProduct) {
        if (!uiState.isUpdatingProduct && uiState.error == null) {
            // Check if we just finished updating a product successfully
            if (showEditProductDialog) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Product updated successfully!",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = if (snackbarData.visuals.message.contains("error", ignoreCase = true) ||
                            snackbarData.visuals.message.contains("failed", ignoreCase = true)) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.inverseSurface
                        },
                        contentColor = if (snackbarData.visuals.message.contains("error", ignoreCase = true) ||
                            snackbarData.visuals.message.contains("failed", ignoreCase = true)) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.inverseOnSurface
                        },
                        actionColor = if (snackbarData.visuals.message.contains("error", ignoreCase = true) ||
                            snackbarData.visuals.message.contains("failed", ignoreCase = true)) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.inversePrimary
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(5.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { showAddProductDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
                IconButton(onClick = {
                    viewModel.loadProducts()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Products refreshed",
                            duration = SnackbarDuration.Short
                        )
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.products.isEmpty() -> {
                        EmptyProductsState(
                            onAddProduct = { showAddProductDialog = true }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.products) { product ->
                                ProductCard(
                                    product = product,
                                    onEditClick = {
                                        viewModel.selectProduct(product)
                                        showEditProductDialog = true
                                    },
                                    onDeleteClick = {
                                        viewModel.selectProduct(product)
                                        showDeleteDialog = true
                                    },
                                    onStockUpdateClick = {
                                        viewModel.selectProduct(product)
                                        showStockUpdateDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddProductDialog) {
        AddEditProductDialog(product = null, onDismiss = { showAddProductDialog = false }, onSave = { product, imageUris ->
                viewModel.addProduct(product, imageUris)
                showAddProductDialog = false
            }, isLoading = uiState.isAddingProduct)
    }

    if (showEditProductDialog) {
        AddEditProductDialog(
            product = uiState.selectedProduct,
            onDismiss = { showEditProductDialog = false },
            onSave = { product, imageUris ->
                uiState.selectedProduct?.let { selectedProduct ->
                    viewModel.updateProduct(selectedProduct.id, product, imageUris)
                }
                showEditProductDialog = false
            },
            isLoading = uiState.isUpdatingProduct
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            productName = uiState.selectedProduct?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                uiState.selectedProduct?.let { product ->
                    viewModel.deleteProduct(product.id)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Product '${product.name}' deleted successfully",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showDeleteDialog = false
            }
        )
    }

//    if (showStockUpdateDialog) {
//        StockUpdateDialog(
//            currentStock = uiState.selectedProduct?.availableStock ?: 0,
//            onDismiss = { showStockUpdateDialog = false },
//            onUpdate = { newStock ->
//                uiState.selectedProduct?.let { product ->
//                    viewModel.updateStock(product.id, newStock)
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar(
//                            message = "Stock updated to $newStock for '${product.name}'",
//                            actionLabel = "OK",
//                            duration = SnackbarDuration.Short
//                        )
//                    }
//                }
//                showStockUpdateDialog = false
//            }
//        )
//    }
}

@Composable
fun ProductCard(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStockUpdateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Product Image
                if (product.productImages.isNotEmpty()) {
                    AsyncImage(
                        model = product.productImages.first(),
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "No image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Product Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹${product.pricePerUnit}/grams",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Action Buttons
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Stock and Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    Text(
//                        text = "Stock: ${product.availableStock} grams",
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Medium
//                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        color = when (product.status) {
                            ProductStatus.AVAILABLE -> MaterialTheme.colorScheme.primaryContainer
                            ProductStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.errorContainer
                            ProductStatus.DISCONTINUED -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = product.status.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (product.status) {
                                ProductStatus.AVAILABLE -> MaterialTheme.colorScheme.onPrimaryContainer
                                ProductStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.onErrorContainer
                                ProductStatus.DISCONTINUED -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

//                Button(
//                    onClick = onStockUpdateClick,
//                    modifier = Modifier.height(32.dp),
//                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
//                ) {
//                    Text(
//                        text = "Update",
//                        style = MaterialTheme.typography.labelMedium
//                    )
//                }
            }

            // Order quantity range
            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Order Range: ${product.minimumOrderQuantity} - ${product.maximumOrderQuantity} units",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
        }
    }
}

@Composable
fun EmptyProductsState(
    onAddProduct: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Inventory,
            contentDescription = "No products",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Products Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first mouth freshener product to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddProduct,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Product")
        }
    }
}