package com.lifeplus.healthcare.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.BuildConfig
import com.lifeplus.healthcare.R
import com.lifeplus.healthcare.ui.theme.GradientPrimary
import com.lifeplus.healthcare.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    val appName = stringResource(R.string.app_name)
    val appTagline = stringResource(R.string.app_tagline)

    LaunchedEffect(Unit) {
        delay(300)
        showLogo = true
        delay(600)
        showText = true
        delay(2200)
        onNavigateNext()
    }

    // Background animation: Floating circles
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val circleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle1"
    )
    val circleOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientPrimary)
    ) {
        // Decorative background elements
        Surface(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp + circleOffset1.dp, y = (-100).dp + circleOffset2.dp)
                .alpha(0.1f),
            shape = CircleShape,
            color = Color.White
        ) {}

        Surface(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp + circleOffset2.dp, y = 50.dp + circleOffset1.dp)
                .alpha(0.08f),
            shape = CircleShape,
            color = Color.White
        ) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Logo
            AnimatedVisibility(
                visible = showLogo,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)) + fadeIn()
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (showLogo) 360f else 0f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing),
                    label = "logo_rotation"
                )
                
                Surface(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer(rotationZ = rotation)
                        .shadow(20.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.3f)),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Animated App Name & Tagline
            AnimatedVisibility(
                visible = showText,
                enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = appTagline,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Version text at bottom
        AnimatedVisibility(
            visible = showText,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            enter = fadeIn(animationSpec = tween(1000))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Simple loading indicator
                LinearProgressIndicator(
                    modifier = Modifier.width(40.dp).height(2.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
