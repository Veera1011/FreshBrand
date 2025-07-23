package com.apmw.freshbrand.view.admin

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.apmw.freshbrand.model.CartItem
import com.apmw.freshbrand.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDialog(
    order: Order,
    user: com.apmw.freshbrand.model.User?,
  //  customDesign: com.apmw.freshbrand.model.CustomDesigns?,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 8.dp else 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Professional Header with Actions - More responsive
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    if (isLandscape) {
                        // Landscape layout - horizontal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Invoice Preview",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionButtons(
                                    isGeneratingPdf = isGeneratingPdf,
                                    onDownloadClick = {
                                        if (!isGeneratingPdf) {
                                            scope.launch {
                                                isGeneratingPdf = true
                                                try {
                                                   // generateInvoicePDF(context, order, user, customDesign)
                                                    generateInvoicePDF(context, order, user)
                                                } finally {
                                                    isGeneratingPdf = false
                                                }
                                            }
                                        }
                                    },
                                    onCloseClick = onDismiss,
                                    isCompact = true
                                )
                            }
                        }
                    } else {
                        // Portrait layout - vertical for small screens
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Invoice Preview",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Close button always visible
                                IconButton(
                                    onClick = onDismiss,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Download button below on small screens
                            ActionButtons(
                                isGeneratingPdf = isGeneratingPdf,
                                onDownloadClick = {
                                    if (!isGeneratingPdf) {
                                        scope.launch {
                                            isGeneratingPdf = true
                                            try {
                                                generateInvoicePDF(context, order, user)
                                               // generateInvoicePDF(context, order, user, customDesign)
                                            } finally {
                                                isGeneratingPdf = false
                                            }
                                        }
                                    }
                                },
                                onCloseClick = null, // Already have close button above
                                isCompact = false
                            )
                        }
                    }
                }

                // Invoice Content with Better Scrolling - Use regular Column with scroll for better performance
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFAFAFA))
                        .verticalScroll(rememberScrollState())
                        .padding(if (isLandscape) 16.dp else 24.dp)
                ) {
                    InvoiceContent(
                        order = order,
                        user = user,
                        //customDesign = customDesign,
                        isCompact = isLandscape
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isGeneratingPdf: Boolean,
    onDownloadClick: () -> Unit,
    onCloseClick: (() -> Unit)?,
    isCompact: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
    ) {
        FilledTonalButton(
            onClick = onDownloadClick,
            enabled = !isGeneratingPdf,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = if (!isCompact) Modifier.fillMaxWidth() else Modifier
        ) {
            if (isGeneratingPdf) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCompact) "Generating..." else "Generating PDF...")
            } else {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download PDF")
            }
        }

        // Only show close button if provided
        onCloseClick?.let { closeClick ->
            IconButton(
                onClick = closeClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
fun InvoiceContent(
    order: Order,
    user: com.apmw.freshbrand.model.User?,
   // customDesign: com.apmw.freshbrand.model.CustomDesigns?,
    isCompact: Boolean = false
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 16.dp else 32.dp)
        ) {
            // Responsive Invoice Header
            if (screenWidth > 600) {
                // Wide screen layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    CompanyDetails(modifier = Modifier.weight(1f), isCompact = isCompact)
                    Spacer(modifier = Modifier.width(16.dp))
                    InvoiceDetails(
                        order = order,
                        dateFormatter = dateFormatter,
                        modifier = Modifier.weight(0.8f),
                        isCompact = isCompact
                    )
                }
            } else {
                // Narrow screen layout - stack vertically
                CompanyDetails(modifier = Modifier.fillMaxWidth(), isCompact = true)
                Spacer(modifier = Modifier.height(16.dp))
                InvoiceDetails(
                    order = order,
                    dateFormatter = dateFormatter,
                    modifier = Modifier.fillMaxWidth(),
                    isCompact = true
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 32.dp))

            // Responsive Bill To Section
            BillToSection(
                user = user,
                order = order,
                isCompact = isCompact
            )

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))

            // Custom Design Section
