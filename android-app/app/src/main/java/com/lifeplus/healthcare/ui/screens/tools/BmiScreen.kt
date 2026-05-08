package com.lifeplus.healthcare.ui.screens.tools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.components.InputField
import com.lifeplus.healthcare.ui.components.PrimaryButton
import com.lifeplus.healthcare.ui.components.PremiumTopBar
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.theme.*
import java.util.Locale
import kotlin.math.pow

@Composable
fun BmiScreen(
    onNavigateBack: () -> Unit
) {
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("65") }
    var bmiResult by remember { mutableStateOf<Float?>(null) }
    var bmiCategory by remember { mutableStateOf("") }
    var categoryColor by remember { mutableStateOf(Primary) }

    fun calculateBmi() {
        val h = height.toFloatOrNull() ?: 0f
        val w = weight.toFloatOrNull() ?: 0f
        if (h > 0 && w > 0) {
            val bmi = w / (h / 100).pow(2)
            bmiResult = bmi
            when {
                bmi < 18.5 -> {
                    bmiCategory = "Underweight"
                    categoryColor = InfoColor
                }
                bmi < 25 -> {
                    bmiCategory = "Healthy / স্বাভাবিক"
                    categoryColor = SuccessColor
                }
                bmi < 30 -> {
                    bmiCategory = "Overweight"
                    categoryColor = WarningColor
                }
                else -> {
                    bmiCategory = "Obese"
                    categoryColor = ErrorColor
                }
            }
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "BMI Calculator",
                subtitle = "Check your Body Mass Index",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Enter Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                InputField(
                                    value = height,
                                    onValueChange = { height = it },
                                    label = "Height (cm)",
                                    placeholder = "170",
                                    leadingIcon = { Icon(Icons.Default.Height, null, tint = Primary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                InputField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    label = "Weight (kg)",
                                    placeholder = "65",
                                    leadingIcon = { Icon(Icons.Default.MonitorWeight, null, tint = Primary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        PrimaryButton(
                            text = "Calculate BMI",
                            onClick = { calculateBmi() },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Result Section
                AnimatedVisibility(
                    visible = bmiResult != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "YOUR SCORE",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", bmiResult ?: 0f),
                                style = MaterialTheme.typography.displayLarge,
                                color = categoryColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 56.sp
                            )
                            Text(
                                text = bmiCategory,
                                style = MaterialTheme.typography.headlineSmall,
                                color = categoryColor,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Premium Scale UI
                            BMIScale(bmiResult ?: 0f)

                            Spacer(modifier = Modifier.height(32.dp))

                            Surface(
                                color = categoryColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (bmiCategory.contains("Healthy")) 
                                        "Great job! Your weight is in the ideal range. Keep up your healthy lifestyle."
                                        else "Consult with a nutritionist to maintain a healthy weight and balanced diet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun BMIScale(score: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("15.0", style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text("Under", style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text("Normal", style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text("Over", style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text("40.0", style = MaterialTheme.typography.labelSmall, color = TextHint)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(Surface2Light)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(18.5f).fillMaxHeight().background(InfoColor))
                Box(modifier = Modifier.weight(6.5f).fillMaxHeight().background(SuccessColor))
                Box(modifier = Modifier.weight(5f).fillMaxHeight().background(WarningColor))
                Box(modifier = Modifier.weight(10f).fillMaxHeight().background(ErrorColor))
            }
            
            // Marker
            val position = ((score - 15f) / 25f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(position)
                    .padding(end = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(modifier = Modifier.size(8.dp, 20.dp).background(Color.White, CircleShape).shadow(2.dp, CircleShape))
            }
        }
    }
}
