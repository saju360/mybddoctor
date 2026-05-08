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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.theme.*

data class Vaccine(
    val nameEn: String,
    val nameBn: String,
    val dose: String,
    val color: Color
)

data class AgeGroup(
    val age: String,
    val vaccines: List<Vaccine>
)

val vaccinationSchedule = listOf(
    AgeGroup("At Birth", listOf(
        Vaccine("BCG", "বিসিজি", "Single Dose", Color(0xFF3B82F6)),
        Vaccine("OPV 0", "ওপিভি ০", "Initial Dose", Color(0xFF10B981))
    )),
    AgeGroup("6 Weeks", listOf(
        Vaccine("Pentavalent 1", "পেন্টাভ্যালেন্ট ১", "Dose 1", Color(0xFFEF4444)),
        Vaccine("OPV 1", "ওপিভি ১", "Dose 1", Color(0xFF10B981)),
        Vaccine("PCV 1", "পিসিভি ১", "Dose 1", Color(0xFF8B5CF6))
    )),
    AgeGroup("10 Weeks", listOf(
        Vaccine("Pentavalent 2", "পেন্টাভ্যালেন্ট ২", "Dose 2", Color(0xFFEF4444)),
        Vaccine("OPV 2", "ওপিভি ২", "Dose 2", Color(0xFF10B981)),
        Vaccine("PCV 2", "পিসিভি ২", "Dose 2", Color(0xFF8B5CF6))
    )),
    AgeGroup("14 Weeks", listOf(
        Vaccine("Pentavalent 3", "পেন্টাভ্যালেন্ট ৩", "Dose 3", Color(0xFFEF4444)),
        Vaccine("OPV 3", "ওপিভি ৩", "Dose 3", Color(0xFF10B981)),
        Vaccine("IPV", "আইপিভি", "Injectable", Color(0xFFF59E0B))
    )),
    AgeGroup("9 Months", listOf(
        Vaccine("MR 1", "এমআর ১", "Measles Rubella", Color(0xFFEC4899))
    ))
)

@Composable
fun VaccinationScreen(
    onNavigateBack: () -> Unit
) {
    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Vaccination",
                subtitle = "National EPI Schedule",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    // Schedule Info Header
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = Secondary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Shield, null, tint = Secondary, modifier = Modifier.size(30.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Immunization", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                Text("Protecting future generations", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                items(vaccinationSchedule) { group ->
                    PremiumAgeGroupSection(group)
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun PremiumAgeGroupSection(group: AgeGroup) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
        ) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = Primary,
                border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryLight)
            ) {}
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = group.age,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
        
        Column(
            modifier = Modifier.padding(start = 13.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            group.vaccines.forEach { vaccine ->
                PremiumVaccineCard(vaccine)
            }
        }
    }
}

@Composable
fun PremiumVaccineCard(vaccine: Vaccine) {
    Surface(
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
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = vaccine.color.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = null,
                        tint = vaccine.color,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vaccine.nameEn,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
                Text(
                    text = vaccine.nameBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = vaccine.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = vaccine.dose,
                        style = MaterialTheme.typography.labelSmall,
                        color = vaccine.color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Surface2Light
            ) {
                Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ChevronRight, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
