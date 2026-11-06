package com.docsalert.reminder.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdHelper {
    private var rewardedAd: RewardedAd? = null
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            }
        )
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { _ ->
                onRewardEarned()
                rewardedAd = null
                loadAd(activity)
            }
        } else {
            Toast.makeText(activity, "Ad loading... Reward granted!", Toast.LENGTH_SHORT).show()
            onRewardEarned()
            loadAd(activity)
        }
    }
}