//            customDesign?.let { design ->
//                CustomDesignSection(design = design, isCompact = isCompact)
//                Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))
//            } ?: order.customDesign?.let { design ->
//                CustomDesignSection(design = design, isCompact = isCompact)
//                Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))
//            }

            // Responsive Items Table
            ItemsTable(
                items = order.items,
                isCompact = isCompact,
                screenWidth = screenWidth
            )

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))

            // Responsive Totals Section
            TotalsSection(
                order = order,
                isCompact = isCompact,
                screenWidth = screenWidth
            )

            Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 40.dp))

            // Footer
            InvoiceFooter(isCompact = isCompact)
        }
    }
}

@Composable
private fun CompanyDetails(modifier: Modifier = Modifier, isCompact: Boolean) {
    Column(modifier = modifier) {
        Text(
            text = "FRESH BRAND",
            fontSize = if (isCompact) 20.sp else 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2E7D32),
            letterSpacing = 1.sp
        )
        Text(
            text = "Premium Food Manufacturer & Supplier",
            fontSize = if (isCompact) 12.sp else 14.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

        Surface(
            color = Color(0xFFF1F8E9),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp)
            ) {
                Text("123 Business Street", fontSize = if (isCompact) 10.sp else 12.sp, color = Color(0xFF424242))
                Text("Chennai, Tamil Nadu 600001", fontSize = if (isCompact) 10.sp else 12.sp, color = Color(0xFF424242))
                Text("GSTIN: 33AABCU9603R1ZX", fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
private fun InvoiceDetails(
    order: Order,
    dateFormatter: SimpleDateFormat,
    modifier: Modifier = Modifier,
    isCompact: Boolean
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Surface(
            color = Color(0xFF2E7D32),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "INVOICE",
                fontSize = if (isCompact) 16.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(
                    horizontal = if (isCompact) 12.dp else 16.dp,
                    vertical = if (isCompact) 6.dp else 8.dp
                )
            )
        }

        Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Invoice #: INV-${order.id.take(8)}",
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242)
                )
                Text(
                    text = " Order Date: ${dateFormatter.format(Date(order.orderDate))}",
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    color = Color(0xFF666666)
                )
                Text(text = order.paymentStatus.name, fontSize = if (isCompact) 14.sp else 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text(text = order.paymentMethod.name, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                Text(text = order.orderDate.formatDate(), fontSize = if (isCompact) 14.sp else 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text(text = order.deliveryDate.formatDate(), fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))

            }
        }
    }
}

