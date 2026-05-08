package com.lifeplus.healthcare.ui.screens.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.*
import com.lifeplus.healthcare.ui.components.*
import com.lifeplus.healthcare.ui.theme.*

import com.lifeplus.healthcare.data.model.*
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntityScreen(
    type: String,
    entityId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val userPhone by authViewModel.phone.collectAsState()
    val userDistrict by authViewModel.district.collectAsState()

    // Dynamically get the right ViewModel based on type
    val hospitalViewModel: HospitalViewModel = hiltViewModel()
    val clinicViewModel: ClinicViewModel = hiltViewModel()
    val pharmacyViewModel: PharmacyViewModel = hiltViewModel()
    val ambulanceViewModel: AmbulanceViewModel = hiltViewModel()
    val bloodBankViewModel: BloodBankViewModel = hiltViewModel()
    val diagnosticViewModel: DiagnosticViewModel = hiltViewModel()
    val bloodOrgViewModel: BloodOrgViewModel = hiltViewModel()
    
    val hospitalState by hospitalViewModel.state.collectAsState()
    val clinicState by clinicViewModel.state.collectAsState()
    val pharmacyState by pharmacyViewModel.state.collectAsState()
    val ambulanceState by ambulanceViewModel.state.collectAsState()
    val bloodBankState by bloodBankViewModel.state.collectAsState()
    val diagnosticState by diagnosticViewModel.state.collectAsState()
    val bloodOrgState by bloodOrgViewModel.state.collectAsState()
    
    val displayType = type.replace("_", " ").replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }
    var extraDetail by remember { mutableStateOf("") } // For specialties, tests, etc.
    var is24h by remember { mutableStateOf(false) }
    var isIcu by remember { mutableStateOf(false) }
    
    val districts = com.lifeplus.healthcare.util.DataConstants.districtsWithPlaceholder
    var selectedDistrict by remember { mutableStateOf(districts[0]) }
    var expandDistrict by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var districtError by remember { mutableStateOf<String?>(null) }

    // Auto-fill phone and district for new entity
    LaunchedEffect(entityId, userPhone, userDistrict) {
        if (entityId == null) {
            if (phone.isBlank()) phone = userPhone ?: ""
            if (selectedDistrict == districts[0]) selectedDistrict = userDistrict ?: districts[0]
        }
    }

    LaunchedEffect(entityId, type) {
        if (entityId != null) {
            when(type) {
                "hospital" -> hospitalViewModel.loadMy()
                "clinic" -> clinicViewModel.loadMy()
                "pharmacy" -> pharmacyViewModel.loadMy()
                "ambulance" -> ambulanceViewModel.loadMy()
                "blood_bank" -> bloodBankViewModel.loadMy()
                "diagnostic" -> diagnosticViewModel.loadMy()
                "blood_org" -> bloodOrgViewModel.loadMy()
            }
        }
    }

    LaunchedEffect(entityId, type, hospitalState.data, clinicState.data, pharmacyState.data, ambulanceState.data, bloodBankState.data, diagnosticState.data, bloodOrgState.data) {
        if (entityId != null) {
            when(type) {
                "hospital" -> hospitalState.data.find { it.id == entityId }?.let {
                    name = it.name; location = it.address; phone = it.phone; upazila = it.upazila; selectedDistrict = it.district; is24h = it.open24h; isIcu = it.icuAvailable
                }
                "clinic" -> clinicState.data.find { it.id == entityId }?.let {
                    name = it.name; location = it.address; phone = it.phone; upazila = it.upazila; selectedDistrict = it.district; extraDetail = it.specialties
                }
                "pharmacy" -> pharmacyState.data.find { it.id == entityId }?.let {
                    name = it.name; location = it.address; phone = it.phone; upazila = it.upazila; selectedDistrict = it.district; is24h = it.open24h
                }
                "ambulance" -> ambulanceState.data.find { it.id == entityId }?.let {
                    name = it.name; phone = it.phone; selectedDistrict = it.district; upazila = it.upazila; isIcu = it.icuEquipped
                }
                "blood_bank" -> bloodBankState.data.find { it.id == entityId }?.let {
                    name = it.name; location = it.address; phone = it.phone; upazila = it.upazila; selectedDistrict = it.district
                }
                "diagnostic" -> diagnosticState.data.find { it.id == entityId }?.let {
                    name = it.name; location = it.address; phone = it.phone; upazila = it.upazila; selectedDistrict = it.district; extraDetail = it.testsOffered
                }
                "blood_org" -> bloodOrgState.data.find { it.id == entityId }?.let {
                    name = it.name; location = it.address; phone = it.phone; upazila = it.upazila; selectedDistrict = it.district
                }
            }
        }
    }

    val currentActionState = when(type) {
        "hospital" -> hospitalViewModel.action.collectAsState().value
        "clinic" -> clinicViewModel.action.collectAsState().value
        "pharmacy" -> pharmacyViewModel.action.collectAsState().value
        "ambulance" -> ambulanceViewModel.action.collectAsState().value
        "blood_bank" -> bloodBankViewModel.action.collectAsState().value
        "diagnostic" -> diagnosticViewModel.action.collectAsState().value
        "blood_org" -> bloodOrgViewModel.action.collectAsState().value
        else -> ActionUiState()
    }
    val currentListState = when(type) {
        "hospital" -> hospitalState
        "clinic" -> clinicState
        "pharmacy" -> pharmacyState
        "ambulance" -> ambulanceState
        "blood_bank" -> bloodBankState
        "diagnostic" -> diagnosticState
        "blood_org" -> bloodOrgState
        else -> ListUiState()
    }

    LaunchedEffect(currentActionState.isSuccess) {
        if (currentActionState.isSuccess) {
            val msg = if (entityId == null) "$displayType added successfully" else "$displayType updated successfully"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(currentActionState.error) {
        if (!currentActionState.error.isNullOrBlank()) {
            Toast.makeText(context, currentActionState.error, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = if (entityId == null) "Add $displayType" else "Edit $displayType",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Fill in the details below",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 56.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (entityId != null && currentListState.isLoading && currentListState.data.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            InputField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = "Name",
                placeholder = "Enter name",
                isError = nameError != null,
                errorMessage = nameError
            )
            InputField(value = location, onValueChange = { location = it }, label = "Address", placeholder = "Enter address")
            InputField(value = phone, onValueChange = { phone = it }, label = "Phone", placeholder = "Enter phone")
            InputField(value = upazila, onValueChange = { upazila = it }, label = "Upazila/Area", placeholder = "Enter upazila")

            DropdownField(
                label = "District",
                selected = selectedDistrict,
                expanded = expandDistrict,
                onExpandedChange = { expandDistrict = it },
                items = districts,
                onItemSelected = {
                    selectedDistrict = it
                    districtError = null
                }
            )
            if (districtError != null) {
                Text(
                    text = districtError.orEmpty(),
                    color = ErrorColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Dynamic fields based on type
            when (type) {
                "hospital" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = is24h, onCheckedChange = { is24h = it })
                        Text("Open 24 Hours", color = TextPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(checked = isIcu, onCheckedChange = { isIcu = it })
                        Text("ICU Available", color = TextPrimary)
                    }
                }
                "pharmacy" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = is24h, onCheckedChange = { is24h = it })
                        Text("Open 24 Hours", color = TextPrimary)
                    }
                }
                "ambulance" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isIcu, onCheckedChange = { isIcu = it })
                        Text("ICU Equipped", color = TextPrimary)
                    }
                }
                "clinic" -> {
                    InputField(value = extraDetail, onValueChange = { extraDetail = it }, label = "Specialties", placeholder = "e.g. Cardiology, Dental")
                }
                "diagnostic" -> {
                    InputField(value = extraDetail, onValueChange = { extraDetail = it }, label = "Tests Offered", placeholder = "e.g. X-Ray, Blood Test")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = if (entityId == null) "Submit Request" else "Update Details",
                onClick = { 
                    val validation = validateEntityForm(name, selectedDistrict, districts[0])
                    nameError = validation.nameError
                    districtError = validation.districtError
                    if (!validation.shouldSubmit) return@PrimaryButton

                    if (entityId == null) {
                        when(type) {
                            "hospital" -> hospitalViewModel.create(Hospital(name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, open24h = is24h, icuAvailable = isIcu))
                            "clinic" -> clinicViewModel.create(Clinic(name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, specialties = extraDetail))
                            "pharmacy" -> pharmacyViewModel.create(Pharmacy(name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, open24h = is24h))
                            "ambulance" -> ambulanceViewModel.create(Ambulance(name = name, district = selectedDistrict, upazila = upazila, phone = phone, icuEquipped = isIcu))
                            "blood_bank" -> bloodBankViewModel.create(BloodBank(name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone))
                            "diagnostic" -> diagnosticViewModel.create(DiagnosticCenter(name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, testsOffered = extraDetail))
                            "blood_org" -> bloodOrgViewModel.create(BloodOrganization(name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone))
                        }
                    } else {
                        when(type) {
                            "hospital" -> hospitalViewModel.update(entityId, Hospital(id = entityId, name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, open24h = is24h, icuAvailable = isIcu))
                            "clinic" -> clinicViewModel.update(entityId, Clinic(id = entityId, name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, specialties = extraDetail))
                            "pharmacy" -> pharmacyViewModel.update(entityId, Pharmacy(id = entityId, name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, open24h = is24h))
                            "ambulance" -> ambulanceViewModel.update(entityId, Ambulance(id = entityId, name = name, district = selectedDistrict, upazila = upazila, phone = phone, icuEquipped = isIcu))
                            "blood_bank" -> bloodBankViewModel.update(entityId, BloodBank(id = entityId, name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone))
                            "diagnostic" -> diagnosticViewModel.update(entityId, DiagnosticCenter(id = entityId, name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone, testsOffered = extraDetail))
                            "blood_org" -> bloodOrgViewModel.update(entityId, BloodOrganization(id = entityId, name = name, address = location, district = selectedDistrict, upazila = upazila, phone = phone))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (entityId == null) SuccessColor else Primary,
                contentColor = Color.White,
                isLoading = currentActionState.isLoading
            )

            Text(
                text = "Admin approval required for new listings.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
