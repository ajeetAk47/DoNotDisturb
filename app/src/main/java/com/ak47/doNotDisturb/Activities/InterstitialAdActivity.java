package com.ak47.doNotDisturb.Activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ak47.doNotDisturb.Helper.InterstitialAdManager;
import com.ak47.doNotDisturb.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;

public class InterstitialAdActivity extends AppCompatActivity {
    String TAG = "Logging - InterstitialAdActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial_ad);
        Log.d(TAG, "onCreate: " + "activity started");
        InterstitialAdManager interstitialAdManager = InterstitialAdManager.getInstance();
        InterstitialAd interstitialAd = interstitialAdManager.getInterstitialAd();
        if (interstitialAd == null) {
            Log.d(TAG, "onCreate: " + "ad is null");
            finish();
        }

        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