@Composable
private fun BillToSection(
    user: com.apmw.freshbrand.model.User?,
    order: Order,
    isCompact: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 12.dp else 20.dp)
        ) {
            Text(
                text = "BILL TO",
                fontSize = if (isCompact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                letterSpacing = 1.sp
            )

            Divider(
                modifier = Modifier
                    .width(40.dp)
                    .padding(vertical = if (isCompact) 4.dp else 8.dp),
                color = Color(0xFF2E7D32),
                thickness = 2.dp
            )

            user?.let { u ->
                Text(text = u.name, fontSize = if (isCompact) 14.sp else 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                if (u.companyName.isNotEmpty()) {
                    Text(text = u.companyName, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                }
                Text(text = u.email, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                Text(text = u.phone, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                if (u.address.isNotEmpty()) {
                    Text(text = u.address, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                }
                if (u.gstNumber.isNotEmpty()) {
                    Text(text = "GST: ${u.gstNumber}", fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                }
            } ?: run {
                Text(text = order.userName, fontSize = if (isCompact) 14.sp else 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text(text = order.userEmail, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                Text(text = order.userPhone, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                if (order.userAddress.isNotEmpty()) {
                    Text(text = order.userAddress, fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
private fun CustomDesignSection(
    design: com.apmw.freshbrand.model.CustomDesigns,
    isCompact: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E8)
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF4CAF50))
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp)
        ) {
            Text(
                text = "CUSTOM DESIGN SPECIFICATIONS",
                fontSize = if (isCompact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

            Column {
                Text("Design Name: ${design.designName}", fontSize = if (isCompact) 11.sp else 13.sp, fontWeight = FontWeight.Medium)
                Text("Title: ${design.title}", fontSize = if (isCompact) 11.sp else 13.sp, color = Color(0xFF666666))
                Text("Color: ${design.colorHex}", fontSize = if (isCompact) 11.sp else 13.sp, color = Color(0xFF666666))
                if (design.logoUrl.isNotEmpty()) {
                    Text("Custom Logo: ✓ Included", fontSize = if (isCompact) 11.sp else 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun ItemsTable(
    items: List<CartItem>, // Define your data class
    isCompact: Boolean,
    screenWidth: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(0.dp)
        ) {
            // Table Header
            Surface(
                color = Color(0xFF2E7D32),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                if (screenWidth > 600) {
                    // Wide screen - show all columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isCompact) 12.dp else 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PRODUCT",
                            fontSize = if (isCompact) 10.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "QUANTITY(gms)",
                            fontSize = if (isCompact) 10.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "RATE(gms)",
                            fontSize = if (isCompact) 10.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "AMOUNT",
                            fontSize = if (isCompact) 10.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Narrow screen - compact layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PRODUCT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "QTY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "AMOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Table Rows
            items.forEachIndexed { index, item ->
                val backgroundColor = if (index % 2 == 0) Color.White else Color(0xFFF5F5F5)

                Surface(
                    color = backgroundColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (screenWidth > 600) {
                        // Wide screen - show all columns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = if (isCompact) 12.dp else 16.dp,
                                    vertical = if (isCompact) 8.dp else 12.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.productName,
                                fontSize = if (isCompact) 11.sp else 14.sp,
                                color = Color(0xFF333333),
                                modifier = Modifier.weight(2f),
                                maxLines = 2,
                               // overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.quantity.toString(),
                                fontSize = if (isCompact) 11.sp else 14.sp,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "₹${String.format("%.2f", item.pricePerUnit)}",
                                fontSize = if (isCompact) 11.sp else 14.sp,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "₹${String.format("%.2f", item.totalPrice)}",
                                fontSize = if (isCompact) 11.sp else 14.sp,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Narrow screen - compact layout
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.productName,
                                    fontSize = 11.sp,
                                    color = Color(0xFF333333),
                                    modifier = Modifier.weight(2f),
                                    maxLines = 1,
                                 //   overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.quantity.toString(),
                                    fontSize = 11.sp,
                                    color = Color(0xFF333333),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹${String.format("%.2f", item.totalPrice)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF333333),
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Show rate on second line for narrow screens
                            if (item.pricePerUnit != item.totalPrice) {
                                Text(
                                    text = "Rate: ₹${String.format("%.2f", item.pricePerUnit)}",
                                    fontSize = 9.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Add divider between rows (except last row)
                if (index < items.size - 1) {
                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}


@Composable
private fun TotalsSection(
    order: Order,
    isCompact: Boolean,
    screenWidth: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (screenWidth > 600) Arrangement.End else Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = if (screenWidth > 600) Modifier.width(300.dp) else Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(if (isCompact) 12.dp else 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                    Text("₹${String.format("%.2f", order.subtotal)}", fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tax (GST 18%):", fontSize = if (isCompact) 12.sp else 14.sp, color = Color(0xFF666666))
                    Text("₹${String.format("%.2f", order.tax)}", fontSize = if (isCompact) 12.sp else 14.sp, fontWeight = FontWeight.Medium)
                }

                Divider(
                    modifier = Modifier.padding(vertical = if (isCompact) 8.dp else 12.dp),
                    color = Color(0xFFE0E0E0)
                )

                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isCompact) 8.dp else 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TOTAL AMOUNT:",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompact) 14.sp else 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "₹${String.format("%.2f", order.totalAmount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompact) 16.sp else 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceFooter(isCompact: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8E9)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 12.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Thank you for choosing Fresh Brand!",
                fontSize = if (isCompact) 14.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

            Text(
                text = "For support and queries:",
                fontSize = if (isCompact) 10.sp else 12.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            Text(
                text = "📧 support@freshbrand.com | 📞 +91 9876543210",
                fontSize = if (isCompact) 10.sp else 12.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}



// Enhanced PDF Generation Function with proper file handling
suspend fun generateInvoicePDF(
    context: Context,
    order: Order,
    user: com.apmw.freshbrand.model.User?,
   // customDesign: com.apmw.freshbrand.model.CustomDesigns?
) = withContext(Dispatchers.IO) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        // Set up paints for different text styles
        val titlePaint = Paint().apply {
            textSize = 24f
            color = android.graphics.Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
        }

        val headerPaint = Paint().apply {
            textSize = 14f
            color = android.graphics.Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
        }

        val normalPaint = Paint().apply {
            textSize = 12f
            color = android.graphics.Color.DKGRAY
        }

        val smallPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
        }

        var yPosition = 60f
        val leftMargin = 40f
        val rightMargin = 555f
        val paint = Paint()

        // Company Header
        canvas.drawText("FRESH BRAND", leftMargin, yPosition, titlePaint)
        yPosition += 20f
        canvas.drawText("Premium Food Manufacturer & Supplier", leftMargin, yPosition, normalPaint)
        yPosition += 40f

        // Invoice details on right
        canvas.drawText("INVOICE", 400f, 60f, titlePaint)
        canvas.drawText("INV-${order.id.take(8)}", 400f, 80f, normalPaint)
        canvas.drawText(dateFormatter.format(Date(order.orderDate)), 400f, 95f, smallPaint)

        // Company address
        canvas.drawText("123 Business Street", leftMargin, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("Chennai, Tamil Nadu 600001", leftMargin, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("GSTIN: 33AABCU9603R1ZX", leftMargin, yPosition, smallPaint)
        yPosition += 30f

        // Bill To section
        canvas.drawText("BILL TO:", leftMargin, yPosition, headerPaint)
        yPosition += 20f

        user?.let { u ->
            canvas.drawText(u.name, leftMargin, yPosition, normalPaint)
            yPosition += 15f
            if (u.companyName.isNotEmpty()) {
                canvas.drawText(u.companyName, leftMargin, yPosition, smallPaint)
                yPosition += 15f
            }
            canvas.drawText(u.email, leftMargin, yPosition, smallPaint)
            yPosition += 15f
            canvas.drawText(u.phone, leftMargin, yPosition, smallPaint)
            yPosition += 15f
            if (u.address.isNotEmpty()) {
                canvas.drawText(u.address, leftMargin, yPosition, smallPaint)
                yPosition += 15f
            }
            if (u.gstNumber.isNotEmpty()) {
                canvas.drawText("GST: ${u.gstNumber}", leftMargin, yPosition, smallPaint)
                yPosition += 15f
            }
        } ?: run {
            canvas.drawText(order.userName, leftMargin, yPosition, normalPaint)
            yPosition += 15f
            canvas.drawText(order.userEmail, leftMargin, yPosition, smallPaint)
            yPosition += 15f
            canvas.drawText(order.userPhone, leftMargin, yPosition, smallPaint)
            yPosition += 15f
            if (order.userAddress.isNotEmpty()) {
                canvas.drawText(order.userAddress, leftMargin, yPosition, smallPaint)
                yPosition += 15f
            }
        }

        yPosition += 20f

//        // Custom Design section (if available)
//        customDesign?.let { design ->
//            canvas.drawText("CUSTOM DESIGN SPECIFICATIONS:", leftMargin, yPosition, headerPaint)
//            yPosition += 20f
//            canvas.drawText("Design Name: ${design.designName}", leftMargin, yPosition, normalPaint)
//            yPosition += 15f
//            canvas.drawText("Title: ${design.title}", leftMargin, yPosition, smallPaint)
//            yPosition += 15f
//            canvas.drawText("Color: ${design.colorHex}", leftMargin, yPosition, smallPaint)
//            yPosition += 15f
//            if (design.logoUrl.isNotEmpty()) {
//                canvas.drawText("Custom Logo: ✓ Included", leftMargin, yPosition, smallPaint)
//                yPosition += 15f
//            }
//            yPosition += 20f
//        } ?: order.customDesign?.let { design ->
//            canvas.drawText("CUSTOM DESIGN SPECIFICATIONS:", leftMargin, yPosition, headerPaint)
//            yPosition += 20f
//            canvas.drawText("Design Name: ${design.designName}", leftMargin, yPosition, normalPaint)
//            yPosition += 15f
//            canvas.drawText("Title: ${design.title}", leftMargin, yPosition, smallPaint)
//            yPosition += 15f
//            canvas.drawText("Color: ${design.colorHex}", leftMargin, yPosition, smallPaint)
//            yPosition += 15f
//            if (design.logoUrl.isNotEmpty()) {
//                canvas.drawText("Custom Logo: ✓ Included", leftMargin, yPosition, smallPaint)
//                yPosition += 15f
//            }
//            yPosition += 20f
//        }

        // Items table header
        canvas.drawText("PRODUCT", leftMargin, yPosition, headerPaint)
        canvas.drawText("QUANTITY", 250f, yPosition, headerPaint)
        canvas.drawText("RATE", 350f, yPosition, headerPaint)
        canvas.drawText("AMOUNT", 450f, yPosition, headerPaint)
        yPosition += 20f

        // Draw line under header
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
        yPosition += 10f

        // Items (you'll need to iterate through order.items based on your data structure)
        // This is a placeholder - replace with actual item iteration
        order.items.forEach { item ->
            canvas.drawText(item.productName, leftMargin, yPosition, normalPaint)
            canvas.drawText(item.quantity.toString(), 250f, yPosition, normalPaint)
            canvas.drawText("₹${String.format("%.2f", item.pricePerUnit)}", 350f, yPosition, normalPaint)
            canvas.drawText("₹${String.format("%.2f", item.totalPrice)}", 450f, yPosition, normalPaint)
            yPosition += 20f
        }

        yPosition += 20f

        // Totals section
        canvas.drawText("Subtotal:", 350f, yPosition, normalPaint)
        canvas.drawText("₹${String.format("%.2f", order.subtotal)}", 450f, yPosition, normalPaint)
        yPosition += 15f

        canvas.drawText("Tax (GST 18%):", 350f, yPosition, normalPaint)
        canvas.drawText("₹${String.format("%.2f", order.tax)}", 450f, yPosition, normalPaint)
        yPosition += 15f

        // Draw line above total
        canvas.drawLine(350f, yPosition, rightMargin, yPosition, paint)
        yPosition += 10f

        canvas.drawText("TOTAL AMOUNT:", 350f, yPosition, headerPaint)
        canvas.drawText("₹${String.format("%.2f", order.totalAmount)}", 450f, yPosition, headerPaint)
        yPosition += 40f

        // Footer
        canvas.drawText("Thank you for choosing Fresh Brand!", leftMargin, yPosition, headerPaint)
        yPosition += 20f
        canvas.drawText("For support and queries:", leftMargin, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("📧 support@freshbrand.com | 📞 +91 9876543210", leftMargin, yPosition, smallPaint)

        pdfDocument.finishPage(page)

        // Save the PDF to external storage
        val fileName = "Invoice_INV-${order.id.take(8)}_${System.currentTimeMillis()}.pdf"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 and above - use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { documentUri ->
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(documentUri)
                outputStream?.let { stream ->
                    pdfDocument.writeTo(stream)
                    stream.close()

                    // Open the PDF
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(documentUri, "application/pdf")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                }
            }
        } else {
            // For Android 9 and below - use external storage directly
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()

            // Open the PDF using FileProvider
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        }

        pdfDocument.close()

    } catch (e: Exception) {
        e.printStackTrace()
        // Handle error - you might want to show a toast or log the error
    }
}