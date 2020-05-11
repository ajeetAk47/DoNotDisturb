package com.ak47.doNotDisturb.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ak47.doNotDisturb.Activities.MainActivity;
import com.ak47.doNotDisturb.R;
import com.ak47.doNotDisturb.Receiver.CallReceiver;
import com.ak47.doNotDisturb.Receiver.RingerModeStateChangeReceiver;
import com.ak47.doNotDisturb.Worker.AdLoadAndShowWorker;

import java.util.concurrent.TimeUnit;

public class HelperForegroundService extends Service {
    String TAG = "Logging - HelperForegroundService ";
    String notificationChannelIdForHelperService = "1000";
    int foregroundServiceID = 100;
    CallReceiver callReceiver = new CallReceiver();
    RingerModeStateChangeReceiver ringerModeStateChangeReceiver = new RingerModeStateChangeReceiver();
    //    CustomNotificationListenerService customNotificationListenerService = new CustomNotificationListenerService();
    private AudioManager mAudioManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate: " + "called");
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("mode_preference", "Silent");
        String visibility = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("notification_visibility", "0");
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                300,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this, notificationChannelIdForHelperService)
                .setSmallIcon(R.drawable.ic_check)
                .setContentIntent(mainActivityPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(status + " Mode")
                        .setSummaryText("Running")
                ).setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setVisibility(Integer.parseInt(visibility))
                .build();

        IntentFilter intentFilterCallReceiver = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, intentFilterCallReceiver);
        IntentFilter intentFilterRingerModeStateChangeReceiver = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        changeMode();
        registerReceiver(ringerModeStateChangeReceiver, intentFilterRingerModeStateChangeReceiver);
//        IntentFilter intentFilterCustomNotificationListenerService=new IntentFilter("com.ak47.doNotDisturb.Service.CustomNotificationListenerService");
//        registerReceiver(customNotificationListenerService,intentFilterCustomNotificationListenerService);
        startForeground(foregroundServiceID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + "service started " + startId);

        PeriodicWorkRequest periodicAdWork = new PeriodicWorkRequest.Builder(AdLoadAndShowWorker.class, 1, TimeUnit.HOURS, 20, TimeUnit.MINUTES).build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork("periodicAdWorkName", ExistingPeriodicWorkPolicy.KEEP, periodicAdWork);


        return START_STICKY;
    }

    @SuppressLint("WrongConstant")
    private void changeMode() {
        String mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("mode_preference", "Silent");
        if (mode.equals("Do Not Disturb")) {
            //Do Not  Disturb Mode

            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        } else if (mode.equals("Silent")) {
            //Silent  Mode
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        Log.d(TAG, "Change Mode - " + mode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + "called");
        try {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            // mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("periodicAdWorkName");
            unregisterReceiver(ringerModeStateChangeReceiver);
            unregisterReceiver(callReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
