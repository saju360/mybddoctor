package com.lifeplus.healthcare.ui.screens.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.R
import com.lifeplus.healthcare.presentation.viewmodel.EmergencyViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.ui.components.DropdownField
import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.theme.*

import com.lifeplus.healthcare.util.DataConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmergencyViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.fullName.collectAsState()
    val userPhone by authViewModel.phone.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val districts = DataConstants.districtsWithPlaceholder
    var selectedDistrict by remember { mutableStateOf(districts[0]) }
    var expandDistrict by remember { mutableStateOf(false) }

    val emergencyTypes = DataConstants.emergencyTypesWithPlaceholder
    var selectedType by remember { mutableStateOf(emergencyTypes[0]) }
    var expandType by remember { mutableStateOf(false) }

    var showCallConfirm by remember { mutableStateOf<Pair<String, String>?>(null) }

    val history by viewModel.history.collectAsState()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(isLoggedIn, userName, userPhone) {
        if (isLoggedIn) {
            name = userName ?: ""
            phone = userPhone ?: ""
            viewModel.loadHistory()
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.clearState()
            // Show success message or stay on screen to see history
            // onNavigateBack() 
        }
    }

    if (showCallConfirm != null) {
        AlertDialog(
            onDismissRequest = { showCallConfirm = null },
            title = { Text(context.getString(R.string.call) + " " + showCallConfirm!!.first) },
            text = { Text("Are you sure you want to call ${showCallConfirm!!.first} (${showCallConfirm!!.second})?") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${showCallConfirm!!.first}"))
                    context.startActivity(intent)
                    showCallConfirm = null
                }) {
                    Text(context.getString(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallConfirm = null }) {
                    Text(context.getString(R.string.no))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmergencyGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = context.getString(R.string.title_emergency),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = context.getString(R.string.help_on_the_way),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = context.getString(R.string.emergency_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Quick Action Cards — real phone calls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickCallCard(
                        title = "999",
                        subtitle = context.getString(R.string.police_fire),
                        icon = Icons.Default.Security,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCallConfirm = "999" to context.getString(R.string.police_fire)
                        }
                    )
                    QuickCallCard(
                        title = "10655",
                        subtitle = context.getString(R.string.feature_ambulance),
                        icon = Icons.Default.LocalHospital,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCallConfirm = "10655" to context.getString(R.string.feature_ambulance)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickCallCard(
                        title = "10655",
                        subtitle = context.getString(R.string.feature_blood_bank),
                        icon = Icons.Default.Bloodtype,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCallConfirm = "10655" to context.getString(R.string.feature_blood_bank)
                        }
                    )
                    QuickCallCard(
                        title = "109",
                        subtitle = context.getString(R.string.women_child_help),
                        icon = Icons.Default.Woman,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCallConfirm = "109" to context.getString(R.string.women_child_help)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error Banner
                if (state.error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.error ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.isSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SuccessColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.emergency_request_sent), color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Emergency Form
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = context.getString(R.string.specific_emergency_alert),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = context.getString(R.string.fill_form_instruction),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        InputField(
                            value = name,
                            onValueChange = { name = it },
                            label = context.getString(R.string.hint_fullname),
                            placeholder = "John Doe"
                        )
                        
                        InputField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = context.getString(R.string.hint_phone),
                            placeholder = "01XXXXXXXXX"
                        )

                        DropdownField(
                            label = "District",
                            selected = selectedDistrict,
                            expanded = expandDistrict,
                            onExpandedChange = { expandDistrict = it },
                            items = districts,
                            onItemSelected = { selectedDistrict = it }
                        )

                        DropdownField(
                            label = "Emergency Type",
                            selected = selectedType,
                            expanded = expandType,
                            onExpandedChange = { expandType = it },
                            items = emergencyTypes,
                            onItemSelected = { selectedType = it }
                        )

                        InputField(
                            value = location,
                            onValueChange = { location = it },
                            label = context.getString(R.string.exact_location),
                            placeholder = context.getString(R.string.location_placeholder)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            text = context.getString(R.string.send_alert_now),
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank() && location.isNotBlank()) {
                                    if (selectedDistrict == districts[0] || selectedType == emergencyTypes[0]) {
                                        // Maybe show a toast or local error state
                                        // For now, we'll just not send if placeholder is selected
                                    } else {
                                        viewModel.send(name, phone, selectedDistrict, selectedType, location)
                                    }
                                }
                            },
                            isLoading = state.isLoading,
                            containerColor = ErrorColor
                        )
                    }
                }
                
                if (isLoggedIn && history.data.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        stringResource(R.string.recent_history),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    history.data.take(5).forEach { req ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(req.emergencyType, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(req.district, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(
                                    color = if (req.status == "PENDING") WarningColor else SuccessColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(req.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun QuickCallCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}
