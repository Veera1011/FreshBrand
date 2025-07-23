package com.apmw.freshbrand.view.admin


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.model.UserStatus
import com.apmw.freshbrand.model.UserType

import com.apmw.freshbrand.viewmodel.UsersViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: UsersViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditUserDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

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

    // Handle success messages for update
    LaunchedEffect(uiState.isUpdatingUser) {
        if (!uiState.isUpdatingUser && uiState.error == null) {
            if (showEditUserDialog) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "User updated successfully!",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                }
                showEditUserDialog = false
                selectedUser = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
                .padding(16.dp)
        ) {
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    viewModel.loadUsers()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Users refreshed",
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
                    uiState.users.isEmpty() -> {
                        EmptyUsersState()
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.users) { user ->
                                UserCard(
                                    user = user,
                                    onEditClick = {
                                        selectedUser = user
                                        showEditUserDialog = true
                                    },
                                    onDeleteClick = {
                                        selectedUser = user
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit User Dialog
    if (showEditUserDialog && selectedUser != null) {
        AddEditUserDialog(
            user = selectedUser,
            onDismiss = {
                showEditUserDialog = false
                selectedUser = null
            },
            onSave = { updatedUser ->
                selectedUser?.let { user ->
                    viewModel.updateUser(user.id, updatedUser)
                }
            },
            isLoading = uiState.isUpdatingUser
        )
    }

    // Delete User Dialog
    if (showDeleteDialog && selectedUser != null) {
        DeleteUserConfirmationDialog(
            userName = selectedUser?.name ?: "",
            onDismiss = {
                showDeleteDialog = false
                selectedUser = null
            },
            onConfirm = {
                selectedUser?.let { user ->
                    viewModel.deleteUser(user.id)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "User '${user.name}' deleted successfully",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showDeleteDialog = false
                selectedUser = null
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (user.companyName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Status and Type Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when (user.userType) {
                            UserType.ADMIN -> MaterialTheme.colorScheme.primaryContainer
                            UserType.CLIENT -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = user.userType.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (user.userType) {
                                UserType.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                                UserType.CLIENT -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }

                    Surface(
                        color = when (user.status) {
                            UserStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                            UserStatus.INACTIVE -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = user.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (user.status) {
                                UserStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
                                UserStatus.INACTIVE -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }
            }

            // GST Number if available
            if (user.gstNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GST: ${user.gstNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Address if available
            if (user.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Address: ${user.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyUsersState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = "No users",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Users Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Users will appear here once they register",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeleteUserConfirmationDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete User") },
        text = { Text("Are you sure you want to delete '$userName'? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUserDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var companyName by remember { mutableStateOf(user?.companyName ?: "") }
    var gstNumber by remember { mutableStateOf(user?.gstNumber ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var userType by remember { mutableStateOf(user?.userType ?: UserType.CLIENT) }
    var userStatus by remember { mutableStateOf(user?.status ?: UserStatus.ACTIVE) }

    var showUserTypeDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    // Validation states
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var companyNameError by remember { mutableStateOf(false) }

    fun validateForm(): Boolean {
        nameError = name.isBlank()
        emailError = email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        phoneError = phone.isBlank() || phone.length < 10
        companyNameError = companyName.isBlank()

        return !nameError && !emailError && !phoneError && !companyNameError
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Edit User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable form content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Name Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = false
                            },
                            label = { Text("Full Name *") },
                            isError = nameError,
                            supportingText = if (nameError) {
                                { Text("Name is required") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true
                        )
                    }

                    item {
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = false
                            },
                            label = { Text("Email Address *") },
                            isError = emailError,
                            supportingText = if (emailError) {
                                { Text("Valid email is required") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    item {
                        // Phone Field
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = false
                            },
                            label = { Text("Phone Number *") },
                            isError = phoneError,
                            supportingText = if (phoneError) {
                                { Text("Valid phone number is required") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    item {
                        // Company Name Field
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = {
                                companyName = it
                                companyNameError = false
                            },
                            label = { Text("Company Name *") },
                            isError = companyNameError,
                            supportingText = if (companyNameError) {
                                { Text("Company name is required") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Business, contentDescription = null)
                            },
                            singleLine = true
                        )
                    }

                    item {
                        // GST Number Field
                        OutlinedTextField(
                            value = gstNumber,
                            onValueChange = { gstNumber = it },
                            label = { Text("GST Number") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Receipt, contentDescription = null)
                            },
                            singleLine = true,
                            placeholder = { Text("Optional") }
                        )
                    }

                    item {
                        // Address Field
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            minLines = 2,
                            maxLines = 3,
                            placeholder = { Text("Optional") }
                        )
                    }

                    item {
                        // User Type Dropdown - FIXED VERSION
                        ExposedDropdownMenuBox(
                            expanded = showUserTypeDropdown,
                            onExpandedChange = { showUserTypeDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = userType.name,
                                onValueChange = { },
                                label = { Text("User Type *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUserTypeDropdown)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = showUserTypeDropdown,
                                onDismissRequest = { showUserTypeDropdown = false }
                            ) {
                                UserType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            userType = type
                                            showUserTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Status Dropdown - FIXED VERSION
                        ExposedDropdownMenuBox(
                            expanded = showStatusDropdown,
                            onExpandedChange = { showStatusDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = userStatus.name,
                                onValueChange = { },
                                label = { Text("Status *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        if (userStatus == UserStatus.ACTIVE) Icons.Default.CheckCircle
                                        else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (userStatus == UserStatus.ACTIVE)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = showStatusDropdown,
                                onDismissRequest = { showStatusDropdown = false }
                            ) {
                                UserStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (status == UserStatus.ACTIVE) Icons.Default.CheckCircle
                                                    else Icons.Default.Block,
                                                    contentDescription = null,
                                                    tint = if (status == UserStatus.ACTIVE)
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(status.name)
                                            }
                                        },
                                        onClick = {
                                            userStatus = status
                                            showStatusDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
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
                            if (validateForm()) {
                                val updatedUser = User(
                                    id = user?.id ?: "",
                                    name = name.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    userType = userType,
                                    gstNumber = gstNumber.trim(),
                                    companyName = companyName.trim(),
                                    address = address.trim(),
                                    status = userStatus,
                                    createdDate = user?.createdDate ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updatedUser)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update User")
                        }
                    }
                }
            }
        }
    }
}