package com.lifeplus.healthcare.ui.screens.features

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lifeplus.healthcare.data.model.MedicineReminder
import com.lifeplus.healthcare.presentation.viewmodel.ReminderViewModel
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.theme.ErrorColor
import com.lifeplus.healthcare.ui.theme.Primary
import com.lifeplus.healthcare.ui.theme.PrimaryLight
import com.lifeplus.healthcare.ui.theme.SuccessColor
import com.lifeplus.healthcare.ui.theme.Surface2Light
import com.lifeplus.healthcare.ui.theme.TextHint
import com.lifeplus.healthcare.ui.theme.TextPrimary
import com.lifeplus.healthcare.ui.theme.TextSecondary
import com.lifeplus.healthcare.util.NotificationScheduler
import java.util.Calendar
import java.util.Locale

@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val actionState by viewModel.action.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<MedicineReminder?>(null) }
    var pendingCreatedReminder by remember { mutableStateOf<MedicineReminder?>(null) }
    var pendingPermissionReminder by remember { mutableStateOf<MedicineReminder?>(null) }

    val requestNotificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingPermissionReminder?.let { NotificationScheduler.scheduleReminder(context, it) }
        }
        pendingPermissionReminder = null
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun scheduleWithPermission(reminder: MedicineReminder) {
        if (hasNotificationPermission()) {
            NotificationScheduler.scheduleReminder(context, reminder)
            return
        }
        pendingPermissionReminder = reminder
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    LaunchedEffect(actionState.isSuccess, actionState.error, state.data, pendingCreatedReminder) {
        if (actionState.isSuccess) {
            android.widget.Toast.makeText(context, "Success!", android.widget.Toast.LENGTH_SHORT).show()
            pendingCreatedReminder?.let { draft ->
                val resolved = state.data
                    .filter {
                        it.medicineName == draft.medicineName &&
                            it.dosage == draft.dosage &&
                            it.nextTime == draft.nextTime
                    }
                    .maxByOrNull { it.id }
                if (resolved != null && resolved.active) {
                    scheduleWithPermission(resolved)
                }
                pendingCreatedReminder = null
            }
        }
        if (!actionState.error.isNullOrBlank()) {
            android.widget.Toast.makeText(context, actionState.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Medicine Reminders",
                subtitle = "Manage your daily dosage",
                onBackClick = onNavigateBack
            )

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.data.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    !state.error.isNullOrBlank() && state.data.isEmpty() -> {
                        ReminderErrorState(
                            message = state.error.orEmpty(),
                            onRetry = { viewModel.load() }
                        )
                    }

                    state.data.isEmpty() -> {
                        EmptyRemindersState()
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            item {
                                Text(
                                    text = "Your Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            items(state.data) { reminder ->
                                PremiumReminderCard(
                                    reminder = reminder,
                                    onToggle = { isEnabled ->
                                        val updated = reminder.copy(active = isEnabled)
                                        viewModel.toggle(reminder)
                                        if (isEnabled) {
                                            scheduleWithPermission(updated)
                                        } else {
                                            NotificationScheduler.cancelReminder(context, reminder.id)
                                        }
                                    },
                                    onDelete = {
                                        NotificationScheduler.cancelReminder(context, reminder.id)
                                        viewModel.delete(reminder.id)
                                    },
                                    onClick = { editingReminder = reminder }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
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
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add reminder",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            SnackbarHost(hostState = snackbarHostState)
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, dosage, time, imageUri ->
                val draft = MedicineReminder(
                    id = 0,
                    medicineName = name,
                    dosage = dosage,
                    nextTime = time,
                    active = true,
                    imageUrl = imageUri?.toString()
                )
                pendingCreatedReminder = draft
                viewModel.create(draft)
                showAddDialog = false
            }
        )
    }

    if (editingReminder != null) {
        AddReminderDialog(
            initialReminder = editingReminder,
            onDismiss = { editingReminder = null },
            onConfirm = { name, dosage, time, imageUri ->
                editingReminder?.let { original ->
                    val updated = original.copy(
                        medicineName = name,
                        dosage = dosage,
                        nextTime = time,
                        imageUrl = imageUri?.toString() ?: original.imageUrl
                    )
                    viewModel.update(updated)
                }
                editingReminder = null
            }
        )
    }
}

@Composable
private fun ReminderErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Failed to load reminders", color = ErrorColor, fontWeight = FontWeight.Bold)
            Text(message, color = TextSecondary)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun EmptyRemindersState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            color = SuccessColor.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AlarmOn, null, modifier = Modifier.size(70.dp), tint = SuccessColor.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "No Reminders Set",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Keep track of your medicines effortlessly. Tap + to add your first reminder.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun PremiumReminderCard(
    reminder: MedicineReminder,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Medicine image or default icon
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Primary.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!reminder.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = reminder.imageUrl,
                                contentDescription = reminder.medicineName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Medication, null, tint = Primary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.medicineName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = TextHint, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${reminder.dosage} · ${reminder.nextTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = reminder.active,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Surface2Light
                    )
                )

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteOutline, null, tint = ErrorColor.copy(alpha = 0.7f))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
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
fun AddReminderDialog(
    initialReminder: MedicineReminder? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, dosage: String, time: String, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val now = remember { Calendar.getInstance() }

    var name by remember { mutableStateOf(initialReminder?.medicineName ?: "") }
    var dosage by remember { mutableStateOf(initialReminder?.dosage ?: "") }
    
    // Parse initial time if editing
    val initialTimeParts = initialReminder?.nextTime?.split(":")
    var hour by remember { mutableStateOf(initialTimeParts?.getOrNull(0)?.toIntOrNull() ?: now.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(initialTimeParts?.getOrNull(1)?.toIntOrNull() ?: now.get(Calendar.MINUTE)) }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(initialReminder?.imageUrl?.let { Uri.parse(it) }) }

    val timeText = remember(hour, minute) {
        String.format(Locale.ENGLISH, "%02d:%02d", hour, minute)
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, pickedHour, pickedMinute ->
                hour = pickedHour
                minute = pickedMinute
            },
            hour,
            minute,
            true
        )
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = {
            Text(if (initialReminder == null) "New Reminder" else "Edit Reminder", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Medicine Name",
                    placeholder = "e.g. Napa Extend"
                )
                InputField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = "Dosage",
                    placeholder = "e.g. 1 Tablet"
                )

                // Time picker row
                Surface(
                    onClick = { timePickerDialog.show() },
                    shape = RoundedCornerShape(14.dp),
                    color = Surface2Light,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary)
                        Text(
                            "Reminder Time: $timeText",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Medicine image picker
                Surface(
                    onClick = { imageLauncher.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedImageUri != null) Color.Transparent else Surface2Light,
                    border = if (selectedImageUri != null)
                        BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                    else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (selectedImageUri != null) 140.dp else 56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Medicine image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Overlay change button
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp),
                                shape = CircleShape,
                                color = Primary.copy(alpha = 0.85f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Change image",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Add Medicine Photo (optional)",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (initialReminder == null) "Set Reminder" else "Update Reminder",
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), dosage.trim(), timeText, selectedImageUri)
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
