package com.ak47.doNotDisturb.Application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.ak47.doNotDisturb.R;
import com.google.android.gms.ads.MobileAds;

public class App extends Application {
    String notificationChannelIdForHelperService = "1000";
    String notificationChannelNameForHelperService = "Tile Change"; //Helper Foreground Service Channel
    String notificationChannelIdForRingerModeReceiver = "2000";
    String notificationChannelNameForRingerModeReceiver = "Mode Change"; //Ringer Mode Receiver Channel
    String notificationChannelIdForAdsAndService = "3000";
    String notificationChannelNameForAdsAndService = "Support";

    @Override
    public void onCreate() {
        createNotificationChannelForForegroundService();
        createNotificationChannelForRingerModeReceiver();
        initializeMobileSdk();
        super.onCreate();
    }

    private void initializeMobileSdk() {
        MobileAds.initialize(getApplicationContext(), getApplicationContext().getString(R.string.admob_app_id));
    }


    private void createNotificationChannelForRingerModeReceiver() {
        NotificationChannel notificationChannel = new NotificationChannel(notificationChannelIdForRingerModeReceiver, notificationChannelNameForRingerModeReceiver, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void createNotificationChannelForForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannelForHelperService = new NotificationChannel(
                    notificationChannelIdForHelperService,
                    notificationChannelNameForHelperService,
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationChannel notificationChannelForAdsAndService = new NotificationChannel(
                    notificationChannelIdForAdsAndService,
                    notificationChannelNameForAdsAndService,
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannelForHelperService);
                notificationManager.createNotificationChannel(notificationChannelForAdsAndService);
            }
        }
    }
}
