package com.lifeplus.healthcare.ui.screens.features

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.data.model.HealthRecord
import com.lifeplus.healthcare.presentation.viewmodel.HealthRecordViewModel
import com.lifeplus.healthcare.ui.components.*
import com.lifeplus.healthcare.ui.theme.*
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun HealthRecordsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HealthRecordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val actionState by viewModel.action.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { 
        selectedImageUri = it 
    }
    val filteredRecords = remember(state.data, selectedFilter) {
        filterRecords(state.data, selectedFilter)
    }

    val context = LocalContext.current

    LaunchedEffect(actionState.isSuccess, actionState.error) {
        if (actionState.isSuccess) {
            android.widget.Toast.makeText(context, "Health record saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
        }
        if (!actionState.error.isNullOrBlank()) {
            android.widget.Toast.makeText(context, actionState.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Health Records",
                subtitle = "Your private medical vault",
                onBackClick = onNavigateBack
            )

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (!state.error.isNullOrBlank() && state.data.isEmpty()) {
                    ErrorStateCard(
                        message = state.error.orEmpty(),
                        onRetry = { viewModel.load() }
                    )
                } else if (state.data.isEmpty()) {
                    EmptyRecordsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
                                color = Primary,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.VerifiedUser, null, tint = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Encrypted Storage", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Your records are visible only to you", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        
                        item {
                            Text("Recent Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        }
                        item {
                            RecordTypeFilterRow(
                                selectedFilter = selectedFilter,
                                onFilterChange = { selectedFilter = it }
                            )
                        }

                        items(filteredRecords) { record ->
                            PremiumRecordCard(
                                record = record, 
                                onDelete = { viewModel.delete(record.id) },
                                onClick = { editingRecord = record }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }

                Surface(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(60.dp)
                        .shadow(12.dp, CircleShape, spotColor = Primary),
                    shape = CircleShape,
                    color = Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudUpload, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
            SnackbarHost(hostState = snackbarHostState)
        }
    }

    if (showAddDialog) {
        AddRecordDialog(
            onDismiss = { showAddDialog = false },
            onPickImage = { launcher.launch("image/*") },
            selectedImageUri = selectedImageUri,
            onConfirm = { title, type ->
                viewModel.create(title, type, selectedImageUri?.toString())
                selectedImageUri = null
                showAddDialog = false
            }
        )
    }

    if (editingRecord != null) {
        AddRecordDialog(
            initialRecord = editingRecord,
            onDismiss = { editingRecord = null },
            onPickImage = { launcher.launch("image/*") },
            selectedImageUri = selectedImageUri ?: editingRecord?.imageUrl?.let { android.net.Uri.parse(it) },
            onConfirm = { title, type ->
                editingRecord?.let { original ->
                    viewModel.update(original.id, title, type, selectedImageUri?.toString() ?: original.imageUrl)
                }
                selectedImageUri = null
                editingRecord = null
            }
        )
    }
}

@Composable
fun PremiumRecordCard(record: HealthRecord, onDelete: () -> Unit, onClick: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = Surface2Light
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (record.type == "Lab") Icons.Default.Science else Icons.Outlined.Description, 
                        null, tint = Primary, modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${record.type} • ${record.date}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                
                if (record.imageUrl != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = record.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete record", tint = ErrorColor.copy(alpha = 0.6f))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Record") },
            text = { Text("Are you sure you want to delete this record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RecordTypeFilterRow(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val options = listOf("All", "Lab", "Prescription", "Vaccination")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedFilter == option,
                onClick = { onFilterChange(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
private fun ErrorStateCard(
    message: String,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Failed to load records", color = ErrorColor, fontWeight = FontWeight.Bold)
                Text(message, color = TextSecondary, textAlign = TextAlign.Center)
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
fun EmptyRecordsState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Surface2Light) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(60.dp), tint = TextHint.copy(alpha = 0.4f))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("No Health Records", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Upload your prescriptions and lab reports to keep them organized.", textAlign = TextAlign.Center, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun AddRecordDialog(
    initialRecord: HealthRecord? = null,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    selectedImageUri: android.net.Uri?,
    onConfirm: (title: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf(initialRecord?.title ?: "") }
    val types = listOf("Lab", "Prescription", "Vaccination", "Other")
    var selectedType by remember { mutableStateOf(initialRecord?.type ?: types[0]) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = { Text(if (initialRecord == null) "New Health Record" else "Edit Health Record", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InputField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Report Title",
                    placeholder = "e.g. Blood Test - Jan 2024"
                )

                DropdownField(
                    label = "Record Type",
                    selected = selectedType,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    items = types,
                    onItemSelected = { selectedType = it }
                )

                Surface(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Surface2Light,
                    border = if (selectedImageUri != null) androidx.compose.foundation.BorderStroke(1.dp, Primary) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = Primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Upload Photo", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (initialRecord == null) "Save Record" else "Update Record",
                onClick = {
                    if (title.isNotBlank()) onConfirm(title.trim(), selectedType)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
