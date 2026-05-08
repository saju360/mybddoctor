package com.lifeplus.healthcare.ui.screens.browse

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.ReviewViewModel
import com.lifeplus.healthcare.presentation.viewmodel.ChatViewModel
import com.lifeplus.healthcare.data.model.Review
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import com.lifeplus.healthcare.presentation.viewmodel.BloodRequestViewModel
import com.lifeplus.healthcare.presentation.viewmodel.DonorViewModel
import java.util.*
import java.text.SimpleDateFormat

@Composable
fun DetailsScreen(
    title: String,
    subtitle: String,
    type: String,
    phone: String,
    address: String,
    entityId: Long = -1L,
    donorId: Long = -1L,
    physicalHistory: String = "",
    rewardPoints: Int = 0,
    consultationHours: String = "",
    onNavigateBack: () -> Unit,
    onBookClick: (() -> Unit)? = null,
    onChatClick: (Long, String) -> Unit = { _, _ -> },
    reviewViewModel: ReviewViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    bloodViewModel: BloodRequestViewModel = hiltViewModel(),
    donorViewModel: DonorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val reviewState by reviewViewModel.state.collectAsState()
    val reviewAction by reviewViewModel.action.collectAsState()
    val bloodAction by bloodViewModel.action.collectAsState()
    val donorState by donorViewModel.currentDonor.collectAsState()
    
    val targetId = if (type.lowercase() == "donor") donorId else entityId

    var showCallConfirm by remember { mutableStateOf<String?>(null) }

    if (showCallConfirm != null) {
        AlertDialog(
            onDismissRequest = { showCallConfirm = null },
            title = { Text("Confirm Call") },
            text = { Text("Do you want to call $title at ${showCallConfirm}?") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${showCallConfirm}"))
                    context.startActivity(intent)
                    showCallConfirm = null
                }) {
                    Text("Call", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallConfirm = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
    
    LaunchedEffect(targetId) {
        if (targetId != -1L) {
            reviewViewModel.load(type, targetId)
        }
        if (type.lowercase() == "blood_request") {
            // Load specific request if needed, but we have enough info from nav args for now
        }
    }

    // Countdown logic for donors
    val eligibilityStatus = remember(donorState) {
        donorState?.let { donor ->
            if (donor.lastDonationDate == null) "Eligible to donate"
            else {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                try {
                    val lastDate = sdf.parse(donor.lastDonationDate)
                    val cal = Calendar.getInstance()
                    cal.time = lastDate
                    cal.add(Calendar.MONTH, 3) // 3 months wait
                    
                    if (cal.time.after(Date())) {
                        val diff = cal.time.time - Date().time
                        val days = diff / (24 * 60 * 60 * 1000)
                        "Next donation in $days days"
                    } else "Eligible to donate"
                } catch (e: Exception) { "Eligible to donate" }
            }
        } ?: "Register as donor to help"
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Details",
                subtitle = "Information about $title",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        val shareText = when(type.lowercase()) {
                            "blood_request" -> "URGENT BLOOD NEEDED!\nGroup: $title\nPatient: $subtitle\nHospital: $address\nContact: $phone\nHelp save a life. Shared via LifePlus Healthcare."
                            "doctor" -> "Doctor Profile: $title\nSpecialty: $subtitle\nHospital: $address\nContact: $phone\nBook via LifePlus Healthcare."
                            else -> "Check out $title on LifePlus Healthcare.\nAddress: $address\nContact: $phone"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            this.type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Primary)
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Premium Hero Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = Primary),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(110.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = PrimaryLight.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when(type.lowercase()) {
                                        "hospital" -> Icons.Default.LocalHospital
                                        "doctor" -> Icons.Default.Person
                                        "pharmacy" -> Icons.Default.LocalPharmacy
                                        "blood_bank" -> Icons.Default.Bloodtype
                                        "blood_request" -> Icons.Default.WaterDrop
                                        "donor" -> Icons.Default.Favorite
                                        "ambulance" -> Icons.Default.DirectionsCar
                                        "clinic" -> Icons.Default.MedicalServices
                                        "diagnostic" -> Icons.Default.Science
                                        "blood_org" -> Icons.Default.VolunteerActivism
                                        else -> Icons.Default.HealthAndSafety
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 24.sp
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 16.sp
                        )
                        
                        if (type.lowercase() == "donor" || type.lowercase() == "blood_request") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = if (eligibilityStatus.contains("Eligible")) SuccessColor.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (eligibilityStatus.contains("Eligible")) Icons.Default.CheckCircle else Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (eligibilityStatus.contains("Eligible")) SuccessColor else ErrorColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = eligibilityStatus,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (eligibilityStatus.contains("Eligible")) SuccessColor else ErrorColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (type.lowercase() == "donor") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "$rewardPoints Reward Points",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (type.lowercase() != "blood_request") {
                            Spacer(modifier = Modifier.height(20.dp))
                            Surface(
                                color = SuccessColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Verified Provider",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SuccessColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (type.lowercase() == "donor" && physicalHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(4.dp, 20.dp).background(Primary, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Physical History",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = physicalHistory,
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (type.lowercase() == "doctor") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(4.dp, 20.dp).background(Primary, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Working Hours",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = Primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                consultationHours.ifBlank { "10:00 AM - 05:00 PM" },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Detail Section Title
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(4.dp, 20.dp).background(Primary, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Contact Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Address — clickable to open maps
                Surface(
                    onClick = {
                        if (address.isNotBlank()) {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to browser maps
                                val browserUri = Uri.parse("https://maps.google.com/?q=${Uri.encode(address)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface2Light
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, null, tint = Primary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Location Address", style = MaterialTheme.typography.labelSmall, color = TextHint, letterSpacing = 0.5.sp)
                            Text(
                                text = address.ifBlank { "Not specified" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Icon(Icons.Default.OpenInNew, null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                InfoDetailCard(Icons.Default.Phone, "Phone Number", phone.ifBlank { "Not available" })
                if (type.lowercase() != "doctor") {
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoDetailCard(Icons.Default.AccessTime, "Operating Hours", "Available 24/7 for you")
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Call Button — real phone intent
                    Surface(
                        onClick = {
                            showCallConfirm = phone.ifBlank { "999" }
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryLight,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Call Now", color = Primary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    if (onBookClick != null) {
                        PrimaryButton(
                            text = if (type == "doctor") "Book Appointment" else "Request Blood",
                            onClick = onBookClick,
                            modifier = Modifier.weight(1.5f).height(60.dp)
                        )
                    } else {
                        PrimaryButton(
                            text = "Directions",
                            onClick = {
                                val query = address.ifBlank { title }
                                val browserUri = Uri.parse("https://maps.google.com/?q=${Uri.encode(query)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                            },
                            modifier = Modifier.weight(1.5f).height(60.dp)
                        )
                    }
                    
                    if (type == "doctor" || type == "donor" || type == "blood_request") {
                        Surface(
                            onClick = {
                            if (type == "blood_request") {
                                showCallConfirm = phone.ifBlank { "999" }
                            } else if (targetId != -1L) {
                                    chatViewModel.startChat(targetId) { roomId ->
                                        onChatClick(roomId, title)
                                    }
                                }
                            },
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryLight,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(if (type == "blood_request") Icons.Default.Call else Icons.Default.ChatBubbleOutline, null, tint = Primary)
                            }
                        }
                    }
                }
                
                if (type == "blood_request") {
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = if (bloodAction.isLoading) "Processing..." else "I Have Donated",
                        onClick = {
                            if (eligibilityStatus.contains("Eligible")) {
                                bloodViewModel.completeDonation(entityId) {
                                    // Point logic moved to admin approval
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        containerColor = SuccessColor,
                        enabled = eligibilityStatus.contains("Eligible") && !bloodAction.isLoading
                    )
                    Text(
                        text = "Point will be added after admin verification.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (!eligibilityStatus.contains("Eligible")) {
                        Text(
                            text = "You are not eligible to donate yet.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                // ── Reviews Section ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(4.dp, 20.dp).background(Primary, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reviews & Ratings",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Write a Review Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        var rating by remember { mutableIntStateOf(5) }
                        var comment by remember { mutableStateOf("") }
                        
                        Text("Share your experience", style = MaterialTheme.typography.labelMedium, color = TextHint)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { i ->
                                IconButton(onClick = { rating = i }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = null,
                                        tint = if (i <= rating) Color(0xFFFFB800) else TextHint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Write a comment...", fontSize = 14.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Surface2Light
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PrimaryButton(
                            text = if (reviewAction.isLoading) "Submitting..." else "Post Review",
                            onClick = {
                                if (targetId != -1L && comment.isNotBlank()) {
                                    reviewViewModel.submit(
                                        Review(
                                            entityType = type,
                                            entityId = targetId,
                                            rating = rating,
                                            comment = comment
                                        )
                                    )
                                    comment = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            isLoading = reviewAction.isLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Review List
                if (reviewState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                } else if (reviewState.data.isEmpty()) {
                    Text("No reviews yet. Be the first to review!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                } else {
                    reviewState.data.forEach { review ->
                        ReviewItem(review)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Surface2Light.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = PrimaryLight) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(review.userName.take(1), color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(review.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row {
                        (1..5).forEach { i ->
                            Icon(
                                Icons.Default.Star, null,
                                tint = if (i <= review.rating) Color(0xFFFFB800) else TextHint.copy(alpha = 0.3f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review.comment, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}

@Composable
fun InfoDetailCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Surface2Light
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextHint, letterSpacing = 0.5.sp)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
