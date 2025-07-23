// Updated OrderProductsScreen.kt
package com.apmw.freshbrand.view.client

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.model.*
import com.apmw.freshbrand.viewmodel.ProductViewModel
import com.apmw.freshbrand.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderProductsScreen(
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    onNavigateToCart: () -> Unit = {}
) {
    val productsUiState by productViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Products") },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                if (cartUiState.itemCount > 0) {
                                    Badge {
                                        Text(cartUiState.itemCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            productsUiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            productsUiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = productsUiState.error ?: "Something went wrong")
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    items(productsUiState.products.filter { it.status == ProductStatus.AVAILABLE }) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = { quantity -> cartViewModel.addToCart(product, quantity) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(product.minimumOrderQuantity) }
    var showQuantityDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹${product.pricePerUnit}/gram",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
//                    Text(
//                        text = "Stock: ${product.availableStock}g",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = if (product.availableStock > 0) Color.Green else Color.Red
//                    )
                }

//                Column(horizontalAlignment = Alignment.End) {
//                    Text(
//                        text = "Min: ${product.minimumOrderQuantity}g",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    Text(
//                        text = "Max: ${product.maximumOrderQuantity}g",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showQuantityDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Cart")

                }

//                Button(
//                    onClick = {
//                        onAddToCart(quantity)
//                        // You can add buy now logic here
//                    },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Buy Now")
//                }
            }
        }
    }

    if (showQuantityDialog) {
        QuantityDialog(
            product = product,
            initialQuantity = quantity,
            onQuantitySelected = { selectedQuantity ->
                quantity = selectedQuantity
                onAddToCart(selectedQuantity)
                showQuantityDialog = false
            },
            onDismiss = { showQuantityDialog = false }
        )
    }
}

@Composable
fun QuantityDialog(
    product: Product,
    initialQuantity: Int,
    onQuantitySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(initialQuantity.toString()) }
    val quantityInt = quantity.toIntOrNull() ?: 0
    val current= LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Quantity") },
        text = {
            Column {
                Text("${product.name}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Price: ₹${product.pricePerUnit}/gram")
//                Text("Min: ${product.minimumOrderQuantity}g, Max: ${product.maximumOrderQuantity}g")
//                Text("Available: ${product.availableStock}g")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = quantityInt < product.minimumOrderQuantity ||
//                            quantityInt > product.maximumOrderQuantity ||
//                            quantityInt > product.availableStock
                )

                if (quantityInt > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total: ₹${(quantityInt * product.pricePerUnit).toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onQuantitySelected(quantityInt) },
//                enabled = quantityInt >= product.minimumOrderQuantity &&
//                        quantityInt <= product.maximumOrderQuantity &&
//                        quantityInt <= product.availableStock
            ) {
                Text("Add to Cart")

            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}