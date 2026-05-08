package com.lifeplus.healthcare.ui.screens.tools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

data class FirstAidTopic(
    val id: String,
    val title: String,
    val titleBn: String,
    val icon: ImageVector,
    val color: Color,
    val steps: List<String>
)

val firstAidTopics = listOf(
    FirstAidTopic("1", "CPR", "সিপিআর", Icons.Default.Favorite, ErrorColor, listOf("Check for response / সাড়া পরীক্ষা করুন", "Open airway / শ্বাসনালী খুলে দিন", "Give 30 compressions / ৩০ বার বুক চাপুন", "Give 2 breaths / ২ বার মুখ দিয়ে বাতাস দিন")),
    FirstAidTopic("2", "Burns", "পোড়া", Icons.Default.FireHydrantAlt, WarningColor, listOf("Cool the burn with water / পানি দিয়ে পোড়া জায়গা ঠান্ডা করুন", "Remove tight clothing / টাইট কাপড় খুলে ফেলুন", "Cover with clean wrap / পরিষ্কার কাপড় দিয়ে ঢেকে দিন")),
    FirstAidTopic("3", "Bleeding", "রক্তপাত", Icons.Default.Bloodtype, Color(0xFFC62828), listOf("Apply direct pressure / সরাসরি চাপ প্রয়োগ করুন", "Clean the wound / ক্ষত পরিষ্কার করুন", "Apply firm bandage / শক্ত ব্যান্ডেজ ব্যবহার করুন")),
    FirstAidTopic("4", "High Fever", "তীব্র জ্বর", Icons.Default.Thermostat, InfoColor, listOf("Sponge with cool water / জলপট্টি দিন", "Drink plenty of fluids / প্রচুর পানি পান করুন", "Take paracetamol / প্যারাসিটামল সেবন করুন")),
    FirstAidTopic("5", "Choking", "শ্বাসরোধ", Icons.Default.Air, Color(0xFF795548), listOf("5 back blows / পিঠে ৫ বার থাপ্পড় দিন", "5 abdominal thrusts / পেটে ৫ বার চাপ দিন", "Repeat until cleared / ক্লিয়ার না হওয়া পর্যন্ত পুনরাবৃত্তি করুন"))
)

@Composable
fun FirstAidScreen(
    onNavigateBack: () -> Unit
) {
    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "First Aid Guide",
                subtitle = "Life-saving emergency steps",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Modern Info Header
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Accent),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White
                    ) {
                        Box(modifier = Modifier.background(AccentLight.copy(alpha = 0.6f))) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = Accent.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Column {
                                    Text(
                                        text = "Emergency Guide",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Immediate actions save lives",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Common Emergencies",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(firstAidTopics) { topic ->
                    FirstAidCard(topic)
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun FirstAidCard(topic: FirstAidTopic) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (expanded) 8.dp else 2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = topic.color.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = topic.icon,
                            contentDescription = null,
                            tint = topic.color,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = topic.titleBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Surface2Light)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .background(Surface2Light.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    topic.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = topic.color
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 2.dp),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
