package com.apmw.freshbrand.view.admin

// ProductDialogs.kt


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.apmw.freshbrand.model.Product
import com.apmw.freshbrand.model.ProductStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product, List<Uri>) -> Unit,
    isLoading: Boolean
) {
    val isEditing = product != null

    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var pricePerUnit by remember { mutableStateOf(product?.pricePerUnit?.toString() ?: "") }
    var minOrderQuantity by remember { mutableStateOf(product?.minimumOrderQuantity?.toString() ?: "1") }
    var maxOrderQuantity by remember { mutableStateOf(product?.maximumOrderQuantity?.toString() ?: "1000") }
    var availableStock by remember { mutableStateOf(product?.availableStock?.toString() ?: "0") }
    var status by remember { mutableStateOf(product?.status ?: ProductStatus.AVAILABLE) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImages by remember { mutableStateOf(product?.productImages ?: emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = uris
    }

    val isFormValid = name.isNotBlank() &&
            description.isNotBlank() &&
            pricePerUnit.isNotBlank()
//            &&
//            minOrderQuantity.isNotBlank() &&
//            maxOrderQuantity.isNotBlank() &&
//            availableStock.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Product" else "Add Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = name.isBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3,
                    maxLines = 5,
                    isError = description.isBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price
                OutlinedTextField(
                    value = pricePerUnit,
                    onValueChange = { pricePerUnit = it },
                    label = { Text("Price per Unit (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = pricePerUnit.isBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Order Quantity Range
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    OutlinedTextField(
//                        value = minOrderQuantity,
//                        onValueChange = { minOrderQuantity = it },
//                        label = { Text("Min Order") },
//                        modifier = Modifier.weight(1f),
//                        enabled = !isLoading,
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        isError = minOrderQuantity.isBlank()
//                    )
//
//                    OutlinedTextField(
//                        value = maxOrderQuantity,
//                        onValueChange = { maxOrderQuantity = it },
//                        label = { Text("Max Order") },
//                        modifier = Modifier.weight(1f),
//                        enabled = !isLoading,
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        isError = maxOrderQuantity.isBlank()
//                    )
//                }

                Spacer(modifier = Modifier.height(16.dp))

//                // Available Stock
//                OutlinedTextField(
//                    value = availableStock,
//                    onValueChange = { availableStock = it },
//                    label = { Text("Available Stock") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !isLoading,
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = availableStock.isBlank()
//                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status Dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = status.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ProductStatus.values().forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption.name.replace("_", " ")) },
                                onClick = {
                                    status = statusOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Section
                Text(
                    text = "Product Images",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Existing Images (for editing)
                if (isEditing && existingImages.isNotEmpty()) {
                    Text(
                        text = "Current Images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(existingImages) { imageUrl ->
                            Box(
                                modifier = Modifier.size(80.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Product image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )

                                // Delete button
                                IconButton(
                                    onClick = {
                                        existingImages = existingImages.filter { it != imageUrl }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // New Images Selection
                if (selectedImageUris.isNotEmpty()) {
                    Text(
                        text = "New Images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImageUris) { uri ->
                            Box(
                                modifier = Modifier.size(80.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )

                                // Delete button
                                IconButton(
                                    onClick = {
                                        selectedImageUris = selectedImageUris.filter { it != uri }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Add Images Button
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Images")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                val newProduct = Product(
                                    name = name,
                                    description = description,
                                    pricePerUnit = pricePerUnit.toDoubleOrNull() ?: 0.0,
                                    minimumOrderQuantity = minOrderQuantity.toIntOrNull() ?: 1,
                                    maximumOrderQuantity = maxOrderQuantity.toIntOrNull() ?: 1000,
                                    availableStock = availableStock.toIntOrNull() ?: 0,
                                    status = status,
                                    productImages = existingImages
                                )
                                onSave(newProduct, selectedImageUris)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && isFormValid
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isEditing) "Update" else "Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    productName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Product") },
        text = {
            Text("Are you sure you want to delete '$productName'? This action cannot be undone.")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    )
}

@Composable
fun StockUpdateDialog(
    currentStock: Int,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var newStock by remember { mutableStateOf(currentStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock") },
        text = {
            Column {
                Text("Current Stock: $currentStock")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newStock,
                    onValueChange = { newStock = it },
                    label = { Text("New Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    newStock.toIntOrNull()?.let { stock ->
                        onUpdate(stock)
                    }
                },
                enabled = newStock.toIntOrNull() != null
            ) {
                Text("Update")
            }
        }
    )
}