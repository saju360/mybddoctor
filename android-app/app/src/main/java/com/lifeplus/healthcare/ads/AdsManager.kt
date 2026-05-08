package com.lifeplus.healthcare.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.lifeplus.healthcare.util.AppSettings
import java.util.concurrent.atomic.AtomicBoolean

object AdsManager {
    private val initialized = AtomicBoolean(false)

    private var admobInterstitial: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    private var facebookInterstitial: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var appOpenAd: com.google.android.gms.ads.appopen.AppOpenAd? = null

    private var navActionCounter = 0
    private var lastInterstitialShownAt = 0L
    private var isShowingAd = false

    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            MobileAds.initialize(context)
            AudienceNetworkAds.initialize(context)
        }
    }

    fun refreshPolicyAndConsent(activity: Activity) {
        applyRequestConfiguration(activity)
        requestConsentAndLoadAds(activity)
    }

    fun preloadInterstitial(context: Context) {
        val settings = AppSettings.get(context)
        if (!settings.isAdsEnabled() || !settings.isInterstitialEnabled()) return
        val provider = settings.getAdProviderPriority()
        if (provider == "facebook") {
            val unitId = settings.getFacebookInterstitialPlacementId()
            if (unitId.isBlank()) return
            facebookInterstitial?.destroy()
            facebookInterstitial = InterstitialAd(context, unitId).apply {
                loadAd(buildLoadAdConfig().withAdListener(object : InterstitialAdListener {
                    override fun onInterstitialDisplayed(ad: com.facebook.ads.Ad?) {}
                    override fun onInterstitialDismissed(ad: com.facebook.ads.Ad?) { preloadInterstitial(context) }
                    override fun onError(ad: com.facebook.ads.Ad?, error: com.facebook.ads.AdError?) {}
                    override fun onAdLoaded(ad: com.facebook.ads.Ad?) {}
                    override fun onAdClicked(ad: com.facebook.ads.Ad?) {}
                    override fun onLoggingImpression(ad: com.facebook.ads.Ad?) {}
                }).build())
            }
        } else {
            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                context,
                settings.getAdmobInterstitialUnitId(),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                        admobInterstitial = ad
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) { admobInterstitial = null }
                }
            )
        }
    }

    fun maybeShowInterstitial(activity: Activity, navigationAction: String) {
        val settings = AppSettings.get(activity)
        if (!settings.isAdsEnabled() || !settings.isInterstitialEnabled()) return
        
        // Prevent showing ads on sensitive or main navigation tabs (AdMob Policy)
        if (navigationAction in setOf("home_tab", "profile_tab", "search_tab", "health_tab", "emergency", "blood_request")) return
        
        val now = SystemClock.elapsedRealtime()
        val cooldownMs = settings.getInterstitialCooldownSeconds().coerceAtLeast(60) * 1000L // Increased default cooldown
        
        navActionCounter += 1
        // Show ad only after a certain number of meaningful clicks
        if (navActionCounter < settings.getInterstitialEveryNClicks().coerceAtLeast(5)) return
        if (now - lastInterstitialShownAt < cooldownMs) return
        if (isShowingAd) return

        navActionCounter = 0
        lastInterstitialShownAt = now
        
        if (settings.getAdProviderPriority() == "facebook" && facebookInterstitial?.isAdLoaded == true) {
            facebookInterstitial?.show()
            preloadInterstitial(activity)
            return
        }
        
        val ad = admobInterstitial ?: return
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { isShowingAd = true }
            override fun onAdDismissedFullScreenContent() { 
                isShowingAd = false
                admobInterstitial = null
                preloadInterstitial(activity)
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isShowingAd = false
                admobInterstitial = null
                preloadInterstitial(activity)
            }
        }
        ad.show(activity)
    }

    fun loadAppOpenAd(context: Context) {
        val settings = AppSettings.get(context)
        if (!settings.isAdsEnabled()) return
        
        val adUnitId = settings.getAdmobAppOpenUnitId().ifBlank { "ca-app-pub-3940256099942544/9257391923" } // Test ID if not set
        
        com.google.android.gms.ads.appopen.AppOpenAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                    appOpenAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                }
            }
        )
    }

    fun showAppOpenAdIfAvailable(activity: Activity) {
        if (isShowingAd) return
        val ad = appOpenAd ?: return
        
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { isShowingAd = true }
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                appOpenAd = null
                loadAppOpenAd(activity)
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isShowingAd = false
                appOpenAd = null
                loadAppOpenAd(activity)
            }
        }
        ad.show(activity)
    }

    fun preloadRewarded(context: Context) {
        val settings = AppSettings.get(context)
        if (!settings.isAdsEnabled() || !settings.isRewardedEnabled()) return
        RewardedAd.load(
            context,
            settings.getAdmobRewardedUnitId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { rewardedAd = null }
            }
        )
    }

    fun showRewarded(activity: Activity, onRewardEarned: (RewardItem) -> Unit): Boolean {
        val ad = rewardedAd ?: return false
        ad.show(activity, onRewardEarned)
        rewardedAd = null
        preloadRewarded(activity)
        return true
    }

    private fun applyRequestConfiguration(context: Context) {
        val settings = AppSettings.get(context)
        val maxRating = when (settings.getMaxAdContentRating().uppercase()) {
            "G" -> RequestConfiguration.MAX_AD_CONTENT_RATING_G
            "PG" -> RequestConfiguration.MAX_AD_CONTENT_RATING_PG
            "MA" -> RequestConfiguration.MAX_AD_CONTENT_RATING_MA
            else -> RequestConfiguration.MAX_AD_CONTENT_RATING_T
        }
        val childTag = if (settings.isTagForChildDirectedTreatment()) {
            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
        } else {
            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
        }
        val underAgeTag = if (settings.isTagForUnderAgeOfConsent()) {
            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
        } else {
            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE
        }
        val config = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(childTag)
            .setTagForUnderAgeOfConsent(underAgeTag)
            .setMaxAdContentRating(maxRating)
            .build()
        MobileAds.setRequestConfiguration(config)
    }

    private fun requestConsentAndLoadAds(activity: Activity) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable &&
                    consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED
                ) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                        if (consentInformation.canRequestAds()) {
                            preloadInterstitial(activity)
                            preloadRewarded(activity)
                        }
                    }
                } else if (consentInformation.canRequestAds()) {
                    preloadInterstitial(activity)
                    preloadRewarded(activity)
                }
            },
            { _ ->
                if (consentInformation.canRequestAds()) {
                    preloadInterstitial(activity)
                    preloadRewarded(activity)
                }
            }
        )
    }
}
