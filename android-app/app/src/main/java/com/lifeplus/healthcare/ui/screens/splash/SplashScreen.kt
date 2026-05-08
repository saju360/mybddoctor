package com.lifeplus.healthcare.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeplus.healthcare.BuildConfig
import com.lifeplus.healthcare.R
import com.lifeplus.healthcare.ui.theme.GradientPrimary
import com.lifeplus.healthcare.ui.theme.Primary
import com.lifeplus.healthcare.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    var showLogo by remember { mutableStateOf(false) }
    var showName by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }

    val appName = stringResource(R.string.app_name)
    val appTagline = stringResource(R.string.app_tagline)

    LaunchedEffect(Unit) {
        showLogo = true
        delay(450)
        showName = true
        delay(400)
        showTagline = true
        delay(2400)
        onNavigateNext()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "logo_scale"
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (showLogo) 360f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "logo_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientPrimary)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .scale(logoScale)
                    .graphicsLayer(rotationZ = logoRotation)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = showName,
                enter = fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing))
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Primary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing))
            ) {
                Text(
                    text = appTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}
