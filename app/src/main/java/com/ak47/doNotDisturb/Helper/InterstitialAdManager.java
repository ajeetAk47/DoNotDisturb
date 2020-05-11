package com.ak47.doNotDisturb.Helper;

import android.util.Log;

import com.google.android.gms.ads.InterstitialAd;

public class InterstitialAdManager {
    // Singleton Class support
    private static InterstitialAdManager interstitialAdManagerSingleton;
    private InterstitialAd interstitialAd;

    private InterstitialAdManager() {
    }

    public static InterstitialAdManager getInstance() {
        if (interstitialAdManagerSingleton == null) {
            String TAG = "Logging - InterstitialAdManager";
            Log.d(TAG, "getInstance: " + "instance is null...");
            interstitialAdManagerSingleton = new InterstitialAdManager();
        }
        return interstitialAdManagerSingleton;
    }

    // Normal class support
    public InterstitialAd getInterstitialAd() {
        return this.interstitialAd;
    }

    public void setInterstitialAd(InterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
    }
}
