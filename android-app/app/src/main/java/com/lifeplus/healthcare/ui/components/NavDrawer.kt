package com.lifeplus.healthcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.theme.*

import androidx.compose.material.icons.filled.HealthAndSafety

import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun NavDrawerContent(
    isLoggedIn: Boolean,
    userFullName: String?,
    userPhone: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userRole by authViewModel.role.collectAsState()
    val isAuthorized = userRole?.uppercase() == "ADMIN" || userRole?.uppercase() == "OWNER"
    val context = LocalContext.current

    ModalDrawerSheet(
        drawerContainerColor = BackgroundLight,
        drawerContentColor = TextPrimary,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Premium Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(PremiumBlueGradient)
            ) {
                // Decorative Circle
                Surface(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = (-30).dp, y = (-30).dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {}

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoggedIn) {
                                Text(
                                    text = userFullName?.take(1)?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isLoggedIn) {
                        Text(
                            text = userFullName ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = userPhone ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "Guest User",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Login to access all features",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.clickable { onNavigate("login"); onClose() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items with Premium Styling
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = "Main Menu",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                
                DrawerItem(Icons.Outlined.Home, "Home", onClick = { onNavigate("home_tab"); onClose() })
                DrawerItem(Icons.Outlined.Explore, "Explore Services", onClick = { onNavigate("search_tab"); onClose() })
                DrawerItem(Icons.Outlined.WaterDrop, "Blood Requests", onClick = { onNavigate("donation_requests"); onClose() })
                DrawerItem(Icons.Outlined.FavoriteBorder, "Available Donors", onClick = { onNavigate("browse_donors"); onClose() })
                DrawerItem(Icons.Outlined.History, "Health Records", onClick = { onNavigate("health_tab"); onClose() })
                
                if (isAuthorized) {
                    DrawerItem(Icons.Outlined.EditAttributes, "Manage Listings", onClick = { onNavigate("manage_listings"); onClose() })
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp), color = Surface2Light)
                
                Text(
                    text = "Quick Search",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                DrawerItem(Icons.Outlined.LocalHospital, "Hospitals", onClick = { onNavigate("browse_hospitals"); onClose() })
                DrawerItem(Icons.Outlined.PersonSearch, "Doctors", onClick = { onNavigate("browse_doctors"); onClose() })
                DrawerItem(Icons.Outlined.Emergency, "Emergency Help", onClick = { onNavigate("emergency"); onClose() })
                DrawerItem(Icons.Outlined.ChatBubbleOutline, "Messages", onClick = { onNavigate("chat"); onClose() })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp), color = Surface2Light)
                
                DrawerItem(Icons.Outlined.Settings, "Settings", onClick = { onNavigate("settings"); onClose() })
                
                Spacer(modifier = Modifier.weight(1f))
                
                DrawerItem(
                    Icons.Outlined.SupportAgent, 
                    "Contact Support", 
                    onClick = { 
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@lifeplus.health"))
                        context.startActivity(intent)
                        onClose() 
                    }
                )
                if (isLoggedIn) {
                    DrawerItem(
                        icon = Icons.AutoMirrored.Filled.Logout, 
                        title = "Logout", 
                        onClick = { onLogout(); onClose() }, 
                        color = ErrorColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, title: String, onClick: () -> Unit, color: Color = TextPrimary) {
    NavigationDrawerItem(
        label = { 
            Text(
                text = title, 
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            ) 
        },
        selected = false,
        onClick = onClick,
        icon = { 
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (color == TextPrimary) Primary else color,
                modifier = Modifier.size(24.dp)
            ) 
        },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = color
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
