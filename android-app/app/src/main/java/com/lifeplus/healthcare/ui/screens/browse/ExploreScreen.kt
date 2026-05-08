package com.lifeplus.healthcare.ui.screens.browse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.theme.*
import com.lifeplus.healthcare.ui.components.AppBackground

import com.lifeplus.healthcare.presentation.viewmodel.DoctorViewModel
import com.lifeplus.healthcare.presentation.viewmodel.HospitalViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit = {},
    doctorViewModel: DoctorViewModel = hiltViewModel(),
    hospitalViewModel: HospitalViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val doctorState by doctorViewModel.state.collectAsState()
    val hospitalState by hospitalViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        doctorViewModel.loadAll()
        hospitalViewModel.loadAll()
    }
    
    val categories = listOf(
        ServiceCategory("Hospitals", Icons.Default.LocalHospital, Primary, "browse_hospitals"),
        ServiceCategory("Doctors", Icons.Default.Person, Color(0xFF6366F1), "browse_doctors"),
        ServiceCategory("Ambulances", Icons.Default.DirectionsCar, WarningColor, "browse_ambulances"),
        ServiceCategory("Pharmacies", Icons.Default.LocalPharmacy, Color(0xFFEC4899), "browse_pharmacies"),
        ServiceCategory("Blood Banks", Icons.Default.Bloodtype, Color(0xFFEF4444), "browse_blood_banks"),
        ServiceCategory("Donors", Icons.Default.Favorite, Color(0xFFEC4899), "browse_donors"),
        ServiceCategory("Clinics", Icons.Default.MedicalServices, Color(0xFF10B981), "browse_clinics"),
        ServiceCategory("Diagnostics", Icons.Default.Science, Color(0xFF8B5CF6), "browse_diagnostics"),
        ServiceCategory("Telemedicine", Icons.Default.VideoCall, Color(0xFF3B82F6), "telemedicine"),
        ServiceCategory("Emergency", Icons.Default.Warning, Color(0xFFD32F2F), "emergency"),
        ServiceCategory("Blood Req", Icons.Default.Favorite, Color(0xFFE91E63), "blood_request"),
        ServiceCategory("Blood Orgs", Icons.Default.Business, Color(0xFFD32F2F), "browse_blood_orgs")
    )

    val toolCategories = listOf(
        ServiceCategory("BMI Calc", Icons.Default.MonitorWeight, Color(0xFF4CAF50), "bmi_calc"),
        ServiceCategory("First Aid", Icons.Default.MedicalInformation, Color(0xFFF44336), "first_aid"),
        ServiceCategory("Health Tips", Icons.Default.Lightbulb, Color(0xFFFF9800), "health_tips"),
        ServiceCategory("Vaccination", Icons.Default.Vaccines, Color(0xFF2196F3), "vaccination")
    )

    val filteredCategories = categories.filter { 
        it.title.contains(searchQuery, ignoreCase = true) 
    }

    val filteredTools = toolCategories.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Header with Search
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Explore Services",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        placeholder = { Text("Find health services...", color = TextHint) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Surface2Light,
                            unfocusedContainerColor = Surface2Light,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        ),
                        singleLine = true
                    )
                }
            }

            if (filteredCategories.isEmpty() && filteredTools.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextHint
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No services found for \"$searchQuery\"", color = TextSecondary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        Text("Service Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    
                    items(filteredCategories) { category ->
                        ExploreCard(category, onNavigate)
                    }

                    if (filteredTools.isNotEmpty()) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Health Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        items(filteredTools) { tool ->
                            ExploreCard(tool, onNavigate)
                        }
                    }

                    if (searchQuery.isEmpty()) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Featured Doctors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        items(doctorState.data.take(4)) { doctor ->
                            FeaturedEntityCard(
                                title = doctor.name.ifBlank { "Doctor" },
                                subtitle = doctor.specialty.ifBlank { "Specialist" },
                                image = null, // Placeholder or actual image URL
                        onClick = {
                            onNavigate(
                                "details?title=${android.net.Uri.encode(doctor.name)}" +
                                    "&subtitle=${android.net.Uri.encode(doctor.specialty)}&type=doctor" +
                                    "&phone=${android.net.Uri.encode(doctor.phone)}&address=${android.net.Uri.encode(doctor.district)}&entityId=${doctor.id}" +
                                    "&hours=${android.net.Uri.encode(doctor.consultationHours)}"
                            )
                        }
                            )
                        }

                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Top Hospitals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        items(hospitalState.data.take(4)) { hospital ->
                            FeaturedEntityCard(
                                title = hospital.name.ifBlank { "Hospital" },
                                subtitle = hospital.district.ifBlank { "District" },
                                image = null,
                        onClick = {
                            onNavigate(
                                "details?title=${android.net.Uri.encode(hospital.name)}" +
                                    "&subtitle=${android.net.Uri.encode(hospital.district)}&type=hospital" +
                                    "&phone=${android.net.Uri.encode(hospital.phone)}&address=${android.net.Uri.encode(hospital.address)}&entityId=${hospital.id}"
                            )
                        }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedEntityCard(title: String, subtitle: String, image: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = Surface2Light
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Image, null, tint = TextHint)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun ExploreCard(category: ServiceCategory, onNavigate: (String) -> Unit) {
    Surface(
        onClick = { onNavigate(category.route) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = category.color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "View ${category.title.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

data class ServiceCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)
