package com.apmw.freshbrand.view.admin

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apmw.freshbrand.model.Order
import com.apmw.freshbrand.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsDialog(
    order: Order,
    user: com.apmw.freshbrand.model.User?,
    isUpdating: Boolean,
    onUpdateStatus: (OrderStatus) -> Unit,
    onUpdateDeliveryDate: (Long) -> Unit,
    onDeleteOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInvoice by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order #${order.id}")
                if (order.status == OrderStatus.CONFIRMED) {
                    TextButton(onClick = { showInvoice = true }) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Invoice")
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { OrderSummarySection(order, dateFormatter) }
                item { CustomerInfoSection(user, order) }
                item { OrderItemsSection(order) }
                item {
                    ActionSection(
                        order,
                        showStatusDropdown,
                        isUpdating,
                        onUpdateStatus = {
                            onUpdateStatus(it)
                            showStatusDropdown = false
                            showToast(context, "Order status updated to ${it.name}")
                        },
                        onShowStatusMenu = { showStatusDropdown = it },
                        onPickDate = { showDatePicker = true },
                        onDeleteOrder = {
                            showDeleteDialog = true
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeleteOrder()
                showDeleteDialog = false
                showToast(context, "Order deleted successfully")
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Date Picker
    if (showDatePicker) {
        ShowDatePickerDialog(
            context = context,
            onDateSelected = {
                onUpdateDeliveryDate(it)
                showDatePicker = false
                showToast(context, "Delivery date updated")
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Show Invoice Dialog
    if (showInvoice) {
        InvoiceDialog(
            order = order,
            user = user,
            onDismiss = { showInvoice = false }
        )
    }
}


@Composable
fun OrderSummarySection(order: Order, dateFormatter: SimpleDateFormat) {
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.CONFIRMED -> Color(0xFF2196F3)
        OrderStatus.SHIPPED -> Color(0xFF9C27B0)
        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> Color(0xFFF44336)
        else -> Color.Magenta
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(" Order Status: ", fontWeight = FontWeight.SemiBold)
                Text(order.status.name, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(" Payment Status: ", fontWeight = FontWeight.SemiBold)
                Text(order.paymentStatus.name, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(" Payment Method: ", fontWeight = FontWeight.SemiBold)
                Text(order.paymentMethod.name, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("Order Date: ${dateFormatter.format(Date(order.orderDate))}")
            if (order.deliveryDate > 0) {
                Text("Delivery Date: ${dateFormatter.format(Date(order.deliveryDate))}")
            }
            else{
                Text("Delivery Date: Not Set")
            }
        }
    }
}

@Composable
fun CustomerInfoSection(user: com.apmw.freshbrand.model.User?, order: Order) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Customer Info", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Name: ${user?.name ?: order.userName}")
            Text("Email: ${user?.email ?: order.userEmail}")
            Text("Phone: ${user?.phone ?: order.userPhone}")
            user?.address?.takeIf { it.isNotEmpty() }?.let { Text("Address: $it") }
            user?.companyName?.takeIf { it.isNotEmpty() }?.let { Text("Company: $it") }
            user?.gstNumber?.takeIf { it.isNotEmpty() }?.let { Text("GST: $it") }
        }
    }
}

//@Composable
//fun CustomDesignSection(design: com.apmw.freshbrand.model.CustomDesigns?) {
//    design ?: return
//    Card(Modifier.fillMaxWidth()) {
//        Column(Modifier.padding(16.dp)) {
//            Text("Custom Design", fontWeight = FontWeight.Bold)
//            Text("Design Name: ${design.designName}")
//            Text("Title: ${design.title}")
//            Text("Color: ${design.colorHex}")
//            if (design.logoUrl.isNotEmpty()) {
//                Text("Logo: Available")
//            }
//        }
//    }
//}

@Composable
fun OrderItemsSection(order: Order) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Items", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.productName, fontWeight = FontWeight.Medium)
                        Text("Qty: ${item.quantity}g × ₹${item.pricePerUnit}", fontSize = 12.sp)
                    }
                    Text("₹${item.totalPrice}", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Divider()
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Subtotal"); Text("₹${order.subtotal}")
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Tax"); Text("₹${order.tax}")
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ActionSection(
    order: Order,
    showStatusDropdown: Boolean,
    isUpdating: Boolean,
    onUpdateStatus: (OrderStatus) -> Unit,
    onShowStatusMenu: (Boolean) -> Unit,
    onPickDate: () -> Unit,
    onDeleteOrder: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onShowStatusMenu(true) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Update, null)
                Spacer(Modifier.width(6.dp))
                Text("Update Status")
            }

            OutlinedButton(
                onClick = onPickDate,
                modifier = Modifier.weight(1f),
                enabled = !isUpdating
            ) {
                Icon(Icons.Default.DateRange, null)
                Spacer(Modifier.width(6.dp))
                Text("Set Delivery Date")
            }
        }

        DropdownMenu(
            expanded = showStatusDropdown,
            onDismissRequest = { onShowStatusMenu(false) }
        ) {
            OrderStatus.values().forEach { status ->
                val color = when (status) {
                    OrderStatus.CONFIRMED -> Color(0xFF2E7D32)
                    OrderStatus.PENDING -> Color(0xFFFFA000)
                    OrderStatus.CANCELLED -> Color(0xFFD32F2F)
                    else -> Color.Gray
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = status.name,
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = { onUpdateStatus(status) }
                )
            }
        }

        Button(
            onClick = onDeleteOrder,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(4.dp))
            Text("Delete Order")
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Order?") },
        text = { Text("Are you sure? This cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ShowDatePickerDialog(
    context: Context,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            setOnDismissListener { onDismiss() }
        }
    }

    LaunchedEffect(Unit) {
        datePicker.show()
    }
}