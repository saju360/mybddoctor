package com.lifeplus.healthcare.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.FrameLayout
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250
import com.facebook.ads.AdView as FbAdView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.lifeplus.healthcare.util.AppSettings
import com.lifeplus.healthcare.ui.theme.TextHint
import com.lifeplus.healthcare.ui.theme.Surface2Light

@Composable
fun DynamicBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings = AppSettings.get(context)
    if (!settings.isAdsEnabled() || !settings.isBannerEnabled()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Sponsored",
            style = MaterialTheme.typography.labelSmall,
            color = TextHint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Surface2Light, RoundedCornerShape(12.dp)),
            color = Color.White
        ) {
            val provider = settings.getAdProviderPriority()
            if (provider == "facebook") {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            val fb = FbAdView(ctx, settings.getFacebookBannerPlacementId(), RECTANGLE_HEIGHT_250)
                            addView(fb)
                            fb.loadAd()
                        }
                    }
                )
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = settings.getAdmobBannerUnitId()
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RewardedSupportAction(
    modifier: Modifier = Modifier,
    onNotAvailable: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings = AppSettings.get(context)
    if (!settings.isAdsEnabled() || !settings.isRewardedEnabled()) return

    val rewardAmount = settings.getRewardedPoints().coerceAtLeast(0)
    if (rewardAmount <= 0) return
    val rewardUnit = settings.getRewardedUnit().ifBlank { "Support Points" }

    LaunchedEffect(Unit) { AdsManager.preloadRewarded(context) }
    val activity = context.findActivity() ?: return

    var showConsent by remember { mutableStateOf(false) }
    var rewardBalance by remember { mutableIntStateOf(RewardPointsStore.getBalance(context)) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Surface2Light, RoundedCornerShape(16.dp)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Rewarded Ad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Watch one ad to earn $rewardAmount $rewardUnit.",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )
            Text(
                text = "Balance: $rewardBalance $rewardUnit",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            androidx.compose.material3.Button(
                onClick = { showConsent = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Watch & Earn")
            }
        }
    }

    if (showConsent) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Reward Confirmation") },
            text = { Text("Watch this rewarded ad to receive $rewardAmount $rewardUnit.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showConsent = false
                    val shown = AdsManager.showRewarded(activity) {
                        rewardBalance = RewardPointsStore.addPoints(context, rewardAmount)
                    }
                    if (!shown) onNotAvailable()
                }) {
                    Text("Watch Ad")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showConsent = false }) {
                    Text("Not Now")
                }
            }
        )
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
