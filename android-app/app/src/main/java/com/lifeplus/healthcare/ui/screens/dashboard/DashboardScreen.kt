@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.lifeplus.healthcare.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.presentation.viewmodel.DashboardViewModel
import com.lifeplus.healthcare.presentation.viewmodel.BloodRequestViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AppointmentViewModel
import com.lifeplus.healthcare.ui.theme.*
import com.lifeplus.healthcare.data.model.BloodRequest
import com.lifeplus.healthcare.data.model.Appointment
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.ShimmerItem
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.lifeplus.healthcare.ads.DynamicBannerAd
import com.lifeplus.healthcare.ads.RewardedSupportAction

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay

import com.lifeplus.healthcare.model.DashboardSlide
import com.lifeplus.healthcare.presentation.viewmodel.ReminderViewModel
import com.lifeplus.healthcare.data.model.MedicineReminder
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun UpcomingAppointmentsSection(appointments: List<Appointment>, onViewAll: () -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Upcoming Appointments", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onViewAll) {
                Text("View All", color = Primary, fontWeight = FontWeight.Bold)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(appointments) { appointment ->
                AppointmentQuickCard(appointment)
            }
        }
    }
}

@Composable
fun AppointmentQuickCard(appointment: Appointment) {
    Surface(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(20.dp),
        color = Primary.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Event, null, tint = Primary)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(appointment.doctorName.ifBlank { "Doctor" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${appointment.date} • ${appointment.time}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Surface(
                    color = if (appointment.status == "CONFIRMED") SuccessColor.copy(alpha = 0.1f) else WarningColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        appointment.status,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (appointment.status == "CONFIRMED") SuccessColor else WarningColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MarqueeSection(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Primary.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Primary,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    "NEWS",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    delayMillis = 0,
                    initialDelayMillis = 0,
                    velocity = 50.dp
                )
            )
        }
    }
}

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    bloodViewModel: BloodRequestViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    reminderViewModel: ReminderViewModel = hiltViewModel(),
    configViewModel: com.lifeplus.healthcare.presentation.viewmodel.ConfigViewModel = hiltViewModel(),
    hospitalViewModel: com.lifeplus.healthcare.presentation.viewmodel.HospitalViewModel = hiltViewModel(),
    clinicViewModel: com.lifeplus.healthcare.presentation.viewmodel.ClinicViewModel = hiltViewModel(),
    pharmacyViewModel: com.lifeplus.healthcare.presentation.viewmodel.PharmacyViewModel = hiltViewModel(),
    ambulanceViewModel: com.lifeplus.healthcare.presentation.viewmodel.AmbulanceViewModel = hiltViewModel(),
    donorViewModel: com.lifeplus.healthcare.presentation.viewmodel.DonorViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userFullName by authViewModel.fullName.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val requestPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            hospitalViewModel.detectNearbyAndLoad()
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    val userRole by authViewModel.role.collectAsState()
    val isAuthorized = userRole?.uppercase() == "ADMIN" || userRole?.uppercase() == "OWNER"
    val bloodRequests by bloodViewModel.dashboardRequests.collectAsState()
    val stats by dashboardViewModel.stats.collectAsState()
    val slides by dashboardViewModel.slides.collectAsState()
    val appSettings by configViewModel.settings.collectAsState()
    val appointmentState by appointmentViewModel.state.collectAsState()
    val reminderState by reminderViewModel.state.collectAsState()

    val upcomingAppointments = remember(appointmentState) {
        appointmentState.data.filter { it.status == "PENDING" || it.status == "CONFIRMED" }
    }
    
    val activeReminders = remember(reminderState) {
        reminderState.data.filter { it.active }.take(3)
    }

    // Services for the current user
    val hospitalState by hospitalViewModel.state.collectAsState()
    val clinicState by clinicViewModel.state.collectAsState()
    val pharmacyState by pharmacyViewModel.state.collectAsState()
    val ambulanceState by ambulanceViewModel.state.collectAsState()
    val donorProfile by donorViewModel.currentDonor.collectAsState()

    LaunchedEffect(Unit) {
        bloodViewModel.loadDashboard()
        dashboardViewModel.loadStats()
        dashboardViewModel.loadSlides()
        if (isLoggedIn) {
            appointmentViewModel.load()
            reminderViewModel.load()
            hospitalViewModel.loadMy()
            clinicViewModel.loadMy()
            pharmacyViewModel.loadMy()
            ambulanceViewModel.loadMy()
            donorViewModel.loadMyProfile()
        }
    }

    val baseFeatures = listOf(
        FeatureItem("Hospitals", Icons.Default.LocalHospital, Primary, "browse_hospitals"),
        FeatureItem("Doctors", Icons.Default.Person, Color(0xFF6366F1), "browse_doctors"),
        FeatureItem("Ambulance", Icons.Default.DirectionsCar, WarningColor, "browse_ambulances"),
        FeatureItem("Pharmacies", Icons.Default.LocalPharmacy, Color(0xFFEC4899), "browse_pharmacies"),
        FeatureItem("Clinics", Icons.Default.MedicalServices, Color(0xFF10B981), "browse_clinics"),
        FeatureItem("Diagnostics", Icons.Default.Science, Color(0xFF8B5CF6), "browse_diagnostics"),
        FeatureItem("Blood Banks", Icons.Default.Bloodtype, Color(0xFFEF4444), "browse_blood_banks"),
        FeatureItem("Telemedicine", Icons.Default.VideoCall, Color(0xFF3B82F6), "telemedicine"),
        FeatureItem("Request Blood", Icons.Default.AddBox, Color(0xFFF43F5E), "blood_request"),
        FeatureItem("Blood Requests", Icons.Default.WaterDrop, Color(0xFFEF4444), "donation_requests"),
        FeatureItem("Donor List", Icons.Default.Favorite, Color(0xFFD946EF), "browse_donors")
    )
    
    val marqueeText = remember(bloodRequests) {
        if (bloodRequests.isEmpty()) {
            "Stay healthy, stay safe with LifePlus Healthcare Services. Donate blood and save lives today!"
        } else {
            val latest = bloodRequests.first()
            "URGENT: ${latest.bloodGroup} needed at ${latest.hospitalName}. Please contact ${latest.contactPhone}. • " + 
            "LifePlus is your companion for all healthcare needs. Find doctors, hospitals, and ambulances instantly."
        }
    }

    // Filter based on backend settings (defaults to true if not set)
    val features = baseFeatures.filter { feature ->
        val key = "feature_" + feature.title.lowercase().replace(" ", "_")
        configViewModel.isFeatureEnabled(key, true)
    }

    val isRefreshing = remember { mutableStateOf(false) }
    
    // Auto-reset refreshing state when data changes
    LaunchedEffect(bloodRequests, stats, slides) {
        isRefreshing.value = false
    }

    AppBackground {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing.value),
            onRefresh = {
                isRefreshing.value = true
                bloodViewModel.loadDashboard()
                dashboardViewModel.loadStats()
                dashboardViewModel.loadSlides()
                appointmentViewModel.load()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            // --- Premium Header ---
            HeaderSection(
                isLoggedIn = isLoggedIn,
                userName = userFullName ?: "User",
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { onNavigate("notifications") },
                onProfileClick = { onNavigate("profile_tab") }
            )

            // --- Search Bar Section ---
            Surface(
                onClick = { onNavigate("search_tab") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Surface2Light)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search doctors, hospitals, medicines...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHint
                    )
                }
            }

            // --- Dynamic Marquee ---
            MarqueeSection(text = marqueeText)

            Spacer(modifier = Modifier.height(12.dp))

            // --- Dynamic Slideshow ---
            if (slides.isEmpty() && stats.doctorCount == "...") {
                ShimmerItem(modifier = Modifier.padding(horizontal = 24.dp), height = 180.dp, shape = RoundedCornerShape(28.dp))
            } else if (slides.isNotEmpty()) {
                DynamicSlideshow(slides = slides, onNavigate = onNavigate)
            } else {
                PromoBanner(
                    title = if (isLoggedIn) "Hello, ${userFullName?.split(" ")?.firstOrNull() ?: "User"}!" else "Quality Healthcare",
                    subtitle = "Find the best medical services nearby",
                    onClick = { onNavigate("search_tab") },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // --- Upcoming Appointments (Logged In Only) ---
            if (isLoggedIn && upcomingAppointments.isNotEmpty()) {
                UpcomingAppointmentsSection(
                    appointments = upcomingAppointments,
                    onViewAll = { onNavigate("appointments") }
                )
            }

            // --- Medicine Reminders (Logged In Only) ---
            if (isLoggedIn && activeReminders.isNotEmpty()) {
                MedicineReminderSection(
                    reminders = activeReminders,
                    onViewAll = { onNavigate("reminders") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Quick Access Tools ---
            QuickToolsSection(onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Stats Section ---
            Text(
                text = "Platform Stats",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (stats.doctorCount == "...") {
                Row(modifier = Modifier.padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ShimmerItem(modifier = Modifier.weight(1f), height = 110.dp)
                    ShimmerItem(modifier = Modifier.weight(1f), height = 110.dp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { StatCard("Doctors", stats.doctorCount, Icons.Default.Person, Color(0xFF6366F1), onClick = { onNavigate("browse_doctors") }) }
                    item { StatCard("Donors", stats.donorCount, Icons.Default.Favorite, Color(0xFFEC4899), onClick = { onNavigate("browse_donors") }) }
                    item { StatCard("Hospitals", stats.hospitalCount, Icons.Default.LocalHospital, Primary, onClick = { onNavigate("browse_hospitals") }) }
                    item { StatCard("Requests", stats.activeRequestsCount, Icons.Default.WaterDrop, ErrorColor, onClick = { onNavigate("donation_requests") }) }
                }
            }

            // --- Management Section (Owner/Admin Only) ---
            if (isLoggedIn && isAuthorized) {
                Spacer(modifier = Modifier.height(32.dp))
                ManagementSection(
                    onNavigate = onNavigate,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            // --- Emergency Section ---
            EmergencySection(
                onClick = { onNavigate("emergency") },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
            )

            // --- Blood Donor Distribution Graph ---
            if (stats.bloodGroupStats.isNotEmpty()) {
                BloodGroupGraphSection(stats.bloodGroupStats)
            }

            // --- Active Blood Requests ---
            if (bloodRequests.isNotEmpty()) {
                ActiveBloodRequestsSection(requests = bloodRequests, onViewAll = { onNavigate("donation_requests") }, onNavigate = onNavigate)
            }

            Spacer(modifier = Modifier.height(24.dp))
            DynamicBannerAd(modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(36.dp))
            RewardedSupportAction(
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))

            // --- Services Grid ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health Services",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                TextButton(onClick = { onNavigate("search_tab") }) {
                    Text("Explore All", color = Primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(modifier = Modifier.padding(horizontal = 24.dp)) {
                val screenWidth = maxWidth
                val itemWidth = (screenWidth - 36.dp) / 4 
                
                Column {
                    features.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { feature ->
                                val isVerified = when(feature.title) {
                                    "Hospitals" -> hospitalState.data.any { it.status?.uppercase() == "APPROVED" }
                                    "Clinics" -> clinicState.data.any { it.status?.uppercase() == "APPROVED" }
                                    "Pharmacies" -> pharmacyState.data.any { it.status?.uppercase() == "APPROVED" }
                                    "Ambulance" -> ambulanceState.data.any { it.status?.uppercase() == "APPROVED" }
                                    "Donor List" -> donorProfile?.status?.uppercase() == "APPROVED"
                                    else -> false
                                }
                                FeatureCard(feature, onNavigate, width = itemWidth, isVerified = isVerified)
                            }
                            repeat(4 - rowItems.size) {
                                Spacer(modifier = Modifier.width(itemWidth))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
}

@Composable
fun MedicineReminderSection(reminders: List<MedicineReminder>, onViewAll: () -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Medicine Reminders", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onViewAll) {
                Text("Manage", color = SuccessColor, fontWeight = FontWeight.Bold)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(reminders) { reminder ->
                Surface(
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = SuccessColor.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessColor.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!reminder.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = reminder.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Medication, null, tint = SuccessColor, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(reminder.medicineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(reminder.nextTime, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicSlideshow(slides: List<DashboardSlide>, onNavigate: (String) -> Unit) {
    if (slides.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { slides.size })

    LaunchedEffect(slides.size) {
        while (true) {
            delay(5000)
            if (slides.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % slides.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(28.dp))
        ) { page ->
            val slide = slides.getOrNull(page) ?: return@HorizontalPager
            Surface(
                        onClick = {
                            val action = slide.actionUrl?.trim().orEmpty()
                            if (action.isNotBlank() && !action.startsWith("http", ignoreCase = true)) onNavigate(action)
                        },
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = slide.imageUrl,
                        contentDescription = slide.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    
                    // Gradient Overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                    startY = 100f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = slide.title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = slide.subtitle ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        if (slides.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(slides.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Primary else Primary.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 12.dp else 6.dp)
                            .height(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BloodGroupGraphSection(stats: Map<String, Int>) {
    val maxVal = remember(stats) { stats.values.maxOrNull() ?: 1 }.toFloat()
    val sortedGroups = remember(stats) { stats.keys.sorted() }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text(
            "Donor Distribution",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .height(200.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                sortedGroups.forEach { group ->
                    val count = stats[group] ?: 0
                    val barHeight = 160.dp * (count / maxVal)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Primary, Primary.copy(alpha = 0.5f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            group.replace("_POS", "+").replace("_NEG", "-"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickToolsSection(onNavigate: (String) -> Unit) {
    val tools = listOf(
        ToolItem("BMI Calc", Icons.Default.MonitorWeight, Color(0xFF4CAF50), "bmi_calc"),
        ToolItem("First Aid", Icons.Default.MedicalInformation, Color(0xFFF44336), "first_aid"),
        ToolItem("Health Tips", Icons.Default.Lightbulb, Color(0xFFFF9800), "health_tips"),
        ToolItem("Vaccination", Icons.Default.Vaccines, Color(0xFF2196F3), "vaccination")
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            "Health Tools",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tools.forEach { tool ->
                Surface(
                    onClick = { onNavigate(tool.route) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = tool.color.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tool.color.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(tool.icon, null, tint = tool.color, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            tool.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = tool.color,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

data class ToolItem(val title: String, val icon: ImageVector, val color: Color, val route: String)

@Composable
fun HeaderSection(
    isLoggedIn: Boolean,
    userName: String,
    onOpenDrawer: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onOpenDrawer,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Welcome to LifePlus",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (isLoggedIn) userName else "Guest User",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onNotificationClick,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = PrimaryLight,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun PromoBanner(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize().background(PremiumBlueGradient)) {
            // Background Decoration
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(200.dp)
                    .offset(x = 40.dp)
                    .alpha(0.15f),
                tint = Color.White
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PREMIUM CARE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle background glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(color.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementSection(onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    var showAddSheet by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = "Your Services",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                title = "Manage",
                icon = Icons.Default.EditAttributes,
                color = Primary,
                onClick = { onNavigate("manage_listings") },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Add New",
                icon = Icons.Default.AddBusiness,
                color = SuccessColor,
                onClick = { showAddSheet = true },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text(
                    "What would you like to add?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                val types = listOf(
                    Triple("Hospital", Icons.Default.LocalHospital, "hospital"),
                    Triple("Clinic", Icons.Default.MedicalServices, "clinic"),
                    Triple("Pharmacy", Icons.Default.LocalPharmacy, "pharmacy"),
                    Triple("Ambulance", Icons.Default.DirectionsCar, "ambulance"),
                    Triple("Blood Bank", Icons.Default.Bloodtype, "blood_bank"),
                    Triple("Diagnostic Center", Icons.Default.Science, "diagnostic"),
                    Triple("Blood Organization", Icons.Default.VolunteerActivism, "blood_org")
                )
                
                types.forEach { (label, icon, type) ->
                    Surface(
                        onClick = { 
                            showAddSheet = false
                            onNavigate("add_entity/$type") 
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = BackgroundLight
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(label, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmergencySection(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Accent),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.background(EmergencyGradient).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Emergency Help", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Instant medical assistance & ambulance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ActiveBloodRequestsSection(requests: List<BloodRequest>, onViewAll: () -> Unit, onNavigate: (String) -> Unit) {
    val urgentRequests = remember(requests) { requests.filter { it.status == "OPEN" } }
    if (urgentRequests.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { urgentRequests.size })

    LaunchedEffect(urgentRequests) {
        while (true) {
            delay(4000)
            if (urgentRequests.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % urgentRequests.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ErrorColor,
                    shape = CircleShape,
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Urgent Blood Needs", 
                    style = MaterialTheme.typography.titleLarge, 
                    color = TextPrimary, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
            TextButton(onClick = onViewAll) {
                Text("See All", color = Primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp
        ) { page ->
            val request = urgentRequests.getOrNull(page) ?: return@HorizontalPager
            BloodRequestCard(
                request = request, 
                modifier = Modifier.fillMaxWidth(), 
                onNavigate = onNavigate
            )
        }
        
        // Pager Indicators
        if (urgentRequests.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(urgentRequests.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) Primary else Primary.copy(alpha = 0.2f)
                    val width = if (isSelected) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .width(width)
                            .height(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BloodRequestCard(request: BloodRequest, modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    val timeAgo = remember(request.createdAt) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(request.createdAt ?: "")
            val now = Date()
            val diff = now.time - (date?.time ?: now.time)
            val minutes = diff / (60 * 1000)
            val hours = minutes / 60
            val days = hours / 24
            
            when {
                days > 0 -> "$days days ago"
                hours > 0 -> "$hours hours ago"
                minutes > 0 -> "$minutes mins ago"
                else -> "Just now"
            }
        } catch (e: Exception) { "Recent" }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (request.urgency == "URGENT") ErrorColor.copy(alpha = 0.1f) else AccentLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            request.bloodGroup ?: "?", 
                            style = MaterialTheme.typography.headlineSmall, 
                            color = if (request.urgency == "URGENT") ErrorColor else Accent, 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(request.patientName ?: "Unknown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        if (request.urgency == "URGENT") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = ErrorColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "URGENT",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = TextHint, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeAgo, style = MaterialTheme.typography.bodySmall, color = TextHint)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Surface2Light, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(request.hospitalName ?: "Hospital", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(request.district ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
            onNavigate(
                "details?title=${android.net.Uri.encode("${request.bloodGroup} Needed")}" +
                    "&subtitle=${android.net.Uri.encode(request.patientName.orEmpty())}" +
                    "&type=blood_request&phone=${android.net.Uri.encode(request.contactPhone.orEmpty())}" +
                    "&address=${android.net.Uri.encode(request.hospitalName.orEmpty())}&entityId=${request.id}"
            )
                    }
                ) {
                    Text(
                        text = "Donate Now",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(feature: FeatureItem, onNavigate: (String) -> Unit, width: androidx.compose.ui.unit.Dp, isVerified: Boolean = false) {
    Column(
        modifier = Modifier
            .width(width)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onNavigate(feature.route) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(18.dp),
                color = feature.color.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, feature.color.copy(alpha = 0.12f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = feature.icon, 
                        contentDescription = feature.title, 
                        modifier = Modifier.size(28.dp), 
                        tint = feature.color
                    )
                }
            }
            if (isVerified) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(Color.White, CircleShape)
                        .padding(1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = feature.title,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.2.sp
        )
    }
}
