package ru.stan.mydesck.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import ru.stan.mydesck.R
import ru.stan.mydesck.utils.BillingManager

open class BaseAdsFragment : Fragment(), InterAdsClose {
    lateinit var adView: AdView
    var inTerAd: InterstitialAd? = null
    private var pref: SharedPreferences? = null
    private var isPremium = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = activity?.getSharedPreferences(BillingManager.MAIN_PREF, AppCompatActivity.MODE_PRIVATE)
        isPremium = pref?.getBoolean(BillingManager.REMOVE_ADS_PREF, false)!!
        isPremium = true
        if (!isPremium){
            initAds()
            loadInterAd()
        }
        else {
            adView.visibility = View.GONE
        }

    }


    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    private fun initAds() {
        MobileAds.initialize(activity as Activity)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context as Context,
            getString(R.string.ad_iner_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    inTerAd = ad
                }
            })
    }

    fun showInterAd() {
        if (inTerAd != null) {
            inTerAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    unClose()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    unClose()
                }
            }
            inTerAd?.show(activity as Activity)

        } else {
            unClose()
        }
    }

    override fun unClose() {
    }

}