package com.lifeplus.healthcare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.presentation.viewmodel.HealthRecordViewModel
import com.lifeplus.healthcare.ui.screens.profile.ProfileScreen
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lifeplus.healthcare.ui.components.MeoBottomNavBar
import com.lifeplus.healthcare.ui.components.AppBackground
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.launch
import com.lifeplus.healthcare.ui.components.NavDrawerContent
import com.lifeplus.healthcare.ui.theme.*

@Composable
fun MainScreen(
    onNavigateFeature: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userFullName by authViewModel.fullName.collectAsState()
    val userPhone by authViewModel.phone.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawerContent(
                isLoggedIn = isLoggedIn,
                userFullName = userFullName,
                userPhone = userPhone,
                onNavigate = { route ->
                    if (route.endsWith("_tab")) {
                        bottomNavController.navigate(route) {
                            bottomNavController.graph.startDestinationRoute?.let { popUpTo(it) { saveState = true } }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        onNavigateFeature(route)
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    onNavigateFeature("login")
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                MeoBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        bottomNavController.navigate(route) {
                            bottomNavController.graph.startDestinationRoute?.let { startRoute ->
                                popUpTo(startRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = bottomNavController,
                        startDestination = "home_tab"
                    ) {
                        composable("home_tab") {
                            com.lifeplus.healthcare.ui.screens.dashboard.DashboardScreen(
                                onNavigate = { route ->
                                    val premiumFeatures = listOf("appointments", "telemedicine", "health_records", "reminders", "manage_listings", "donor_register", "blood_org", "donation_requests", "chat")
                                    if (route.endsWith("_tab")) {
                                        bottomNavController.navigate(route) {
                                            bottomNavController.graph.startDestinationRoute?.let { popUpTo(it) { saveState = true } }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else if (!isLoggedIn && premiumFeatures.contains(route)) {
                                        onNavigateFeature("login")
                                    } else {
                                        onNavigateFeature(route)
                                    }
                                },
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable("search_tab") {
                            com.lifeplus.healthcare.ui.screens.browse.ExploreScreen(
                                onNavigate = { route ->
                                    val premiumFeatures = listOf("appointments", "telemedicine", "health_records", "reminders", "manage_listings", "donor_register", "blood_org", "donation_requests", "chat")
                                    if (route.endsWith("_tab")) {
                                        bottomNavController.navigate(route) {
                                            bottomNavController.graph.startDestinationRoute?.let { popUpTo(it) { saveState = true } }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else if (!isLoggedIn && premiumFeatures.contains(route)) {
                                        onNavigateFeature("login")
                                    } else {
                                        onNavigateFeature(route)
                                    }
                                },
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable("health_tab") {
                            if (isLoggedIn) {
                                HealthRecordsWrapper(onNavigateBack = { bottomNavController.navigateUp() })
                            } else {
                                PremiumGuestLockScreen(
                                    featureName = "Health Records",
                                    featureIcon = Icons.Default.FolderOpen,
                                    description = "Store prescriptions, lab reports and medical history safely in your private vault.",
                                    perks = listOf("End-to-end encrypted storage", "Upload & view documents", "Track your medical history"),
                                    onLogin = { onNavigateFeature("login") }
                                )
                            }
                        }
                        composable("profile_tab") {
                            ProfileScreen(
                                onNavigate = { route ->
                                    val premiumFeatures = listOf("appointments", "telemedicine", "health_records", "reminders", "manage_listings", "donor_register", "blood_org", "donation_requests", "chat")
                                    if (route.endsWith("_tab")) {
                                        bottomNavController.navigate(route) {
                                            bottomNavController.graph.startDestinationRoute?.let { popUpTo(it) { saveState = true } }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else if (!isLoggedIn && premiumFeatures.contains(route)) {
                                        onNavigateFeature("login")
                                    } else {
                                        onNavigateFeature(route)
                                    }
                                },
                                onLogout = { onNavigateFeature("login") }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HealthRecordsWrapper(onNavigateBack: () -> Unit) {
    val viewModel: HealthRecordViewModel = hiltViewModel()
    com.lifeplus.healthcare.ui.screens.features.HealthRecordsScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun PremiumGuestLockScreen(
    featureName: String = "Premium Feature",
    featureIcon: ImageVector = Icons.Default.Lock,
    description: String = "Sign in to unlock this feature.",
    perks: List<String> = emptyList(),
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Icon
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.06f)
                ) {}
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.1f)
                ) {}
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(8.dp, CircleShape, spotColor = Primary),
                    shape = CircleShape,
                    color = Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = featureIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = featureName,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            if (perks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "What you'll get",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        perks.forEach { perk ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    color = SuccessColor.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint = SuccessColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    perk,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Login, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Sign In to Unlock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Free • No Credit Card Required",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }
    }
}

// Keep old GuestLockScreen for compatibility but redirect to premium version
@Composable
fun GuestLockScreen(onLogin: () -> Unit) {
    PremiumGuestLockScreen(onLogin = onLogin)
}
