package com.lifeplus.healthcare.ui.screens.intro

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.ui.components.AppBackground
import com.lifeplus.healthcare.ui.theme.*
import kotlinx.coroutines.launch

data class IntroPage(
    val title: String,
    val description: String,
    val lottieUrl: String,
    val fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector
)

val introPages = listOf(
    IntroPage(
        title = "Quality Care At Your Fingertips",
        description = "Book appointments, consult doctors, and manage your health journey with ease and security.",
        lottieUrl = "https://lottie.host/81a4b433-289b-4404-871d-1510e14a2278/XvXQp6Pj3m.json", // Doctor consultation
        fallbackIcon = Icons.Default.LocalHospital
    ),
    IntroPage(
        title = "24/7 Emergency Support",
        description = "Instant access to ambulances, emergency rooms, and blood banks when every second counts.",
        lottieUrl = "https://lottie.host/4f3e67c8-3e4b-4b2a-8d0b-6a6e9a7e6f8a/P8Z8z5YV3j.json", // Ambulance/Emergency
        fallbackIcon = Icons.Default.VolunteerActivism
    ),
    IntroPage(
        title = "Secure Medical Records",
        description = "Store prescriptions, lab reports, and vaccination history in your private health vault.",
        lottieUrl = "https://lottie.host/f8d2b9d1-3b5f-4d3b-8a8b-4a5e3d7a8b9c/m1P7z4Xv5j.json", // Medical records
        fallbackIcon = Icons.Default.Security
    ),
    IntroPage(
        title = "Your Smart Health Buddy",
        description = "Set smart medicine reminders, track BMI, and access life-saving first-aid guides anytime.",
        lottieUrl = "https://lottie.host/e9a2b7d3-4b6f-4d5b-8c7a-9a0e2d1f8b4a/v2X5z8Pq1m.json", // Smart health tracking
        fallbackIcon = Icons.Default.MonitorHeart
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    onNavigateNext: () -> Unit,
    onNavigateGuest: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { introPages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == introPages.size - 1

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HealthAndSafety, 
                        contentDescription = null, 
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LifePlus",
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                }
                
                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onNavigateGuest()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Skip", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Pager Section
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                IntroPageContent(introPages[page])
            }

            // Bottom Navigation Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(introPages.size) { i ->
                        val isSelected = pagerState.currentPage == i
                        val width by animateDpAsState(targetValue = if (isSelected) 28.dp else 8.dp)
                        
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (isSelected) Primary else Primary.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Get Started / Next Button
                Button(
                    onClick = {
                        if (isLastPage) {
                            viewModel.completeOnboarding()
                            onNavigateNext()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Primary),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isLastPage) "Get Started" else "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        onNavigateGuest()
                    },
                    enabled = !isLastPage
                ) {
                    Text(
                        text = "Continue as Guest",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun IntroPageContent(page: IntroPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Url(page.lottieUrl))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (composition == null) {
                Icon(
                    imageVector = page.fallbackIcon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(120.dp)
                )
            } else {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
