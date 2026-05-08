package com.lifeplus.healthcare.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.ui.theme.*
import com.lifeplus.healthcare.ui.components.AppBackground

import com.lifeplus.healthcare.presentation.viewmodel.DonorViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AppointmentViewModel
import com.lifeplus.healthcare.presentation.viewmodel.ReminderViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage

import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.components.DropdownField

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    donorViewModel: DonorViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    reminderViewModel: ReminderViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val fullName by authViewModel.fullName.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userRole by authViewModel.role.collectAsState()
    val isAuthorized = userRole?.uppercase() == "ADMIN" || userRole?.uppercase() == "OWNER"
    val phone by authViewModel.phone.collectAsState()
    val rewards by donorViewModel.rewards.collectAsState()
    val donorState by donorViewModel.state.collectAsState()
    val eligibility by donorViewModel.eligibility.collectAsState()
    val savedProfileImageUri by authViewModel.profileImageUri.collectAsState()
    
    val appointmentState by appointmentViewModel.state.collectAsState()
    val reminderState by reminderViewModel.state.collectAsState()
    
    // Use saved URI from DataStore; update when user picks a new image
    var profileImageUri by remember(savedProfileImageUri) {
        mutableStateOf(savedProfileImageUri?.let { android.net.Uri.parse(it) })
    }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            profileImageUri = uri
            authViewModel.saveProfileImage(uri.toString())
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            donorViewModel.loadRewards()
            donorViewModel.loadMy()
            appointmentViewModel.load()
            reminderViewModel.load()
        }
    }

    LaunchedEffect(donorState.data) {
        if (donorState.data.isNotEmpty()) {
            donorViewModel.checkEligibility(donorState.data[0].id)
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Premium Header with Glassmorphism effect
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(onClick = { onNavigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Avatar with Premium Ring
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .clickable { launcher.launch("image/*") },
                            shape = CircleShape,
                            color = PrimaryLight,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Primary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (profileImageUri != null) {
                                    AsyncImage(
                                        model = profileImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = Primary
                                    )
                                }
                            }
                        }
                        
                        // Edit Icon Button
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .offset(x = (-4).dp, y = (-4).dp),
                            shape = CircleShape,
                            color = Primary,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isLoggedIn) fullName ?: "User Name" else "Guest User",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.clickable(enabled = isLoggedIn) { showEditProfileDialog = true }
                    )
                    Text(
                        text = if (isLoggedIn) phone ?: "" else "Sign in for more features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Dynamic Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem("Appointments", if (isLoggedIn) appointmentState.data.size.toString() else "0") { onNavigate("appointments") }
                        ProfileStatDivider()
                        ProfileStatItem("Donations", if (isLoggedIn) (rewards["donationCount"]?.toString() ?: "0") else "0") { onNavigate("donor_register") }
                        ProfileStatDivider()
                        ProfileStatItem("Reminders", if (isLoggedIn) reminderState.data.size.toString() else "0") { onNavigate("reminders") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Rewards Card
            if (isLoggedIn) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(modifier = Modifier.background(PrimaryLight.copy(alpha = 0.35f))) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Primary.copy(alpha = 0.1f)
                                    ) {
                                        Icon(Icons.Default.Stars, contentDescription = null, tint = Primary, modifier = Modifier.padding(10.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = "Donor Rank", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text(text = rewards["rank"]?.toString() ?: "Bronze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Total Points", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(text = rewards["points"]?.toString() ?: "0", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                }
                            }
                            
                            if (eligibility["eligible"] == false) {
                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Primary.copy(alpha = 0.1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = WarningColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Next donation in ${eligibility["daysRemaining"]} days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Menu Options
            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileMenuItem(Icons.Outlined.CalendarToday, "My Appointments", "Manage your bookings") { onNavigate("appointments") }
                ProfileMenuItem(Icons.Outlined.Folder, "Health Records", "Reports and prescriptions") { onNavigate("health_records") }
                ProfileMenuItem(Icons.Outlined.Notifications, "Medicine Reminders", "Don't miss a dose") { onNavigate("reminders") }
                
                if (isAuthorized) {
                    ProfileMenuItem(Icons.Outlined.EditAttributes, "Manage My Services", "Admin/Owner listing management") { onNavigate("manage_listings") }
                }

                ProfileMenuItem(Icons.Outlined.FavoriteBorder, "Donor Profile", "Blood donor information") { onNavigate("donor_register") }
                
                if (donorState.data.isNotEmpty()) {
                    ProfileMenuItem(Icons.Outlined.Feedback, "Manage Requests", "Direct donation requests for you") { onNavigate("donation_requests") }
                }
                
                ProfileMenuItem(Icons.Outlined.Business, "Blood Organization", "View active organizations") { onNavigate("browse_blood_orgs") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Logout Button
            TextButton(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorColor.copy(alpha = 0.08f)),
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Logout from Account", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (showEditProfileDialog && isLoggedIn) {
        var newName by remember { mutableStateOf(fullName ?: "") }
        var newDistrict by remember { mutableStateOf(authViewModel.district.value ?: "") }
        var newEmail by remember { mutableStateOf(authViewModel.email.value ?: "") }
        val bloodGroups = listOf("Select Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
        var selectedBloodGroup by remember { mutableStateOf(authViewModel.bloodGroup.value ?: bloodGroups[0]) }
        var expandBloodGroup by remember { mutableStateOf(false) }
        
        val authState by authViewModel.state.collectAsState()

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            title = { Text("Edit Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    InputField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = "Full Name",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary) }
                    )
                    
                    InputField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = "Email Address",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary) }
                    )

                    // District input replaced with a better InputField if no dropdown helper
                    InputField(
                        value = newDistrict,
                        onValueChange = { newDistrict = it },
                        label = "District",
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Primary) }
                    )

                    DropdownField(
                        label = "Blood Group",
                        selected = selectedBloodGroup,
                        expanded = expandBloodGroup,
                        onExpandedChange = { expandBloodGroup = it },
                        items = bloodGroups,
                        onItemSelected = { selectedBloodGroup = it }
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Save Changes",
                    onClick = {
                        authViewModel.updateProfile(newName, newEmail, newDistrict, selectedBloodGroup)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    isLoading = authState.isLoading
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
        
        LaunchedEffect(authState.isSuccess) {
            if (authState.isSuccess) {
                showEditProfileDialog = false
            }
        }
    }

}

@Composable
fun ProfileStatItem(label: String, value: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Primary,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ProfileStatDivider() {
    Box(
        modifier = Modifier
            .height(30.dp)
            .width(1.dp)
            .background(Surface2Light)
    )
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
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
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryLight.copy(alpha = 0.5f)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint, modifier = Modifier.size(20.dp))
        }
    }
}
