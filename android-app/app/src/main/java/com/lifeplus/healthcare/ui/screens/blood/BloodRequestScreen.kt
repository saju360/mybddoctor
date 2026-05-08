package com.lifeplus.healthcare.ui.screens.blood

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.BloodRequestViewModel
import com.lifeplus.healthcare.ui.components.*
import com.lifeplus.healthcare.ui.theme.*
import com.lifeplus.healthcare.data.model.BloodRequest

import com.lifeplus.healthcare.presentation.viewmodel.DonorViewModel

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.lifeplus.healthcare.data.model.Donor

import com.lifeplus.healthcare.util.DataConstants

@Composable
fun MatchedDonorsSection(donors: List<Donor>) {
    val context = LocalContext.current
    var showCallConfirm by remember { mutableStateOf<Donor?>(null) }

    if (showCallConfirm != null) {
        AlertDialog(
            onDismissRequest = { showCallConfirm = null },
            title = { Text("Confirm Call") },
            text = { Text("Do you want to call Donor #${showCallConfirm?.id} at ${showCallConfirm?.contactPhone}?") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${showCallConfirm?.contactPhone}"))
                    context.startActivity(intent)
                    showCallConfirm = null
                }) {
                    Text("Call", color = SuccessColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallConfirm = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Matched Eligible Donors",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = SuccessColor
            )
            Surface(
                color = SuccessColor.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    "${donors.size} Available",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        donors.take(3).forEach { donor ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(donor.bloodGroup.replace("_POS","+").replace("_NEG","-"), style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Donor #${donor.id}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("${donor.district}, ${donor.upazila}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(
                        onClick = {
                            showCallConfirm = donor
                        },
                        modifier = Modifier.background(SuccessColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Call, null, tint = SuccessColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodRequestScreen(
    onNavigateBack: () -> Unit,
    donorId: Long = -1L,
    viewModel: BloodRequestViewModel = hiltViewModel(),
    donorViewModel: DonorViewModel = hiltViewModel()
) {
    val bloodGroups = listOf("Select Blood Group") + DataConstants.bloodGroups
    var selectedBloodGroup by remember { mutableStateOf(bloodGroups[0]) }
    var expandBloodGroup by remember { mutableStateOf(false) }

    val districts = DataConstants.districtsWithPlaceholder
    var selectedDistrict by remember { mutableStateOf(districts[0]) }
    var expandDistrict by remember { mutableStateOf(false) }

    val donorState by donorViewModel.state.collectAsState()
    val matchedDonors = remember(selectedBloodGroup, selectedDistrict, donorState) {
        if (selectedBloodGroup == bloodGroups[0]) emptyList()
        else {
            val bg = BloodRequest.parseBloodGroup(selectedBloodGroup)
            donorState.data.filter { 
                it.bloodGroup == bg && 
                (selectedDistrict == districts[0] || it.district == selectedDistrict) &&
                it.availableNow
            }
        }
    }

    LaunchedEffect(Unit) {
        donorViewModel.loadAll()
    }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var patientName by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    val urgencies = listOf("Normal", "URGENT")
    var selectedUrgency by remember { mutableStateOf(urgencies[0]) }
    var expandUrgency by remember { mutableStateOf(false) }

    val state by viewModel.action.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            android.widget.Toast.makeText(context, "Blood Request Posted Successfully!", android.widget.Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        if (!state.error.isNullOrBlank()) {
            android.widget.Toast.makeText(context, state.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = if (donorId != -1L) "Direct Blood Request" else "Blood Request",
                subtitle = if (donorId != -1L) "Sending request to a specific donor" else "Post an urgent request for help",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Info Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = Accent.copy(alpha = 0.08f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.WaterDrop, null, tint = Accent, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(if (donorId != -1L) "Direct Request" else "Urgent Need?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text(if (donorId != -1L) "Donor will be notified directly" else "Fill details to notify nearby donors", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                Text(
                    text = "Request Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Form Fields wrapped in a clean container
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        
                        DropdownField(
                            label = "Blood Group Required",
                            selected = selectedBloodGroup,
                            expanded = expandBloodGroup,
                            onExpandedChange = { expandBloodGroup = it },
                            items = bloodGroups,
                            onItemSelected = { selectedBloodGroup = it }
                        )

                        InputField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = "Patient Name",
                            placeholder = "Enter patient's full name",
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary) }
                        )

                        InputField(
                            value = hospitalName,
                            onValueChange = { hospitalName = it },
                            label = "Hospital / Clinic",
                            placeholder = "Where is blood needed?",
                            leadingIcon = { Icon(Icons.Default.LocalHospital, null, tint = Primary) }
                        )

                        InputField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = "Emergency Contact",
                            placeholder = "Phone number",
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary) }
                        )

                        DropdownField(
                            label = "Location (District)",
                            selected = selectedDistrict,
                            expanded = expandDistrict,
                            onExpandedChange = { expandDistrict = it },
                            items = districts,
                            onItemSelected = { selectedDistrict = it }
                        )

                        // Urgency Toggle
                        Text("Urgency Level", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            urgencies.forEach { level ->
                                FilterChip(
                                    selected = selectedUrgency == level,
                                    onClick = { selectedUrgency = level },
                                    label = { Text(level) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (level == "URGENT") ErrorColor else Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        InputField(
                            value = message,
                            onValueChange = { message = it },
                            label = "Additional Info",
                            placeholder = "Any special requirements...",
                            modifier = Modifier.height(100.dp),
                            singleLine = false
                        )
                    }
                }

                // Matched Donors Section
                if (matchedDonors.isNotEmpty()) {
                    MatchedDonorsSection(matchedDonors)
                }

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = "Post Request",
                    onClick = { 
                        if (selectedBloodGroup != bloodGroups[0] && selectedDistrict != districts[0] && patientName.isNotBlank()) {
                            viewModel.post(
                                BloodRequest(
                                    bloodGroup = BloodRequest.parseBloodGroup(selectedBloodGroup),
                                    district = selectedDistrict,
                                    urgency = selectedUrgency,
                                    patientName = patientName,
                                    hospitalName = hospitalName,
                                    contactPhone = contactPhone,
                                    notes = message,
                                    donorId = if (donorId != -1L) donorId else null,
                                    status = if (donorId != -1L) "PENDING" else "OPEN"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    containerColor = if (selectedUrgency == "URGENT") ErrorColor else Primary,
                    isLoading = state.isLoading
                )

                Text(
                    text = "By posting, you agree to our privacy policy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
