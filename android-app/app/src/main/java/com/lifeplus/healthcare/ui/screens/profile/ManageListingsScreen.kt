package com.lifeplus.healthcare.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.*
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.theme.*

@Composable
fun ManageListingsScreen(
    onNavigateBack: () -> Unit,
    onAddEntity: (String) -> Unit,
    onEditEntity: (String, Long) -> Unit,
    onAddDoctor: (Long) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userRole by authViewModel.role.collectAsState()
    val isAuthorized = userRole?.uppercase() == "ADMIN" || userRole?.uppercase() == "OWNER"

    if (!isAuthorized) {
        // Simple unauthorized state or redirect back
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Hospitals", "Clinics", "Pharmacies", "Ambulances", "Blood Banks", "Diagnostics", "Doctors", "Blood Orgs", "Donors")
    
    // Shared delete observer logic for all viewmodels if needed, 
    // but for simplicity we'll just use the context for toasts inside the sub-lists if possible 
    // or just pass a toast callback.

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "My Listings",
                subtitle = "Manage your healthcare services",
                onBackClick = onNavigateBack
            )

            // Modern Tab Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    edgePadding = 24.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(4.dp)
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Primary)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 14.sp
                                ) 
                            },
                            selectedContentColor = Primary,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_content"
                    ) { targetIndex ->
                        when (tabs[targetIndex]) {
                            "Hospitals" -> HospitalManageList(onEdit = { onEditEntity("hospital", it) }, onAddDoctor = onAddDoctor)
                            "Clinics" -> ClinicManageList(onEdit = { onEditEntity("clinic", it) })
                            "Pharmacies" -> PharmacyManageList(onEdit = { onEditEntity("pharmacy", it) })
                            "Ambulances" -> AmbulanceManageList(onEdit = { onEditEntity("ambulance", it) })
                            "Blood Banks" -> BloodBankManageList(onEdit = { onEditEntity("blood_bank", it) })
                            "Diagnostics" -> DiagnosticManageList(onEdit = { onEditEntity("diagnostic", it) })
                            "Doctors" -> DoctorManageList(onEdit = { id -> onEditEntity("doctor", id) })
                            "Blood Orgs" -> BloodOrgManageList(onEdit = { onEditEntity("blood_org", it) })
                            "Donors" -> DonorManageList(onEdit = { onEditEntity("donor", it) })
                        }
                    }
                }

                if (tabs[selectedTab] != "Doctors" && tabs[selectedTab] != "Donors") {
                    Surface(
                        onClick = {
                            val type = when (tabs[selectedTab]) {
                                "Blood Banks" -> "blood_bank"
                                "Diagnostics" -> "diagnostic"
                                "Blood Orgs"  -> "blood_org"
                                else -> tabs[selectedTab].lowercase().removeSuffix("s")
                            }
                            onAddEntity(type)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .size(60.dp)
                            .shadow(12.dp, CircleShape, spotColor = Primary),
                        shape = CircleShape,
                        color = Primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add New", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalManageList(
    viewModel: HospitalViewModel = hiltViewModel(),
    onEdit: (Long) -> Unit,
    onAddDoctor: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadMy() }

    LaunchedEffect(action.isSuccess) {
        if (action.isSuccess) android.widget.Toast.makeText(context, "Deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) {
        PremiumEmptyState("No hospitals registered.")
    } else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { hospital ->
                PremiumManageCard(
                    title = hospital.name,
                    subtitle = "${hospital.address}, ${hospital.district}",
                    status = hospital.status,
                    adminNotes = hospital.adminNotes,
                    onEdit = { onEdit(hospital.id) },
                    onDelete = { viewModel.delete(hospital.id) },
                    extraAction = {
                        Button(
                            onClick = { onAddDoctor(hospital.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Doctor", color = Primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ClinicManageList(viewModel: ClinicViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadMy() }
    
    LaunchedEffect(action.isSuccess) {
        if (action.isSuccess) android.widget.Toast.makeText(context, "Deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
    }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No clinics registered.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { clinic ->
                PremiumManageCard(
                    title = clinic.name,
                    subtitle = clinic.address,
                    status = clinic.status,
                    adminNotes = clinic.adminNotes,
                    onEdit = { onEdit(clinic.id) },
                    onDelete = { viewModel.delete(clinic.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun PharmacyManageList(viewModel: PharmacyViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No pharmacies registered.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.name,
                    subtitle = item.address,
                    status = item.status,
                    adminNotes = item.adminNotes,
                    onEdit = { onEdit(item.id) },
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun AmbulanceManageList(viewModel: AmbulanceViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No ambulances registered.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.name,
                    subtitle = item.district,
                    status = item.status,
                    adminNotes = item.adminNotes,
                    onEdit = { onEdit(item.id) },
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun BloodBankManageList(viewModel: BloodBankViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No blood banks registered.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.name, 
                    subtitle = item.address, 
                    donorCount = item.donorCount,
                    onEdit = { onEdit(item.id) }, 
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DiagnosticManageList(viewModel: DiagnosticViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No diagnostics registered.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.name,
                    subtitle = item.address,
                    status = item.status,
                    adminNotes = item.adminNotes,
                    onEdit = { onEdit(item.id) },
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DoctorManageList(viewModel: DoctorViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No doctor profiles.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.name,
                    subtitle = "${item.specialty} • ${item.district}",
                    status = item.status,
                    adminNotes = item.adminNotes,
                    onEdit = { onEdit(item.id) },
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun BloodOrgManageList(viewModel: BloodOrgViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No blood orgs registered.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.name, 
                    subtitle = item.address, 
                    donorCount = item.donorCount,
                    onEdit = { onEdit(item.id) }, 
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DonorManageList(viewModel: DonorViewModel = hiltViewModel(), onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMy() }

    if (state.isLoading && state.data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
    } else if (state.data.isEmpty()) PremiumEmptyState("No donor registration.")
    else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.data) { item ->
                PremiumManageCard(
                    title = item.fullName,
                    subtitle = "${item.bloodGroup.replace("_POS", "+").replace("_NEG", "-")} • ${item.district}",
                    status = item.status,
                    adminNotes = item.rejectionReason,
                    onEdit = { onEdit(item.id) },
                    onDelete = { viewModel.delete(item.id) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun PremiumManageCard(
    title: String?,
    subtitle: String?,
    status: String? = "APPROVED",
    adminNotes: String? = null,
    donorCount: Int? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    extraAction: @Composable (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Business, null, tint = Primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title?.takeIf { it.isNotBlank() } ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (status?.uppercase() == "APPROVED") {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, "Verified", tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = subtitle?.takeIf { it.isNotBlank() } ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    
                    if (donorCount != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            com.lifeplus.healthcare.ui.components.AnimatedCounter(
                                targetValue = donorCount,
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessColor
                            )
                            Text(" Donors", style = MaterialTheme.typography.labelSmall, color = SuccessColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { 
                        Icon(Icons.Default.Edit, "Edit", tint = Primary, modifier = Modifier.size(18.dp)) 
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, "Delete", tint = ErrorColor, modifier = Modifier.size(18.dp)) 
                    }
                }
            }

            // Approval Status Section
            if (status?.uppercase() != "APPROVED") {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (status?.uppercase() == "REJECTED") ErrorColor.copy(alpha = 0.08f) else WarningColor.copy(alpha = 0.08f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (status?.uppercase() == "REJECTED") Icons.Default.Cancel else Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (status?.uppercase() == "REJECTED") ErrorColor else WarningColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (status?.uppercase() == "REJECTED") "Listing Rejected" else "Pending Admin Approval",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (status?.uppercase() == "REJECTED") ErrorColor else WarningColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (status?.uppercase() == "REJECTED" && !adminNotes.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Reason: $adminNotes",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorColor,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        } else if (status?.uppercase() == "PENDING") {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Your listing is being reviewed by our team.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningColor,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        }
                    }
                }
            }

            if (extraAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Surface2Light, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                extraAction()
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Listing") },
            text = { Text("Are you sure you want to delete this listing?") },
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
fun PremiumEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Surface2Light
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Inventory2, null, modifier = Modifier.size(48.dp), tint = TextHint)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = message, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(text = "Register a new service to see it here.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
