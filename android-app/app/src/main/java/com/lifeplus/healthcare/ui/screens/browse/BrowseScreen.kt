package com.lifeplus.healthcare.ui.screens.browse

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.components.Status
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.AdvancedFilterSheet
import com.lifeplus.healthcare.ui.theme.*

data class BrowseItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val avatarText: String,
    val status: Status = Status.Available,
    val tag: String? = null,
    val phone: String? = null,          // ← real phone number for call intent
    val hasCall: Boolean = true,
    val hasBook: Boolean = false,
    val hasTelemedicine: Boolean = false,
    val donorCount: Int? = null
)

@Composable
fun BrowseScreen(
    title: String,
    items: List<BrowseItem>,
    isLoading: Boolean = false,
    error: String? = null,
    filters: List<String> = emptyList(),
    onFilterSelected: (String) -> Unit = {},
    onItemClick: ((BrowseItem) -> Unit)? = null,
    onBookClick: ((BrowseItem) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onAdvancedFilter: ((Map<String, String>) -> Unit)? = null,
    nearbyDistrict: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(filters.firstOrNull() ?: "") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showCallConfirm by remember { mutableStateOf<BrowseItem?>(null) }

    if (showCallConfirm != null) {
        AlertDialog(
            onDismissRequest = { showCallConfirm = null },
            title = { Text("Confirm Call") },
            text = { Text("Do you want to call ${showCallConfirm?.title} at ${showCallConfirm?.phone}?") },
            confirmButton = {
                TextButton(onClick = {
                    val phoneNum = showCallConfirm?.phone ?: "999"
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
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

    val filteredItems = remember(items, searchQuery, selectedFilter) {
        items.filter { item ->
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                               item.subtitle.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = when {
                selectedFilter == "All" || selectedFilter == "" || selectedFilter == "সবগুলো" -> true
                selectedFilter == "ICU Available" || selectedFilter == "আইসিইউ" -> item.tag == "ICU"
                selectedFilter == "24/7 Open" -> item.status == Status.Available && item.tag?.contains("24/7") == true
                else -> item.tag?.contains(selectedFilter, ignoreCase = true) == true ||
                        item.subtitle.contains(selectedFilter, ignoreCase = true)
            }

            matchesSearch && matchesFilter
        }
    }
    val prioritizedItems = remember(filteredItems, nearbyDistrict) {
        fun sortKey(item: BrowseItem): Long = item.id.toLongOrNull() ?: Long.MAX_VALUE
        val district = nearbyDistrict?.trim().orEmpty()
        if (district.isBlank()) {
            filteredItems.sortedByDescending { sortKey(it) }
        } else {
            val (locationMatched, others) = filteredItems.partition { item ->
                item.subtitle.contains(district, ignoreCase = true)
            }
            locationMatched.sortedBy { sortKey(it) } +
                others.sortedByDescending { sortKey(it) }
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = title,
                subtitle = when(title) {
                    "Hospitals" -> "Find the best hospitals nearby"
                    "Doctors" -> "Consult with top specialists"
                    "Ambulances" -> "Fast emergency ambulance service"
                    "Blood Banks" -> "Check blood availability"
                    "Available Donors" -> "Connect with blood donors"
                    "Pharmacies" -> "Find medicines near you"
                    "Clinics" -> "Private clinic services"
                    "Diagnostic Centers" -> "Lab tests and imaging"
                    else -> "Explore $title services"
                },
                onBackClick = onNavigateBack
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Search and Filter Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            placeholder = { Text("Search by name or location...", color = TextHint) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                        }
                                    }
                                    IconButton(onClick = { showFilterSheet = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.FilterList,
                                            contentDescription = "Filter",
                                            tint = Primary
                                        )
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

                        if (filters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filters) { filter ->
                                    val isSelected = selectedFilter == filter
                                    Surface(
                                        onClick = {
                                            selectedFilter = filter
                                            onFilterSelected(filter)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Primary else Surface2Light,
                                    ) {
                                        Text(
                                            text = filter,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        if (nearbyDistrict != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = SuccessColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Detected location: $nearbyDistrict (Prioritized first)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                        }
                    } else if (error != null) {
                        ErrorState(error = error, onRetry = onRetry)
                    } else if (prioritizedItems.isEmpty()) {
                        EmptyState(searchQuery)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "${prioritizedItems.size} result${if (prioritizedItems.size != 1) "s" else ""} found",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                            items(prioritizedItems) { item ->
                                BrowseCard(
                                    item = item,
                                    onClick = { onItemClick?.invoke(item) },
                                    onBookClick = { onBookClick?.invoke(item) },
                                    onCallClick = { showCallConfirm = item }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            AdvancedFilterSheet(
                title = title,
                initialDistrict = nearbyDistrict,
                onDismiss = { showFilterSheet = false },
                onApply = { filters ->
                    showFilterSheet = false
                    onAdvancedFilter?.invoke(filters)
                }
            )
        }
    }
}

@Composable
fun BrowseCard(
    item: BrowseItem,
    onClick: () -> Unit,
    onBookClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Avatar Section
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(18.dp),
                color = Primary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.avatarText.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        fontSize = 16.sp
                    )
                    if (item.tag != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = SuccessColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    fontSize = 13.sp
                )

                if (item.donorCount != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        com.lifeplus.healthcare.ui.components.AnimatedCounter(
                            targetValue = item.donorCount,
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessColor
                        )
                        Text(
                            text = " Donors",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (item.status) {
                                    Status.Available -> SuccessColor
                                    Status.Busy -> WarningColor
                                    else -> TextHint
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (item.status) {
                            Status.Available -> "Available"
                            Status.Busy -> "Busy"
                            else -> "Unavailable"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (item.status) {
                            Status.Available -> SuccessColor
                            Status.Busy -> WarningColor
                            else -> TextHint
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.hasCall) {
                    Surface(
                        onClick = onCallClick,
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = PrimaryLight.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (item.hasBook) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onBookClick,
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = SuccessColor.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Book", tint = SuccessColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Surface2Light
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                if (query.isNotBlank()) "No results for \"$query\"" else "Nothing to show",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Try searching for something else or clear the filter",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ErrorState(error: String, onRetry: (() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = ErrorColor.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorColor.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
