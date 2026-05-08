package com.lifeplus.healthcare.ui.screens.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.data.model.Doctor
import com.lifeplus.healthcare.presentation.viewmodel.DoctorViewModel
import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.theme.BackgroundLight
import com.lifeplus.healthcare.ui.theme.Primary
import com.lifeplus.healthcare.ui.theme.Surface2Light
import com.lifeplus.healthcare.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDoctorScreen(
    hospitalId: Long,
    doctorId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: DoctorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var qualifications by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var consultationHours by remember { mutableStateOf("10:00 AM - 05:00 PM") }
    var effectiveHospitalId by remember { mutableStateOf(hospitalId) }
    var availableForTelemedicine by remember { mutableStateOf(false) }
    var available by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        if (doctorId != null && doctorId > 0L) viewModel.loadMy()
    }

    LaunchedEffect(state.data, doctorId) {
        val doctor = state.data.firstOrNull { it.id == doctorId }
        if (doctor != null) {
            name = doctor.name
            specialty = doctor.specialty
            qualifications = doctor.qualifications
            phone = doctor.phone
            district = doctor.district
            consultationHours = doctor.consultationHours.ifBlank { "10:00 AM - 05:00 PM" }
            effectiveHospitalId = doctor.hospitalId
            availableForTelemedicine = doctor.telemedicineAvailable
            available = doctor.available
        }
    }

    LaunchedEffect(action.isSuccess) {
        if (action.isSuccess) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (doctorId == null) "Add Doctor" else "Edit Doctor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface2Light)
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Primary.copy(alpha = 0.1f), CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(40.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            InputField(value = name, onValueChange = { name = it }, label = "Doctor Name")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(value = specialty, onValueChange = { specialty = it }, label = "Specialty (e.g., Cardiology)")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(value = qualifications, onValueChange = { qualifications = it }, label = "Qualifications")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(value = phone, onValueChange = { phone = it }, label = "Contact Phone")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(value = district, onValueChange = { district = it }, label = "District", placeholder = "e.g. Dhaka")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(value = consultationHours, onValueChange = { consultationHours = it }, label = "Consultation Hours", placeholder = "e.g. 10:00 AM - 05:00 PM")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = availableForTelemedicine, onCheckedChange = { availableForTelemedicine = it })
                Text("Available for Telemedicine", color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = available, onCheckedChange = { available = it })
                Text("Currently Available", color = TextPrimary)
            }

            if (action.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(action.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            PrimaryButton(
                text = if (action.isLoading) "Saving..." else if (doctorId == null) "Add Doctor" else "Update Doctor",
                onClick = {
                    val doctor = Doctor(
                        id = doctorId ?: 0L,
                        name = name,
                        specialty = specialty,
                        qualifications = qualifications,
                        phone = phone,
                        district = district,
                        consultationHours = consultationHours,
                        available = available,
                        telemedicineAvailable = availableForTelemedicine,
                        hospitalId = effectiveHospitalId
                    )
                    if (doctorId != null && doctorId > 0L) {
                        viewModel.update(doctorId, doctor)
                    } else {
                        viewModel.create(doctor)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && specialty.isNotBlank() && effectiveHospitalId > 0L && !action.isLoading,
                isLoading = action.isLoading
            )
        }
    }
}
