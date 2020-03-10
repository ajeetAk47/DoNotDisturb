package com.ak47.donotdisturb.Application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application
{
    String notificationChannelIdForHelperService = "1000";
    String notificationChannelNameForHelperService = "Helper Foreground Service Channel";
    String notificationChannelIdForRingerModeReceiver="2000";
    String notificationChannelNameForRingerModeReceiver="Ringer Mode Receiver Channel";
    @Override
    public void onCreate()
    {
        createNotificationChannelForForegroundService();
        createNotificationChannelForRingerModeReceiver();
        super.onCreate();
    }

    private void createNotificationChannelForRingerModeReceiver()
    {
        NotificationChannel notificationChannel = new NotificationChannel(notificationChannelIdForRingerModeReceiver , notificationChannelNameForRingerModeReceiver, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void createNotificationChannelForForegroundService()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannelForHelperService = new NotificationChannel(
                    notificationChannelIdForHelperService,
                    notificationChannelNameForHelperService,
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannelForHelperService);
            }
        }
    }
}
