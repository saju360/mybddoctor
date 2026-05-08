package com.lifeplus.healthcare.ui.screens.blood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.data.model.Donor
import com.lifeplus.healthcare.presentation.viewmodel.DonorViewModel
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.DropdownField
import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.theme.ErrorColor
import com.lifeplus.healthcare.ui.theme.Primary
import com.lifeplus.healthcare.ui.theme.SuccessColor
import com.lifeplus.healthcare.ui.theme.TextPrimary
import com.lifeplus.healthcare.ui.theme.TextSecondary
import com.lifeplus.healthcare.ui.theme.WarningColor
import com.lifeplus.healthcare.util.DataConstants

@Composable
fun DonorRegistrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: DonorViewModel = hiltViewModel()
) {
    val currentDonor by viewModel.currentDonor.collectAsState()
    val action by viewModel.action.collectAsState()
    val organizations by viewModel.organizations.collectAsState()

    var fullName by remember { mutableStateOf("") }
    val bloodGroups = DataConstants.bloodGroupsWithPlaceholder   // "Select Blood Group", "A+", "A-", …
    val districts   = DataConstants.districtsWithPlaceholder     // "Select District", "Dhaka", …
    
    var bloodGroup by remember { mutableStateOf(bloodGroups[0]) }
    var district   by remember { mutableStateOf(districts[0]) }
    
    var expandBlood by remember { mutableStateOf(false) }
    var expandDistrict by remember { mutableStateOf(false) }

    var upazila by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("") }
    var physicalHistory by remember { mutableStateOf("") }
    var availableNow by remember { mutableStateOf(true) }
    
    var selectedOrgName by remember { mutableStateOf("None / Individual") }
    var manualOrgName by remember { mutableStateOf("") }
    var expandOrg by remember { mutableStateOf(false) }
    var showManualOrg by remember { mutableStateOf(false) }

    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadMyProfile()
    }

    LaunchedEffect(currentDonor?.id) {
        currentDonor?.let { donor ->
            fullName = donor.fullName
            // Convert API format (A_POS) → display format (A+)
            bloodGroup = donor.bloodGroup
                .replace("_POS", "+").replace("_NEG", "-")
                .replace("AB+", "AB+").replace("AB-", "AB-")
            district = donor.district.ifBlank { districts[0] }
            upazila = donor.upazila
            contactPhone = donor.contactPhone
            lastDonationDate = donor.lastDonationDate.orEmpty()
            physicalHistory = donor.physicalHistory
            availableNow = donor.availableNow
            
            if (donor.organizationId != null) {
                selectedOrgName = donor.organizationName ?: "Unknown"
            } else if (!donor.organizationName.isNullOrBlank()) {
                selectedOrgName = "Other (Manual)"
                manualOrgName = donor.organizationName
                showManualOrg = true
            }
        }
    }

    LaunchedEffect(action.isSuccess) {
        if (action.isSuccess) {
            android.widget.Toast.makeText(context, "Donor profile saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(action.error) {
        if (!action.error.isNullOrBlank()) {
            android.widget.Toast.makeText(context, action.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Donor Profile",
                subtitle = "Create or update your donor information",
                onBackClick = onNavigateBack
            )

            // ── PENDING: show waiting screen, no editing ──────────────────
            if (currentDonor != null && currentDonor!!.status == "PENDING") {
                DonorPendingScreen()
                return@AppBackground
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Status banner (APPROVED / REJECTED / none) ────────────
                if (currentDonor != null) {
                    DonorStatusCard(
                        status = currentDonor!!.status,
                        rejectionReason = currentDonor!!.rejectionReason
                    )
                }

                InputField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorText = null
                    },
                    label = "Full Name",
                    placeholder = "Enter your full name"
                )
                
                DropdownField(
                    label = "Blood Group",
                    selected = bloodGroup,
                    expanded = expandBlood,
                    onExpandedChange = { expandBlood = it },
                    items = bloodGroups,
                    onItemSelected = { bloodGroup = it }
                )

                DropdownField(
                    label = "District",
                    selected = district,
                    expanded = expandDistrict,
                    onExpandedChange = { expandDistrict = it },
                    items = districts,
                    onItemSelected = { district = it }
                )

                InputField(
                    value = upazila,
                    onValueChange = { upazila = it },
                    label = "Upazila",
                    placeholder = "Enter upazila"
                )

                // Organization Selection
                val orgOptions = listOf("None / Individual") + organizations.map { it.name } + listOf("Other (Manual)")
                DropdownField(
                    label = "Blood Organization (Member of?)",
                    selected = selectedOrgName,
                    expanded = expandOrg,
                    onExpandedChange = { expandOrg = it },
                    items = orgOptions,
                    onItemSelected = { 
                        selectedOrgName = it
                        showManualOrg = it == "Other (Manual)"
                    }
                )

                if (showManualOrg) {
                    InputField(
                        value = manualOrgName,
                        onValueChange = { manualOrgName = it },
                        label = "Organization Name",
                        placeholder = "Enter organization name manually"
                    )
                }

                InputField(
                    value = contactPhone,
                    onValueChange = {
                        contactPhone = it
                        errorText = null
                    },
                    label = "Contact Phone",
                    placeholder = "01XXXXXXXXX"
                )
                InputField(
                    value = lastDonationDate,
                    onValueChange = { lastDonationDate = it },
                    label = "Last Donation Date",
                    placeholder = "yyyy-MM-dd (optional)"
                )
                InputField(
                    value = physicalHistory,
                    onValueChange = { physicalHistory = it },
                    label = "Physical History",
                    placeholder = "Optional notes"
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Available Now",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Switch(
                            checked = availableNow,
                            onCheckedChange = { availableNow = it }
                        )
                    }
                }

                val actionError = errorText ?: action.error
                if (!actionError.isNullOrBlank()) {
                    Text(
                        text = actionError,
                        color = WarningColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        if (fullName.isBlank() || bloodGroup == bloodGroups[0] || district == districts[0] || contactPhone.isBlank()) {
                            errorText = "Please fill full name, blood group, district, and phone."
                            return@Button
                        }

                        val orgId = if (selectedOrgName != "None / Individual" && selectedOrgName != "Other (Manual)") {
                            organizations.find { it.name == selectedOrgName }?.id
                        } else null
                        
                        val orgName = if (selectedOrgName == "Other (Manual)") {
                            manualOrgName.trim()
                        } else if (selectedOrgName != "None / Individual") {
                            selectedOrgName
                        } else null

                        val donor = Donor(
                            id = currentDonor?.id ?: 0L,
                            fullName = fullName.trim(),
                            bloodGroup = Donor.parseBloodGroup(bloodGroup.trim()),
                            district = district.trim(),
                            upazila = upazila.trim(),
                            contactPhone = contactPhone.trim(),
                            availableNow = availableNow,
                            lastDonationDate = lastDonationDate.trim().ifBlank { null },
                            physicalHistory = physicalHistory.trim(),
                            organizationId = orgId,
                            organizationName = orgName
                        )

                        val existingId = currentDonor?.id ?: 0L
                        if (existingId > 0) viewModel.update(existingId, donor) else viewModel.register(donor)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !action.isLoading
                ) {
                    if (action.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.height(18.dp))
                    } else {
                        Text(if ((currentDonor?.id ?: 0L) > 0) "Update Donor Profile" else "Create Donor Profile")
                    }
                }

                // Delete button — only for APPROVED donors
                if (currentDonor != null && currentDonor!!.status == "APPROVED") {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                        enabled = !action.isLoading
                    ) {
                        Text("Delete Donor Profile")
                    }
                    if (showDeleteConfirm) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete Donor Profile") },
                            text = { Text("Are you sure? You will be removed from the donor list.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.delete(currentDonor!!.id)
                                        showDeleteConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                                ) { Text("Delete") }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }

                if (action.isSuccess) {
                    Text(
                        text = "Donor profile saved successfully.",
                        color = SuccessColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun DonorPendingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = WarningColor.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = WarningColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Application Under Review",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your donor registration has been submitted and is waiting for admin approval. You will be notified once it is reviewed.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = WarningColor.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = WarningColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Once approved, your profile will appear in the donor list and you can edit or delete it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun DonorStatusCard(status: String, rejectionReason: String?) {
    val isApproved = status == "APPROVED"
    val isRejected = status == "REJECTED"
    val color = when {
        isApproved -> SuccessColor
        isRejected -> ErrorColor
        else -> WarningColor
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.10f),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = when {
                    isApproved -> "✓ Profile Approved — visible in donor list"
                    isRejected -> "✗ Application Rejected"
                    else -> "⏳ Pending Review"
                },
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            if (isRejected && !rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: $rejectionReason",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please update your information and resubmit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}