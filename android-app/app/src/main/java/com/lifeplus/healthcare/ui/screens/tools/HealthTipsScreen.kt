package com.lifeplus.healthcare.ui.screens.tools

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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.theme.*

data class HealthTip(
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector = Icons.Default.Lightbulb
)

val healthTips = listOf(
    HealthTip("Stay Hydrated", "Drink at least 8-10 glasses of water daily, especially in the humid weather.", "Habits", Icons.Default.WaterDrop),
    HealthTip("Seasonal Safety", "During monsoon, use mosquito nets and keep surroundings clean to prevent Dengue.", "Safety", Icons.Default.Shield),
    HealthTip("Balanced Diet", "Include local seasonal fruits like Guava and Mango for natural vitamins.", "Nutrition", Icons.Default.Restaurant),
    HealthTip("Physical Activity", "A 30-minute brisk walk can significantly reduce heart disease risks.", "Exercise", Icons.Default.DirectionsRun),
    HealthTip("Quality Sleep", "Ensure 7-8 hours of quality sleep to boost your immune system.", "Rest", Icons.Default.Bedtime),
    HealthTip("Hand Hygiene", "Regularly wash your hands with soap to prevent infections.", "Hygiene", Icons.Default.CleanHands)
)

@Composable
fun HealthTipsScreen(
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Habits", "Safety", "Nutrition", "Exercise", "Rest", "Hygiene")
    var selectedCategory by remember { mutableStateOf("All") }
    
    val filteredTips = healthTips.filter { 
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (it.title.contains(searchQuery, ignoreCase = true) || 
         it.description.contains(searchQuery, ignoreCase = true))
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Health Tips",
                subtitle = "Expert advice for a better life",
                onBackClick = onNavigateBack
            )

            // Search and Category Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .height(56.dp),
                        placeholder = { Text("Search advice...", color = TextHint) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null, tint = TextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Surface2Light,
                            focusedContainerColor = Surface2Light,
                            unfocusedContainerColor = Surface2Light
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White,
                                    labelColor = TextSecondary,
                                    containerColor = Surface2Light
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (filteredTips.isEmpty()) {
                    EmptyTipsState(searchQuery)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(filteredTips) { tip ->
                            PremiumTipCard(tip)
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumTipCard(tip: HealthTip) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryLight.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tip.icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SecondaryLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tip.category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.BookmarkBorder, null, tint = TextHint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun EmptyTipsState(query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = TextHint)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No results for \"$query\"", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Try another category or keyword", color = TextSecondary)
        }
    }
}
