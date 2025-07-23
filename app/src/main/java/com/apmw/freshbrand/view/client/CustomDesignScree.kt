package com.apmw.freshbrand.view.client

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apmw.freshbrand.model.CustomDesigns
import com.apmw.freshbrand.viewmodel.CustomDesignViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDesignScreen(viewModel: CustomDesignViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Create Design", "My Design")

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            if (index == 0) Icons.Default.Add else Icons.Default.DesignServices,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> CreateDesignTab(viewModel)
            1 -> MyDesignTab(viewModel)
        }
    }
}
@Composable
fun MyDesignTab(viewModel: CustomDesignViewModel) {
    val design by viewModel.design.collectAsState()
    val isLoading by viewModel.historyLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        viewModel.loadUserDesign()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "My Design",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
//                Text(
//                    if (design.id.isNotEmpty()) "Your custom design" else "No design created yet",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
                Text(
                    "If You want to create or update design go to Create Design Tab",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = { viewModel.loadUserDesign() }
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message Display
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { viewModel.clearErrorMessage() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Authentication Check
        if (currentUser == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Authentication Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Please log in to view your design",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading your design...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (design.id.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DesignServices,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No design yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Go to 'Create Design' tab to create your custom sachet design!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Display existing design
            MyDesignItem(
                design = design,
                //onEdit = { viewModel.prepareForEdit() },
                onDelete = { viewModel.deleteUserDesign() }
            )
        }
    }
}

@Composable
fun MyDesignItem(
    design: CustomDesigns,
   // onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: CustomDesignViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var show by remember{mutableStateOf(false)}
    if(show){
        CreateDesignTab(viewModel)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Design Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = design.designName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row {
//                    IconButton(onClick = {show=true}) {
//                        Icon(
//                            Icons.Default.Edit,
//                            contentDescription = "Edit",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Preview
            Realistic3DSachetPreview(
                title = design.title,
                colorHex = design.colorHex,
                logoUrl = design.logoUrl
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Design Details
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Title,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Title: ${design.title}")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(design.colorHex))
                                } catch (e: Exception) {
                                    Color.Gray
                                }
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Text("Color: ${design.colorHex}")
                }

                if (design.logoUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Logo: ${design.logoUrl}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    "Created: ${formatDate(design.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Design") },
            text = { Text("Are you sure you want to delete your design? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CreateDesignTab(viewModel: CustomDesignViewModel) {
    // Local states
    var designNameState by remember { mutableStateOf(TextFieldValue("")) }
    var sachetTitleState by remember { mutableStateOf(TextFieldValue("")) }
    var colorHexState by remember { mutableStateOf(TextFieldValue("#4CAF50")) }
    var logoUrlState by remember { mutableStateOf(TextFieldValue("")) }
    var showColorPicker by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val existingDesign by viewModel.design.collectAsState()
    val current = LocalContext.current

    // Load existing design data when it changes
    LaunchedEffect(existingDesign) {
        if (existingDesign.id.isNotEmpty()) {
            designNameState = TextFieldValue(existingDesign.designName)
            sachetTitleState = TextFieldValue(existingDesign.title)
            colorHexState = TextFieldValue(existingDesign.colorHex)
            logoUrlState = TextFieldValue(existingDesign.logoUrl)
        }
    }

    LaunchedEffect(saveStatus) {
        if (saveStatus is CustomDesignViewModel.SaveStatus.Success) {
            Toast.makeText(current, "Design saved successfully!", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        } else if (saveStatus is CustomDesignViewModel.SaveStatus.Error) {
            Toast.makeText(current, (saveStatus as CustomDesignViewModel.SaveStatus.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DriveFileRenameOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (existingDesign.id.isNotEmpty()) "Update Your Design" else "Create Custom Design",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (existingDesign.id.isNotEmpty()) "Modify your existing sachet design" else "Create your personalized mouth freshener sachet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            // Design Name Input
            OutlinedTextField(
                value = designNameState,
                onValueChange = { designNameState = it },
                label = { Text("Design Name") },
                placeholder = { Text("e.g., Premium Mint Design") },
                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            // Sachet Title Input
            OutlinedTextField(
                value = sachetTitleState,
                onValueChange = { sachetTitleState = it },
                label = { Text("Sachet Title") },
                placeholder = { Text("e.g., Fresh Mint") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            // Advanced Color Picker
            AdvancedColorPicker(
                selectedColor = colorHexState.text,
                onColorSelected = { colorHexState = TextFieldValue(it) },
                showPicker = showColorPicker,
                onShowPickerChange = { showColorPicker = it }
            )
        }

        item {
            // Logo URL Input
            OutlinedTextField(
                value = logoUrlState,
                onValueChange = { logoUrlState = it },
                label = { Text("Logo URL (Optional)") },
                placeholder = { Text("https://example.com/logo.png") },
                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
        }

        item {
            // Save/Update Button
            Button(
                onClick = {
                    viewModel.saveOrUpdateDesign(
                        designName = designNameState.text,
                        title = sachetTitleState.text,
                        colorHex = colorHexState.text,
                        logoUrl = logoUrlState.text
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && designNameState.text.isNotBlank() && sachetTitleState.text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        if (existingDesign.id.isNotEmpty()) Icons.Default.Update else Icons.Default.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (existingDesign.id.isNotEmpty()) "Update Design" else "Save Design",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            // 3D Sachet Preview
            Text(
                "3D Sachet Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Realistic3DSachetPreview(
                title = if (sachetTitleState.text.isNotBlank()) sachetTitleState.text else "Sachet Title",
                colorHex = colorHexState.text,
                logoUrl = logoUrlState.text
            )
        }
    }
}


@Composable
fun AdvancedColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    showPicker: Boolean,
    onShowPickerChange: (Boolean) -> Unit
) {
    Column {
        Text(
            "Brand Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Color preview and input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(selectedColor))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    )
                    .border(3.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onShowPickerChange(!showPicker) }
                    .shadow(4.dp, CircleShape)
            )

            OutlinedTextField(
                value = selectedColor,
                onValueChange = onColorSelected,
                label = { Text("Hex Color") },
                placeholder = { Text("#4CAF50") },
                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Comprehensive Color Picker
        if (showPicker) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Choose Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Popular Colors
                    Text("Popular Colors", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val popularColors = listOf(
                        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
                        "#DDA0DD", "#F39C12", "#E74C3C", "#9B59B6", "#3498DB",
                        "#2ECC71", "#F1C40F", "#E67E22", "#E91E63", "#607D8B"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(popularColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(color)))
                                    .border(
                                        if (selectedColor == color) 3.dp else 1.dp,
                                        if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                        CircleShape
                                    )
                                    .clickable {
                                        onColorSelected(color)
                                        onShowPickerChange(false)
                                    }
                                    .shadow(2.dp, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Material Colors
                    Text("Material Colors", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val materialColors = listOf(
                        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
                        "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
                        "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800",
                        "#FF5722", "#795548", "#9E9E9E", "#607D8B", "#000000"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(materialColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(color)))
                                    .border(
                                        if (selectedColor == color) 3.dp else 1.dp,
                                        if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                        CircleShape
                                    )
                                    .clickable {
                                        onColorSelected(color)
                                        onShowPickerChange(false)
                                    }
                                    .shadow(2.dp, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gradient Colors
                    Text("Gradient Colors", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val gradientColors = listOf(
                        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
                        "#DDA0DD", "#FFB6C1", "#87CEEB", "#98FB98", "#F0E68C"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(gradientColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(android.graphics.Color.parseColor(color)),
                                                Color(android.graphics.Color.parseColor(color)).copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                                    .border(
                                        if (selectedColor == color) 3.dp else 1.dp,
                                        if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                        CircleShape
                                    )
                                    .clickable {
                                        onColorSelected(color)
                                        onShowPickerChange(false)
                                    }
                                    .shadow(2.dp, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Realistic3DSachetPreview(
    title: String,
    colorHex: String,
    logoUrl: String
) {
    val baseColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4A90E2)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.White)
    ) {
        // Simple 3D sachet
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 160.dp, height = 220.dp)
                .graphicsLayer {
                    rotationY = 15f
                    rotationX = -5f
                }
        ) {
            // Main sachet body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(baseColor)
            ) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo
                        if (logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(logoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = "Brand",
                                    tint = baseColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "PREMIUM",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Middle section - Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "MOUTH FRESHENER",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Bottom section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NET WT",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "5g",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = baseColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Simple 3D side panel
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(12.dp)
                    .background(baseColor.copy(alpha = 0.7f))
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Simple description
    Text(
        text = "Simple 3D preview of your sachet design",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}





@Composable
fun formatDate(timestamp: Long): String {
    return if (timestamp > 0) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } else {
        "Unknown"
    }
}